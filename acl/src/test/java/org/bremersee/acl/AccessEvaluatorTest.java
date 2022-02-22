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
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The access evaluator test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class AccessEvaluatorTest {

  /**
   * Has permission with acl null.
   */
  @Test
  void hasPermissionWithAclNull() {
    assertThat(
        AccessEvaluator
            .of(null)
            .hasPermission(AclUserContext.builder().build(), ""))
        .isFalse();
  }

  /**
   * Has permission with permission null.
   */
  @Test
  void hasPermissionWithPermissionNull() {
    assertThat(
        AccessEvaluator
            .of(Acl.builder().build())
            .hasPermission(AclUserContext.builder().build(), null))
        .isFalse();
  }

  /**
   * Has permission with user is owner.
   */
  @Test
  void hasPermissionWithUserIsOwner() {
    assertThat(
        AccessEvaluator
            .of(Acl.builder().owner("junit").build())
            .hasPermission(AclUserContext.builder().name("junit").build(), ""))
        .isTrue();
  }

  /**
   * Has permission with guest.
   */
  @Test
  void hasPermissionWithGuest() {
    assertThat(
        AccessEvaluator
            .of(Acl.builder().owner("anna").addPermissions(List.of("read")).guest(true).build())
            .hasPermission(AclUserContext.builder().name("junit").build(), "read"))
        .isTrue();
  }

  /**
   * Has permission with user.
   */
  @Test
  void hasPermissionWithUser() {
    assertThat(
        AccessEvaluator
            .of(Acl.builder().owner("anna").addUsers("read", List.of("junit")).build())
            .hasPermission(AclUserContext.builder().name("junit").build(), "read"))
        .isTrue();
  }

  /**
   * Has permission with role.
   */
  @Test
  void hasPermissionWithRole() {
    assertThat(
        AccessEvaluator
            .of(Acl.builder().owner("anna").addRoles("read", List.of("aRole")).build())
            .hasPermission(
                AclUserContext.builder().name("junit").roles(List.of("aRole")).build(),
                "read"))
        .isTrue();
  }

  /**
   * Has permission with group.
   */
  @Test
  void hasPermissionWithGroup() {
    assertThat(
        AccessEvaluator
            .of(Acl.builder().owner("anna").addGroups("read", List.of("aGroup")).build())
            .hasPermission(
                AclUserContext.builder().name("junit").groups(List.of("aGroup")).build(),
                "read"))
        .isTrue();
  }

  /**
   * Has permissions with any.
   */
  @Test
  void hasPermissionsWithAny() {
    assertThat(
        AccessEvaluator
            .of(Acl.builder().owner("anna").addGroups("read", List.of("aGroup")).build())
            .hasPermissions(
                AclUserContext.builder().name("junit").groups(List.of("aGroup")).build(),
                AccessEvaluation.ANY_PERMISSION,
                List.of("delete", "write", "read")))
        .isTrue();
  }

  /**
   * Has permissions with all.
   */
  @Test
  void hasPermissionsWithAll() {
    assertThat(
        AccessEvaluator
            .of(Acl.builder()
                .owner("anna")
                .addGroups("read", List.of("aGroup"))
                .addGroups("write", List.of("aGroup"))
                .build())
            .hasPermissions(
                AclUserContext.builder().name("junit").groups(List.of("aGroup")).build(),
                AccessEvaluation.ALL_PERMISSIONS,
                List.of("write", "read")))
        .isTrue();
  }

}