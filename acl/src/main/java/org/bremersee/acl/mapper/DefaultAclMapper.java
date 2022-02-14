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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.bremersee.acl.Ace;
import org.bremersee.acl.Acl;
import org.bremersee.acl.model.AccessControlEntry;
import org.bremersee.acl.model.AccessControlList;

/**
 * The default acl mapper.
 *
 * @author Christian Bremer
 */
public class DefaultAclMapper extends AbstractAclMapper<Acl> {

  /**
   * Instantiates a new default acl mapper.
   *
   * @param defaultPermissions the default permissions
   * @param hideAdminRoles the hide admin roles
   * @param adminRoles the admin roles
   */
  public DefaultAclMapper(
      String[] defaultPermissions,
      boolean hideAdminRoles,
      Set<String> adminRoles) {
    super(defaultPermissions, hideAdminRoles, adminRoles);
  }

  @Override
  public Acl map(AccessControlList accessControlList) {
    if (isNull(accessControlList)) {
      return Acl.builder().build();
    }
    return Acl.builder()
        .owner(accessControlList.getOwner())
        .permissionMap(accessControlList.getEntries().stream()
            .collect(Collectors.toMap(
                AccessControlEntry::getPermission,
                entry -> Ace.builder()
                    .guest(entry.isGuest())
                    .users(entry.getUsers())
                    .roles(entry.getRoles())
                    .groups(entry.getGroups())
                    .build()))
        )
        .addPermissions(defaultPermissions)
        .addRoles(hideAdminRoles ? adminRoles : List.of())
        .build();
  }

}
