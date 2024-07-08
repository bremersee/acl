/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.acl.spring.data.mongodb;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bremersee.acl.AccessEvaluation;
import org.bremersee.acl.Ace;
import org.bremersee.acl.Acl;
import org.bremersee.acl.AclUserContext;
import org.bremersee.acl.annotation.AclHolder;
import org.bremersee.acl.model.AccessControlEntryModifications;
import org.bremersee.acl.model.AccessControlListModifications;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.Assert;

/**
 * The acl criteria and update builder.
 *
 * @author Christian Bremer
 */
public class AclCriteriaAndUpdateBuilder {

  private final String aclPath;

  /**
   * Instantiates a new acl criteria and update builder.
   *
   * @param aclPath the acl path
   */
  public AclCriteriaAndUpdateBuilder(String aclPath) {
    this.aclPath = Objects.isNull(aclPath) ? "" : aclPath;
  }

  /**
   * Instantiates a new acl criteria and update builder.
   *
   * @param entityClass the entity class
   */
  public AclCriteriaAndUpdateBuilder(Class<?> entityClass) {
    Assert.notNull(entityClass, "Entity class must be present.");
    this.aclPath = Optional
        .ofNullable(findAnnotation(entityClass, AclHolder.class))
        .map(AclHolder::path)
        .orElseThrow(() -> new IllegalArgumentException(String
            .format(
                "Entity class %s must be annotated with %s.",
                entityClass.getSimpleName(), AclHolder.class.getSimpleName())));
  }

  /**
   * Build update acl modification update.
   *
   * @param accessControlListModifications the access control list modifications
   * @return the acl modification update
   */
  public AclModificationUpdate buildUpdate(
      AccessControlListModifications accessControlListModifications) {

    Collection<AccessControlEntryModifications> mods = isEmpty(accessControlListModifications)
        ? List.of()
        : accessControlListModifications.getModificationsDistinct();

    Update addAndSetUpdate = new Update();
    Update removeUpdate = new Update();
    boolean isSomethingRemoved = false;

    for (AccessControlEntryModifications mod : mods) {

      // guest
      addAndSetUpdate = addAndSetUpdate.set(
          path(Acl.ENTRIES, mod.getPermission(), Ace.GUEST),
          mod.isGuest());

      // users
      if (!mod.getAddUsers().isEmpty()) {
        addAndSetUpdate = addAndSetUpdate
            .addToSet(path(Acl.ENTRIES, mod.getPermission(), Ace.USERS))
            .each((Object[]) mod.getAddUsers().toArray(new String[0]));
      }
      if (!mod.getRemoveUsers().isEmpty()) {
        isSomethingRemoved = true;
        removeUpdate = removeUpdate.pullAll(
            path(Acl.ENTRIES, mod.getPermission(), Ace.USERS),
            mod.getRemoveUsers().toArray(new String[0]));
      }

      // roles
      if (!mod.getAddRoles().isEmpty()) {
        addAndSetUpdate = addAndSetUpdate
            .addToSet(path(Acl.ENTRIES, mod.getPermission(), Ace.ROLES))
            .each((Object[]) mod.getAddRoles().toArray(new String[0]));
      }
      if (!mod.getRemoveRoles().isEmpty()) {
        isSomethingRemoved = true;
        removeUpdate = removeUpdate.pullAll(
            path(Acl.ENTRIES, mod.getPermission(), Ace.ROLES),
            mod.getRemoveRoles().toArray(new String[0]));
      }

      if (!mod.getAddGroups().isEmpty()) {
        addAndSetUpdate = addAndSetUpdate
            .addToSet(path(Acl.ENTRIES, mod.getPermission(), Ace.GROUPS))
            .each((Object[]) mod.getAddGroups().toArray(new String[0]));
      }
      if (!mod.getRemoveGroups().isEmpty()) {
        isSomethingRemoved = true;
        removeUpdate = removeUpdate.pullAll(
            path(Acl.ENTRIES, mod.getPermission(), Ace.GROUPS),
            mod.getRemoveGroups().toArray(new String[0]));
      }
    }
    return AclModificationUpdate.builder()
        .preparationUpdates(isSomethingRemoved ? List.of(addAndSetUpdate) : List.of())
        .finalUpdate(isSomethingRemoved ? removeUpdate : addAndSetUpdate)
        .build();
  }

  /**
   * Build update.
   *
   * @param acl the acl
   * @return the update
   */
  public Update buildUpdate(Acl acl) {
    return Update.update(path(), isEmpty(acl) ? Acl.builder().build() : acl);
  }

  /**
   * Build update.
   *
   * @param newOwner the new owner
   * @return the update
   */
  public Update buildUpdate(String newOwner) {
    return Update.update(path(Acl.OWNER), isEmpty(newOwner) ? "" : newOwner);
  }

  /**
   * Build update owner criteria.
   *
   * @param userContext the user context
   * @return the criteria
   */
  public Criteria buildUpdateOwnerCriteria(AclUserContext userContext) {
    Assert.notNull(userContext, "User context must be present.");
    return Criteria.where(path(Acl.OWNER)).is(userContext.getName());
  }

  /**
   * Build permission criteria.
   *
   * @param userContext the user context
   * @param accessEvaluation the access evaluation
   * @param permissions the permissions
   * @return the criteria
   */
  public Criteria buildPermissionCriteria(
      AclUserContext userContext,
      AccessEvaluation accessEvaluation,
      Collection<String> permissions) {

    Assert.notNull(userContext, "User context must be present.");
    Assert.notNull(accessEvaluation, "Access evaluation type must be present.");
    Assert.notEmpty(permissions, "At least one permission must be present.");

    List<Criteria> permissionCriteriaList = Set.copyOf(permissions).stream()
        .map(permission -> createAccessCriteria(userContext, permission))
        .collect(Collectors.toList());
    Criteria permissionCriteria = accessEvaluation.isAnyPermission()
        ? new Criteria().orOperator(permissionCriteriaList)
        : new Criteria().andOperator(permissionCriteriaList);
    if (userContext.getName().isBlank()) {
      return permissionCriteria;
    }
    Criteria ownerCriteria = Criteria.where(path(Acl.OWNER)).is(userContext.getName());
    return new Criteria().orOperator(ownerCriteria, permissionCriteria);
  }

  private Criteria createAccessCriteria(
      AclUserContext userContext,
      String permission) {

    List<Criteria> criteriaList = new ArrayList<>();
    criteriaList.add(Criteria.where(path(Acl.ENTRIES, permission, Ace.GUEST)).is(true));
    if (!userContext.getName().isBlank()) {
      criteriaList.add(Criteria
          .where(path(Acl.ENTRIES, permission, Ace.USERS))
          .all(userContext.getName()));
    }
    criteriaList.addAll(userContext.getRoles().stream()
        .filter(role -> !isEmpty(role))
        .map(role -> Criteria.
            where(path(Acl.ENTRIES, permission, Ace.ROLES))
            .all(role))
        .toList()
    );
    criteriaList.addAll(userContext.getGroups().stream()
        .filter(group -> !isEmpty(group))
        .map(group -> Criteria
            .where(path(Acl.ENTRIES, permission, Ace.GROUPS))
            .all(group))
        .toList()
    );
    return new Criteria().orOperator(criteriaList);
  }

  private String path(String... pathSegments) {
    if (isEmpty(pathSegments)) {
      return aclPath;
    }
    return aclPath + "." + String.join(".", pathSegments);
  }

}
