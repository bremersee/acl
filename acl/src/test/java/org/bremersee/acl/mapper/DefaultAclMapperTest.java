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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import org.bremersee.acl.Acl;
import org.bremersee.acl.model.AccessControlEntry;
import org.bremersee.acl.model.AccessControlList;
import org.junit.jupiter.api.Test;

/**
 * The default acl mapper test.
 *
 * @author Christian Bremer
 */
class DefaultAclMapperTest {

  /**
   * Map to acl with null.
   */
  @Test
  void mapToAclWithNull() {
    DefaultAclMapper target = new DefaultAclMapper(null, false, null);

    assertThat(target.map((AccessControlList) null))
        .isEqualTo(Acl.builder().build());
  }

  /**
   * Map to access control list with null.
   */
  @Test
  void mapToAccessControlListWithNull() {
    DefaultAclMapper target = new DefaultAclMapper(null, false, null);

    assertThat(target.map((Acl) null))
        .isNotNull()
        .extracting(Object::toString)
        .isEqualTo(AccessControlList.builder().build().toString());
  }

  /**
   * Map to acl with no changes.
   */
  @Test
  void mapToAclWithNoChanges() {
    DefaultAclMapper target = new DefaultAclMapper(new String[0], false, Set.of());

    AccessControlList source = AccessControlList.builder()
        .owner("junit")
        .entries(List.of(
            AccessControlEntry.builder()
                .permission("read")
                .isGuest(true)
                .build(),
            AccessControlEntry.builder()
                .permission("write")
                .isGuest(false)
                .users(List.of("u1", "u2"))
                .roles(List.of("r1", "r2"))
                .groups(List.of("g1", "g2"))
                .build()
        ))
        .build();

    Acl expected = Acl.builder()
        .owner("junit")
        .guest("read", true)
        .addUsers("write", List.of("u1", "u2"))
        .addRoles("write", List.of("r1", "r2"))
        .addGroups("write", List.of("g1", "g2"))
        .build();

    Acl actual = target.map(source);

    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Map to access control list with no changes.
   */
  @Test
  void mapToAccessControlListWithNoChanges() {
    DefaultAclMapper target = new DefaultAclMapper(new String[0], false, Set.of());

    Acl source = Acl.builder()
        .owner("junit")
        .guest("read", true)
        .addUsers("write", List.of("u1", "u2"))
        .addRoles("write", List.of("r1", "r2"))
        .addGroups("write", List.of("g1", "g2"))
        .build();

    AccessControlList expected = AccessControlList.builder()
        .owner("junit")
        .entries(List.of(
            AccessControlEntry.builder()
                .permission("read")
                .isGuest(true)
                .build(),
            AccessControlEntry.builder()
                .permission("write")
                .isGuest(false)
                .users(List.of("u1", "u2"))
                .roles(List.of("r1", "r2"))
                .groups(List.of("g1", "g2"))
                .build()
        ))
        .build();

    AccessControlList actual = target.map(source);

    assertThat(actual.toString())
        .isEqualTo(expected.toString());
  }

  /**
   * Map to acl with changes.
   */
  @Test
  void mapToAclWithChanges() {
    DefaultAclMapper target = new DefaultAclMapper(
        new String[]{"read", "write", "delete"},
        true,
        Set.of("ROLE_ADMIN"));

    AccessControlList source = AccessControlList.builder()
        .owner("junit")
        .entries(List.of(
            AccessControlEntry.builder()
                .permission("read")
                .isGuest(true)
                .build(),
            AccessControlEntry.builder()
                .permission("write")
                .isGuest(false)
                .users(List.of("u1", "u2"))
                .roles(List.of("r1", "r2"))
                .groups(List.of("g1", "g2"))
                .build()
        ))
        .build();

    Acl expected = Acl.builder()
        .owner("junit")
        .guest("read", true)
        .addRoles("read", List.of("ROLE_ADMIN"))
        .addUsers("write", List.of("u1", "u2"))
        .addRoles("write", List.of("r1", "r2", "ROLE_ADMIN"))
        .addGroups("write", List.of("g1", "g2"))
        .addRoles("delete", List.of("ROLE_ADMIN"))
        .build();

    Acl actual = target.map(source);

    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Map to access control list with changes.
   */
  @Test
  void mapToAccessControlListWithChanges() {
    DefaultAclMapper target = new DefaultAclMapper(
        new String[]{"read", "write", "delete"},
        true,
        Set.of("ROLE_ADMIN"));

    Acl source = Acl.builder()
        .owner("junit")
        .guest("read", true)
        .addRoles("read", List.of("ROLE_ADMIN"))
        .addUsers("write", List.of("u1", "u2"))
        .addRoles("write", List.of("r1", "r2", "ROLE_ADMIN"))
        .addGroups("write", List.of("g1", "g2"))
        .build();

    AccessControlList expected = AccessControlList.builder()
        .owner("junit")
        .entries(List.of(
            AccessControlEntry.builder()
                .permission("delete")
                .isGuest(false)
                .build(),
            AccessControlEntry.builder()
                .permission("read")
                .isGuest(true)
                .build(),
            AccessControlEntry.builder()
                .permission("write")
                .isGuest(false)
                .users(List.of("u1", "u2"))
                .roles(List.of("r1", "r2"))
                .groups(List.of("g1", "g2"))
                .build()
        ))
        .build();

    AccessControlList actual = target.map(source);

    assertThat(actual.toString())
        .isEqualTo(expected.toString());
  }

}