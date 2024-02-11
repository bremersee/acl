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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * The access control entry.
 *
 * @author Christian Bremer
 */
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

  /**
   * Builder ace builder.
   *
   * @return the ace builder
   */
  static AceBuilder builder() {
    return new AceBuilder();
  }

  /**
   * Empty ace.
   *
   * @return the ace
   */
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
  SortedSet<String> getUsers();

  /**
   * Gets roles.
   *
   * @return the roles
   */
  SortedSet<String> getRoles();

  /**
   * Gets groups.
   *
   * @return the groups
   */
  SortedSet<String> getGroups();

  /**
   * The ace builder.
   *
   * @author Christian Bremer
   */
  @ToString
  @EqualsAndHashCode
  class AceBuilder {

    private boolean guest;

    private final TreeSet<String> users = new TreeSet<>(String::compareToIgnoreCase);

    private final TreeSet<String> roles = new TreeSet<>(String::compareToIgnoreCase);

    private final TreeSet<String> groups = new TreeSet<>(String::compareToIgnoreCase);

    /**
     * From ace builder.
     *
     * @param ace the ace
     * @return the ace builder
     */
    public AceBuilder from(Ace ace) {
      if (nonNull(ace)) {
        guest(ace.isGuest());
        users(ace.getUsers());
        roles(ace.getRoles());
        groups(ace.getGroups());
      }
      return this;
    }

    /**
     * Guest ace builder.
     *
     * @param isGuest the is guest
     * @return the ace builder
     */
    public AceBuilder guest(boolean isGuest) {
      this.guest = isGuest;
      return this;
    }

    /**
     * Users ace builder.
     *
     * @param users the users
     * @return the ace builder
     */
    public AceBuilder users(Collection<String> users) {
      this.users.clear();
      return addUsers(nonNull(users) ? users : List.of());
    }

    /**
     * Add users ace builder.
     *
     * @param users the users
     * @return the ace builder
     */
    public AceBuilder addUsers(Collection<String> users) {
      if (nonNull(users)) {
        users.stream()
            .filter(entry -> nonNull(entry) && !entry.isBlank())
            .forEach(this.users::add);
      }
      return this;
    }

    /**
     * Remove users ace builder.
     *
     * @param users the users
     * @return the ace builder
     */
    public AceBuilder removeUsers(Collection<String> users) {
      if (nonNull(users)) {
        users.stream()
            .filter(Objects::nonNull)
            .forEach(this.users::remove);
      }
      return this;
    }

    /**
     * Roles ace builder.
     *
     * @param roles the roles
     * @return the ace builder
     */
    public AceBuilder roles(Collection<String> roles) {
      this.roles.clear();
      return addRoles(nonNull(roles) ? roles : List.of());
    }

    /**
     * Add roles ace builder.
     *
     * @param roles the roles
     * @return the ace builder
     */
    public AceBuilder addRoles(Collection<String> roles) {
      if (nonNull(roles)) {
        roles.stream()
            .filter(entry -> nonNull(entry) && !entry.isBlank())
            .forEach(this.roles::add);
      }
      return this;
    }

    /**
     * Remove roles ace builder.
     *
     * @param roles the roles
     * @return the ace builder
     */
    public AceBuilder removeRoles(Collection<String> roles) {
      if (nonNull(roles)) {
        roles.stream()
            .filter(Objects::nonNull)
            .forEach(this.roles::remove);
      }
      return this;
    }

    /**
     * Groups ace builder.
     *
     * @param groups the groups
     * @return the ace builder
     */
    public AceBuilder groups(Collection<String> groups) {
      this.groups.clear();
      return addGroups(nonNull(groups) ? groups : List.of());
    }

    /**
     * Add groups ace builder.
     *
     * @param groups the groups
     * @return the ace builder
     */
    public AceBuilder addGroups(Collection<String> groups) {
      if (nonNull(groups)) {
        groups.stream()
            .filter(entry -> nonNull(entry) && !entry.isBlank())
            .forEach(this.groups::add);
      }
      return this;
    }

    /**
     * Remove groups ace builder.
     *
     * @param groups the groups
     * @return the ace builder
     */
    public AceBuilder removeGroups(Collection<String> groups) {
      if (nonNull(groups)) {
        groups.stream()
            .filter(Objects::nonNull)
            .forEach(this.groups::remove);
      }
      return this;
    }

    /**
     * Build ace.
     *
     * @return the ace
     */
    public Ace build() {
      return new AceImpl(guest, users, roles, groups);
    }

  }

  /**
   * The ace implementation.
   *
   * @author Christian Bremer
   */
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
