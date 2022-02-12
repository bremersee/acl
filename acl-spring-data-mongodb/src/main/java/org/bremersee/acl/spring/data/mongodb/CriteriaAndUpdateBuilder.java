/*
 * Copyright 2020 the original author or authors.
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

import static org.springframework.util.ObjectUtils.isEmpty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import org.bremersee.acl.AccessEvaluation;
import org.bremersee.acl.Ace;
import org.bremersee.acl.Acl;
import org.bremersee.acl.UserContext;
import org.bremersee.acl.model.AccessControlEntryModifications;
import org.bremersee.acl.model.AccessControlListModifications;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;

/**
 * @author Christian Bremer
 */
public class CriteriaAndUpdateBuilder {

  private final String aclPath;

  public CriteriaAndUpdateBuilder(String aclPath) {
    this.aclPath = Objects.isNull(aclPath) ? "" : aclPath;
  }

  public Update buildUpdate(
      @NotNull AccessControlListModifications accessControlListModifications) {

    Collection<AccessControlEntryModifications> mods = accessControlListModifications
        .getModifications();
    Update update = new Update();
    for (AccessControlEntryModifications mod : mods) {
      update = update.set(
          path(Acl.ENTRIES, mod.getPermission(), Ace.GUEST),
          mod.isGuest());
      if (!mod.getAddUsers().isEmpty()) {
        for (String user : mod.getAddUsers()) {
          update = update.push(
              path(Acl.ENTRIES, mod.getPermission(), Ace.USERS),
              user);
        }
      }
      if (!mod.getRemoveUsers().isEmpty()) {
        update = update.pullAll(
            path(Acl.ENTRIES, mod.getPermission(), Ace.USERS),
            mod.getRemoveUsers().toArray(new String[0]));
      }
      if (!mod.getAddRoles().isEmpty()) {
        for (String role : mod.getAddRoles()) {
          update = update.push(
              path(Acl.ENTRIES, mod.getPermission(), Ace.ROLES),
              role);
        }
      }
      if (!mod.getRemoveRoles().isEmpty()) {
        update = update.pullAll(
            path(Acl.ENTRIES, mod.getPermission(), Ace.ROLES),
            mod.getRemoveRoles().toArray(new String[0]));
      }
      if (!mod.getAddGroups().isEmpty()) {
        for (String group : mod.getAddGroups()) {
          update = update.push(
              path(Acl.ENTRIES, mod.getPermission(), Ace.GROUPS),
              group);
        }
      }
      if (!mod.getRemoveGroups().isEmpty()) {
        update = update.pullAll(
            path(Acl.ENTRIES, mod.getPermission(), Ace.GROUPS),
            mod.getRemoveGroups().toArray(new String[0]));
      }
    }
    return update;
  }

  public Update buildUpdate(Acl acl) {
    return Update.update(path(), isEmpty(acl) ? Acl.builder().build() : acl);
  }

  public Update buildUpdate(String newOwner) {
    return Update.update(path(Acl.OWNER), newOwner);
  }

  public Criteria buildUpdateOwnerCriteria(@NotNull UserContext userContext) {
    return Criteria.where(path(Acl.OWNER)).is(userContext.getName());
  }

  public Criteria buildPermissionCriteria(
      @NotNull UserContext userContext,
      @NotNull AccessEvaluation accessEvaluation,
      @NotEmpty Collection<String> permissions) {

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
      UserContext userContext,
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
        .collect(Collectors.toList())
    );
    criteriaList.addAll(userContext.getGroups().stream()
        .filter(group -> !isEmpty(group))
        .map(group -> Criteria
            .where(path(Acl.ENTRIES, permission, Ace.GROUPS))
            .all(group))
        .collect(Collectors.toList())
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
