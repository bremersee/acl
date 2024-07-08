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

package org.bremersee.acl.mapper;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.bremersee.acl.Acl;
import org.bremersee.acl.model.AccessControlEntry;
import org.bremersee.acl.model.AccessControlList;

/**
 * The abstract acl mapper.
 *
 * @param <T> the type parameter
 * @author Christian Bremer
 */
public abstract class AbstractAclMapper<T extends Acl> implements AclMapper<T> {

  /**
   * The default permissions.
   */
  protected final Set<String> defaultPermissions;

  /**
   * The hide admin roles.
   */
  protected final boolean hideAdminRoles;

  /**
   * The admin roles.
   */
  protected Set<String> adminRoles;

  /**
   * Instantiates a new abstract acl mapper.
   *
   * @param defaultPermissions the default permissions
   * @param hideAdminRoles the hide admin roles
   * @param adminRoles the admin roles
   */
  public AbstractAclMapper(
      String[] defaultPermissions,
      boolean hideAdminRoles,
      Set<String> adminRoles) {
    this.defaultPermissions = isNull(defaultPermissions)
        ? Set.of()
        : Set.of(defaultPermissions);
    this.hideAdminRoles = hideAdminRoles;
    this.adminRoles = nonNull(adminRoles) ? Set.copyOf(adminRoles) : Set.of();
  }

  @Override
  public AccessControlList map(T acl) {

    Acl source = isNull(acl) ? Acl.builder().build() : acl;
    source = Acl.builder()
        .from(source)
        .addPermissions(defaultPermissions)
        .removeRoles(hideAdminRoles ? adminRoles : List.of())
        .build();
    return AccessControlList.builder()
        .owner(source.getOwner())
        .entries(source.getPermissionMap().entrySet().stream()
            .map(entry -> AccessControlEntry.builder()
                .permission(entry.getKey())
                .isGuest(entry.getValue().isGuest())
                .groups(entry.getValue().getGroups())
                .users(entry.getValue().getUsers())
                .roles(entry.getValue().getRoles())
                .build())
            .collect(Collectors.toCollection(TreeSet::new)))
        .build();
  }

}
