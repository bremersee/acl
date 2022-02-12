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
import static org.bremersee.acl.UserContext.ANONYMOUS;

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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.bremersee.acl.model.AccessControlListModifications;

/**
 * The access control list.
 *
 * @author Christian Bremer
 */
@Valid
public interface Acl {

  String OWNER = "owner";

  String ENTRIES = "entries";

  static AclBuilder builder() {
    return new AclBuilder();
  }

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
  @NotNull
  String getOwner();

  /**
   * Returns the entries of this access control list. The key of the map is the permission. This map
   * is normally unmodifiable.
   *
   * @return the map
   */
  @NotNull
  SortedMap<String, Ace> getPermissionMap();

  default Optional<Acl> modify(
      @NotNull AccessControlListModifications mods,
      @NotNull UserContext userContext,
      @NotNull AccessEvaluation accessEvaluation,
      @NotNull Collection<String> permissions) {

    boolean hasPermission = AccessEvaluator.of(this)
        .hasPermissions(userContext, accessEvaluation, permissions);
    return hasPermission
        ? Optional.of(Acl.builder().from(this).apply(mods).build())
        : Optional.empty();
  }

  @SuppressWarnings("SameNameButDifferent")
  @ToString
  @EqualsAndHashCode
  class AclBuilder {

    private String owner;

    private final Map<String, Ace> permissionMap = new HashMap<>();

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

    public AclBuilder from(Acl acl) {
      if (nonNull(acl)) {
        owner(acl.getOwner());
        permissionMap(acl.getPermissionMap());
      }
      return this;
    }

    public AclBuilder owner(String owner) {
      this.owner = owner;
      return this;
    }

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

    public AclBuilder addPermissions(Collection<String> permissions) {
      return Optional.ofNullable(permissions)
          .stream()
          .flatMap(Collection::stream)
          .map(permission -> doWithAce(permission, ace -> ace))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    public AclBuilder removePermissions(Collection<String> permissions) {
      if (nonNull(permissions)) {
        permissions.forEach(this.permissionMap::remove);
      }
      return this;
    }

    public AclBuilder guest(boolean guest) {
      return guest(p -> true, guest);
    }

    public AclBuilder guest(Predicate<String> permissionFilter, boolean guest) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> guest(permission, guest))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    public AclBuilder guest(String permission, boolean guest) {
      return doWithAce(permission, ace -> Ace.builder().from(ace).guest(guest).build());
    }

    public AclBuilder addUsers(Collection<String> users) {
      return addUsers(p -> true, users);
    }

    public AclBuilder addUsers(Predicate<String> permissionFilter, Collection<String> users) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> addUsers(permission, users))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    public AclBuilder addUsers(String permission, Collection<String> users) {
      if (nonNull(users)) {
        return doWithAce(permission, ace -> Ace.builder().from(ace).addUsers(users).build());
      }
      return this;
    }

    public AclBuilder removeGUsers(Collection<String> users) {
      return removeUsers(p -> true, users);
    }

    public AclBuilder removeUsers(Predicate<String> permissionFilter, Collection<String> users) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> removeUsers(permission, users))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    public AclBuilder removeUsers(String permission, Collection<String> users) {
      if (nonNull(users)) {
        return doWithAce(permission, ace -> Ace.builder().from(ace).removeUsers(users).build());
      }
      return this;
    }

    public AclBuilder addRoles(Collection<String> roles) {
      return addRoles(p -> true, roles);
    }

    public AclBuilder addRoles(Predicate<String> permissionFilter, Collection<String> roles) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> addRoles(permission, roles))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    public AclBuilder addRoles(String permission, Collection<String> roles) {
      if (nonNull(roles)) {
        return doWithAce(permission, ace -> Ace.builder().from(ace).addRoles(roles).build());
      }
      return this;
    }

    public AclBuilder removeRoles(Collection<String> roles) {
      return removeRoles(p -> true, roles);
    }

    public AclBuilder removeRoles(Predicate<String> permissionFilter, Collection<String> roles) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> removeRoles(permission, roles))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    public AclBuilder removeRoles(String permission, Collection<String> roles) {
      if (nonNull(roles)) {
        return doWithAce(permission, ace -> Ace.builder().from(ace).removeRoles(roles).build());
      }
      return this;
    }

    public AclBuilder addGroups(Collection<String> groups) {
      return addGroups(p -> true, groups);
    }

    public AclBuilder addGroups(Predicate<String> permissionFilter, Collection<String> groups) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> addGroups(permission, groups))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    public AclBuilder addGroups(String permission, Collection<String> groups) {
      if (nonNull(groups)) {
        return doWithAce(permission, ace -> Ace.builder().from(ace).addGroups(groups).build());
      }
      return this;
    }

    public AclBuilder removeGroups(Collection<String> groups) {
      return removeGroups(p -> true, groups);
    }

    public AclBuilder removeGroups(Predicate<String> permissionFilter, Collection<String> groups) {
      Predicate<String> filter = nonNull(permissionFilter) ? permissionFilter : p -> true;
      return permissionMap.keySet().stream()
          .filter(filter)
          .map(permission -> removeGroups(permission, groups))
          .reduce((first, second) -> second)
          .orElse(this);
    }

    public AclBuilder removeGroups(String permission, Collection<String> groups) {
      if (nonNull(groups)) {
        return doWithAce(permission, ace -> Ace.builder().from(ace).removeGroups(groups).build());
      }
      return this;
    }

    public AclBuilder apply(AccessControlListModifications modifications) {
      if (nonNull(modifications)) {
        modifications.getModifications().forEach(aceMods -> {
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

    public Acl build() {
      return new AclImpl(owner, permissionMap);
    }
  }

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
