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

import static java.util.Collections.unmodifiableSortedSet;
import static java.util.Objects.nonNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * The access control entry.
 *
 * @author Christian Bremer
 */
@Valid
public interface Ace {

  /**
   * The constant GUEST.
   */
  String GUEST = "guest";

  /**
   * The constant USERS.
   */
  String USERS = "users";

  /**
   * The constant ROLES.
   */
  String ROLES = "roles";

  /**
   * The constant GROUPS.
   */
  String GROUPS = "groups";

  @NotNull
  static AceBuilder builder() {
    return new AceBuilder();
  }

  @NotNull
  static Ace empty() {
    return builder().build();
  }

  /**
   * Determines whether guests have access.
   *
   * @return {@code true} if guests have access, otherwise {@code false}
   */
  boolean isGuest();

  /**
   * Gets users.
   *
   * @return the users
   */
  @NotNull
  SortedSet<String> getUsers();

  /**
   * Gets roles.
   *
   * @return the roles
   */
  @NotNull
  SortedSet<String> getRoles();

  /**
   * Gets groups.
   *
   * @return the groups
   */
  @NotNull
  SortedSet<String> getGroups();

  @SuppressWarnings("SameNameButDifferent")
  @ToString
  @EqualsAndHashCode
  class AceBuilder {

    private boolean guest;

    private final TreeSet<String> users = new TreeSet<>(String::compareToIgnoreCase);

    private final TreeSet<String> roles = new TreeSet<>(String::compareToIgnoreCase);

    private final TreeSet<String> groups = new TreeSet<>(String::compareToIgnoreCase);

    public AceBuilder from(Ace ace) {
      if (nonNull(ace)) {
        guest(ace.isGuest());
        users(ace.getUsers());
        roles(ace.getRoles());
        groups(ace.getGroups());
      }
      return this;
    }

    public AceBuilder guest(boolean isGuest) {
      this.guest = isGuest;
      return this;
    }

    public AceBuilder users(Collection<String> users) {
      this.users.clear();
      return addUsers(nonNull(users) ? users : List.of());
    }

    public AceBuilder addUsers(Collection<String> users) {
      if (nonNull(users)) {
        users.stream()
            .filter(entry -> nonNull(entry) && !entry.isBlank())
            .forEach(this.users::add);
      }
      return this;
    }

    public AceBuilder removeUsers(Collection<String> users) {
      if (nonNull(users)) {
        users.stream()
            .filter(Objects::nonNull)
            .forEach(this.users::remove);
      }
      return this;
    }

    public AceBuilder roles(Collection<String> roles) {
      this.roles.clear();
      return addRoles(nonNull(roles) ? roles : List.of());
    }

    public AceBuilder addRoles(Collection<String> roles) {
      if (nonNull(roles)) {
        roles.stream()
            .filter(entry -> nonNull(entry) && !entry.isBlank())
            .forEach(this.roles::add);
      }
      return this;
    }

    public AceBuilder removeRoles(Collection<String> roles) {
      if (nonNull(roles)) {
        roles.stream()
            .filter(Objects::nonNull)
            .forEach(this.roles::remove);
      }
      return this;
    }

    public AceBuilder groups(Collection<String> groups) {
      this.groups.clear();
      return addGroups(nonNull(groups) ? groups : List.of());
    }

    public AceBuilder addGroups(Collection<String> groups) {
      if (nonNull(groups)) {
        groups.stream()
            .filter(entry -> nonNull(entry) && !entry.isBlank())
            .forEach(this.groups::add);
      }
      return this;
    }

    public AceBuilder removeGroups(Collection<String> groups) {
      if (nonNull(groups)) {
        groups.stream()
            .filter(Objects::nonNull)
            .forEach(this.groups::remove);
      }
      return this;
    }

    public Ace build() {
      return new AceImpl(guest, users, roles, groups);
    }

  }

  @SuppressWarnings("SameNameButDifferent")
  @Getter
  @ToString
  @EqualsAndHashCode
  class AceImpl implements Ace {

    private final boolean guest;

    private final SortedSet<String> users;

    private final SortedSet<String> roles;

    private final SortedSet<String> groups;

    private AceImpl(
        boolean guest,
        SortedSet<String> users,
        SortedSet<String> roles,
        SortedSet<String> groups) {

      this.guest = guest;
      this.users = unmodifiableSortedSet(users);
      this.roles = unmodifiableSortedSet(roles);
      this.groups = unmodifiableSortedSet(groups);
    }
  }

}
