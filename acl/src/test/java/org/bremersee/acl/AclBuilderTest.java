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

package org.bremersee.acl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.acl.Acl.AclBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The acl builder test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class AclBuilderTest {

  /**
   * From.
   *
   * @param softly the softly
   */
  @Test
  void from(SoftAssertions softly) {
    AclBuilder actual = new AclBuilder()
        .from(Acl.builder().owner("junit").build());
    AclBuilder expected = new AclBuilder()
        .owner("junit");
    softly.assertThat(actual)
        .isEqualTo(expected);
    softly.assertThat(actual.hashCode())
        .isEqualTo(expected.hashCode());
    softly.assertThat(actual.toString()).contains("junit");
  }

  /**
   * Owner.
   */
  @Test
  void owner() {
    assertThat(new AclBuilder().owner("junit").build())
        .extracting(Acl::getOwner)
        .isEqualTo("junit");
  }

  /**
   * Permission map.
   */
  @Test
  void permissionMap() {
    assertThat(new AclBuilder().permissionMap(Map.of("test", Ace.empty())).build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of("test", Ace.empty()));
  }

  /**
   * Add permissions and remove permissions.
   *
   * @param softly the softly
   */
  @Test
  void addPermissionsAndRemovePermissions(SoftAssertions softly) {
    AclBuilder actual = new AclBuilder()
        .addPermissions(List.of("a", "b"));
    softly.assertThat(actual.build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of("a", Ace.empty(), "b", Ace.empty()));

    actual = actual.removePermissions(List.of("a", "b"));
    softly.assertThat(actual.build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of());
  }

  /**
   * Guest.
   */
  @Test
  void guest() {
    assertThat(new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .guest(true)
        .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().guest(true).build(),
            "b", Ace.builder().guest(true).build()));
  }

  /**
   * Guest for permission.
   */
  @Test
  void guestForPermission() {
    assertThat(new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .guest("b", true)
        .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().guest(false).build(),
            "b", Ace.builder().guest(true).build()));
  }

  /**
   * Add users.
   */
  @Test
  void addUsers() {
    assertThat(new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .addUsers(List.of("junit"))
        .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().users(List.of("junit")).build(),
            "b", Ace.builder().users(List.of("junit")).build()));
  }

  /**
   * Add users with permission.
   *
   * @param softly the softly
   */
  @Test
  void addUsersWithPermission(SoftAssertions softly) {
    AclBuilder aclBuilder = new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .addUsers("b", List.of("junit"));

    softly.assertThat(aclBuilder
            .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().build(),
            "b", Ace.builder().users(List.of("junit")).build()));

    softly.assertThat(aclBuilder.addUsers("b", null))
        .isEqualTo(aclBuilder);
  }

  /**
   * Remove users.
   */
  @Test
  void removeUsers() {
    assertThat(new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .addUsers(List.of("junit"))
        .removeUsers(List.of("junit"))
        .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().build(),
            "b", Ace.builder().build()));
  }

  /**
   * Remove users with permission.
   *
   * @param softly the softly
   */
  @Test
  void removeUsersWithPermission(SoftAssertions softly) {
    AclBuilder aclBuilder = new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .addUsers(List.of("junit"));

    softly.assertThat(aclBuilder
            .removeUsers("a", List.of("junit"))
            .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().build(),
            "b", Ace.builder().users(List.of("junit")).build()));

    softly.assertThat(aclBuilder.removeUsers("a", null))
        .isEqualTo(aclBuilder);
  }

  /**
   * Add roles.
   */
  @Test
  void addRoles() {
    assertThat(new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .addRoles(List.of("junit"))
        .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().roles(List.of("junit")).build(),
            "b", Ace.builder().roles(List.of("junit")).build()));
  }

  /**
   * Add roles with permission.
   *
   * @param softly the softly
   */
  @Test
  void addRolesWithPermission(SoftAssertions softly) {
    AclBuilder aclBuilder = new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .addRoles("b", List.of("junit"));

    softly.assertThat(aclBuilder
            .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().build(),
            "b", Ace.builder().roles(List.of("junit")).build()));

    softly.assertThat(aclBuilder.addRoles("b", null))
        .isEqualTo(aclBuilder);
  }

  /**
   * Remove roles.
   */
  @Test
  void removeRoles() {
    assertThat(new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .addRoles(List.of("junit"))
        .removeRoles(List.of("junit"))
        .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().build(),
            "b", Ace.builder().build()));
  }

  /**
   * Remove roles with permission.
   *
   * @param softly the softly
   */
  @Test
  void removeRolesWithPermission(SoftAssertions softly) {
    AclBuilder aclBuilder = new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .addRoles(List.of("junit"));

    softly.assertThat(aclBuilder
            .removeRoles("a", List.of("junit"))
            .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().build(),
            "b", Ace.builder().roles(List.of("junit")).build()));

    softly.assertThat(aclBuilder.removeRoles("a", null))
        .isEqualTo(aclBuilder);
  }

  /**
   * Add groups.
   */
  @Test
  void addGroups() {
    assertThat(new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .addGroups(List.of("junit"))
        .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().groups(List.of("junit")).build(),
            "b", Ace.builder().groups(List.of("junit")).build()));
  }

  /**
   * Add groups with permission.
   *
   * @param softly the softly
   */
  @Test
  void addGroupsWithPermission(SoftAssertions softly) {
    AclBuilder aclBuilder = new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .addGroups("b", List.of("junit"));

    softly.assertThat(aclBuilder
            .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().build(),
            "b", Ace.builder().groups(List.of("junit")).build()));

    softly.assertThat(aclBuilder.addGroups("b", null))
        .isEqualTo(aclBuilder);
  }

  /**
   * Remove groups.
   */
  @Test
  void removeGroups() {
    assertThat(new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .addGroups(List.of("junit"))
        .removeGroups(List.of("junit"))
        .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().build(),
            "b", Ace.builder().build()));
  }

  /**
   * Remove groups with permission.
   *
   * @param softly the softly
   */
  @Test
  void removeGroupsWithPermission(SoftAssertions softly) {
    AclBuilder aclBuilder = new AclBuilder()
        .addPermissions(List.of("a", "b"))
        .addGroups(List.of("junit"));

    softly.assertThat(aclBuilder
            .removeGroups("a", List.of("junit"))
            .build())
        .extracting(Acl::getPermissionMap)
        .isEqualTo(Map.of(
            "a", Ace.builder().build(),
            "b", Ace.builder().groups(List.of("junit")).build()));

    softly.assertThat(aclBuilder.removeGroups("a", null))
        .isEqualTo(aclBuilder);
  }

}