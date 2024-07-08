/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.acl;

import static java.util.Collections.unmodifiableSortedMap;
import static java.util.Objects.nonNull;
import static org.bremersee.acl.AclUserContext.ANONYMOUS;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bremersee.acl.model.AccessControlListModifications;

/**
 * The access control list.
 *
 * @author Christian Bremer
 */
public interface Acl {

  /**
   * The constant OWNER.
   */
  String OWNER = "owner";

  /**
   * The constant ENTRIES.
   */
  String ENTRIES = "entries";

  /**
   * Builder acl builder.
   *
   * @return the acl builder
   */
  static AclBuilder builder() {
    return new AclBuilder();
  }

  /**
   * With acl.
   *
   * @param owner the owner
   * @param defaultPermissions the default permissions
   * @param adminRoles the admin roles
   * @return the acl
   */
  static Acl with(
      String owner,
      Collection<String> defaultPermissions,
      Collection<String> adminRoles) {
    return new AclBuilder(owner, defaultPermissions, adminRoles).build();
  }

  /**
   * Gets owner.
   *
   * @return the owner
   */
  String getOwner();

  /**
   * Returns the entries of this access control list. The key of the map is the permission. This map
   * is normally unmodifiable.
   *
   * @return the map
   */
  SortedMap<String, Ace> getPermissionMap();

  /**
   * Modifies the access control list. If the modification forbidden, an empty optional will be
   * returned, otherwise the modified access control list.
   *
   * @param mods the modifications
   * @param userContext the user context
   * @param accessEvaluation the access evaluation
   * @param permissions the permissions
   * @return the optional
   */
  default Optional<Acl> modify(
      AccessControlListModifications mods,
      AclUserContext userContext,
      AccessEvaluation accessEvaluation,
      Collection<String> permissions) {

    boolean hasPermission = AccessEvaluator.of(this)
        .hasPermissions(userContext, accessEvaluation, permissions);
    return hasPermission
        ? Optional.of(Acl.builder().from(this).apply(mods).build())
        : Optional.empty();
  }

  /**
   * The al builder.
   *
   * @author Christian Bremer
   */
  @ToString
  @EqualsAndHashCode
  class AclBuilder {

    private String owner;

    private final Map<String, Ace> permissionMap = new HashMap<>();

    /**
     * Instantiates a new acl builder.
     */
    public AclBuilder() {
      super();
    }

    private AclBuilder(
        String owner,
        Collection<String> defaultPermissions,
        Collection<String> adminRoles) {

      owner(owner);
      if (nonNull(defaultPermissions)) {
        defaultPermissions.forEach(permission -> addRoles(permission, adminRoles));
      }
    }

    /**
     * From acl.
     *
     * @param acl the acl
     * @return the acl builder
     */
    public AclBuilder from(Acl acl) {
      if (nonNull(acl)) {
        owner(acl.getOwner());
        permissionMap(acl.getPermissionMap());
      }
      return this;
    }

    /**
     * Owner.
     *
     * @param owner the owner
     * @return the acl builder
     */
    public AclBuilder owner(String owner) {
      this.owner = owner;
      return this;
    }

    /**
     * Permission map.
     *
     * @param permissionMap the permission map
     * @return the acl builder
     */
    public AclBuilder permissionMap(Map<String, ? extends Ace> permissionMap) {
      this.permissionMap.clear();
      if (nonNull(permissionMap)) {
        permissionMap.entrySet().stream()
            .filter(entry -> nonNull(entry.getKey()) && !entry.getKey().isBlank())
            .forEach(entry -> this.permissionMap.put(entry.getKey(), entry.getValue()));
        this.permissionMap.putAll(permissionMap);
      }
      return this;
    }

    private AclBuilder doWithAce(String permission, Function<Ace, Ace> aceFn) {
      if (nonNull(permission) && !permission.isBlank()) {
        Ace ace = this.permissionMap.getOrDefault(permission, Ace.empty());
        this.permissionMap.put(permission, aceFn.apply(ace));
      }
      return this;
    }

    /**
     * Add permissions.
     *
     * @param permissions the permissions
     * @return the acl builder
     */
    public AclBuilder addPermissions(Collection<String> permissions) {
      return Optional.ofNullable(permissions)
          .stream()
          .flatMap(Collection::stream)
          .map(permission -> doWithAce(permission, ace -> ace))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    /**
     * Remove permissions.
     *
     * @param permissions the permissions
     * @return the acl builder
     */
    public AclBuilder removePermissions(Collection<String> permissions) {
      if (nonNull(permissions)) {
        permissions.forEach(this.permissionMap::remove);
      }
      return this;
    }

    /**
     * Guest.
     *
     * @param guest the guest
     * @return the acl builder
     */
    public AclBuilder guest(boolean guest) {
      return guest(p -> true, guest);
    }

    /**
     * Guest.
     *
     * @param permissionFilter the permission filter
     * @param guest the guest
     * @return the acl builder
     */
    public AclBuilder guest(Predicate<String> permissionFilter, boolean guest) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> guest(permission, guest))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    /**
     * Guest.
     *
     * @param permission the permission
     * @param guest the guest
     * @return the acl builder
     */
    public AclBuilder guest(String permission, boolean guest) {
      return doWithAce(permission, ace -> Ace.builder().from(ace).guest(guest).build());
    }

    /**
     * Add users.
     *
     * @param users the users
     * @return the acl builder
     */
    public AclBuilder addUsers(Collection<String> users) {
      return addUsers(p -> true, users);
    }

    /**
     * Add users.
     *
     * @param permissionFilter the permission filter
     * @param users the users
     * @return the acl builder
     */
    public AclBuilder addUsers(Predicate<String> permissionFilter, Collection<String> users) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> addUsers(permission, users))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    /**
     * Add users.
     *
     * @param permission the permission
     * @param users the users
     * @return the acl builder
     */
    public AclBuilder addUsers(String permission, Collection<String> users) {
      if (nonNull(users)) {
        return doWithAce(permission, ace -> Ace.builder().from(ace).addUsers(users).build());
      }
      return this;
    }

    /**
     * Remove users.
     *
     * @param users the users
     * @return the acl builder
     */
    public AclBuilder removeUsers(Collection<String> users) {
      return removeUsers(p -> true, users);
    }

    /**
     * Remove users.
     *
     * @param permissionFilter the permission filter
     * @param users the users
     * @return the acl builder
     */
    public AclBuilder removeUsers(Predicate<String> permissionFilter, Collection<String> users) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> removeUsers(permission, users))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    /**
     * Remove users.
     *
     * @param permission the permission
     * @param users the users
     * @return the acl builder
     */
    public AclBuilder removeUsers(String permission, Collection<String> users) {
      if (nonNull(users)) {
        return doWithAce(permission, ace -> Ace.builder().from(ace).removeUsers(users).build());
      }
      return this;
    }

    /**
     * Add roles.
     *
     * @param roles the roles
     * @return the acl builder
     */
    public AclBuilder addRoles(Collection<String> roles) {
      return addRoles(p -> true, roles);
    }

    /**
     * Add roles.
     *
     * @param permissionFilter the permission filter
     * @param roles the roles
     * @return the acl builder
     */
    public AclBuilder addRoles(Predicate<String> permissionFilter, Collection<String> roles) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> addRoles(permission, roles))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    /**
     * Add roles.
     *
     * @param permission the permission
     * @param roles the roles
     * @return the acl builder
     */
    public AclBuilder addRoles(String permission, Collection<String> roles) {
      if (nonNull(roles)) {
        return doWithAce(permission, ace -> Ace.builder().from(ace).addRoles(roles).build());
      }
      return this;
    }

    /**
     * Remove roles.
     *
     * @param roles the roles
     * @return the acl builder
     */
    public AclBuilder removeRoles(Collection<String> roles) {
      return removeRoles(p -> true, roles);
    }

    /**
     * Remove roles.
     *
     * @param permissionFilter the permission filter
     * @param roles the roles
     * @return the acl builder
     */
    public AclBuilder removeRoles(Predicate<String> permissionFilter, Collection<String> roles) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> removeRoles(permission, roles))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    /**
     * Remove roles.
     *
     * @param permission the permission
     * @param roles the roles
     * @return the acl builder
     */
    public AclBuilder removeRoles(String permission, Collection<String> roles) {
      if (nonNull(roles)) {
        return doWithAce(permission, ace -> Ace.builder().from(ace).removeRoles(roles).build());
      }
      return this;
    }

    /**
     * Add groups.
     *
     * @param groups the groups
     * @return the acl builder
     */
    public AclBuilder addGroups(Collection<String> groups) {
      return addGroups(p -> true, groups);
    }

    /**
     * Add groups.
     *
     * @param permissionFilter the permission filter
     * @param groups the groups
     * @return the acl builder
     */
    public AclBuilder addGroups(Predicate<String> permissionFilter, Collection<String> groups) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> addGroups(permission, groups))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    /**
     * Add groups.
     *
     * @param permission the permission
     * @param groups the groups
     * @return the acl builder
     */
    public AclBuilder addGroups(String permission, Collection<String> groups) {
      if (nonNull(groups)) {
        return doWithAce(permission, ace -> Ace.builder().from(ace).addGroups(groups).build());
      }
      return this;
    }

    /**
     * Remove groups.
     *
     * @param groups the groups
     * @return the acl builder
     */
    public AclBuilder removeGroups(Collection<String> groups) {
      return removeGroups(p -> true, groups);
    }

    /**
     * Remove groups.
     *
     * @param permissionFilter the permission filter
     * @param groups the groups
     * @return the acl builder
     */
    public AclBuilder removeGroups(Predicate<String> permissionFilter, Collection<String> groups) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> removeGroups(permission, groups))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    /**
     * Remove groups.
     *
     * @param permission the permission
     * @param groups the groups
     * @return the acl builder
     */
    public AclBuilder removeGroups(String permission, Collection<String> groups) {
      if (nonNull(groups)) {
        return doWithAce(permission, ace -> Ace.builder().from(ace).removeGroups(groups).build());
      }
      return this;
    }

    /**
     * Apply modifications.
     *
     * @param modifications the modifications
     * @return the acl builder
     */
    public AclBuilder apply(AccessControlListModifications modifications) {
      if (nonNull(modifications)) {
        modifications.getModificationsDistinct().forEach(aceMods -> {
          guest(aceMods.getPermission(), aceMods.isGuest());
          addUsers(aceMods.getPermission(), aceMods.getAddUsers());
          removeUsers(aceMods.getPermission(), aceMods.getRemoveUsers());
          addRoles(aceMods.getPermission(), aceMods.getAddRoles());
          removeRoles(aceMods.getPermission(), aceMods.getRemoveRoles());
          addGroups(aceMods.getPermission(), aceMods.getAddGroups());
          removeGroups(aceMods.getPermission(), aceMods.getRemoveGroups());
        });
      }
      return this;
    }

    /**
     * Build acl.
     *
     * @return the acl
     */
    public Acl build() {
      return new AclImpl(owner, permissionMap);
    }
  }

  /**
   * The acl implementation.
   *
   * @author Christian Bremer
   */
  @SuppressWarnings("SameNameButDifferent")
  @Getter
  @ToString
  @EqualsAndHashCode
  class AclImpl implements Acl {

    private final String owner;

    private final SortedMap<String, Ace> permissionMap;

    private AclImpl(String owner, Map<String, Ace> permissionMap) {
      this.owner = nonNull(owner) && !owner.isBlank() ? owner : ANONYMOUS;
      this.permissionMap = unmodifiableSortedMap(permissionMap.entrySet().stream()
          .filter(entry -> nonNull(entry.getKey()) && !entry.getKey().isBlank())
          .collect(Collectors.toMap(
              Entry::getKey,
              Entry::getValue,
              (a, b) -> a,
              () -> new TreeMap<>(String::compareToIgnoreCase))));
    }
  }

}
