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
import java.util.Optional;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.acl.model.AccessControlEntryModifications;
import org.bremersee.acl.model.AccessControlListModifications;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The acl test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class AclTest {

  /**
   * With.
   *
   * @param softly the softly
   */
  @Test
  void with(SoftAssertions softly) {
    Acl actual = Acl.with("junit", PermissionConstants.getAll(), List.of("ROLE_ADMIN"));
    softly.assertThat(actual.getOwner())
        .isEqualTo("junit");
    for (String permission : PermissionConstants.getAll()) {
      softly.assertThat(actual.getPermissionMap().get(permission))
          .isNotNull()
          .extracting(Ace::getRoles, InstanceOfAssertFactories.collection(String.class))
          .containsExactly("ROLE_ADMIN");
    }
  }

  /**
   * Gets owner.
   *
   * @param softly the softly
   */
  @Test
  void getOwner(SoftAssertions softly) {
    Acl actual = Acl.builder().owner("junit_1").build();
    softly.assertThat(actual.getOwner())
        .isEqualTo("junit_1");
    softly.assertThat(Acl.builder().from(actual).build())
        .isEqualTo(actual);
    softly.assertThat(actual.toString())
        .contains("junit_1");
  }


  /**
   * Modify with owner.
   */
  @Test
  void modifyWithOwner() {
    Acl target = Acl.builder().owner("anna").build();
    Optional<Acl> actual = target.modify(
        AccessControlListModifications.builder()
            .modifications(List.of(
                AccessControlEntryModifications.builder()
                    .permission("read")
                    .addUsers(List.of("james"))
                    .build()
            ))
            .build(),
        AclUserContext.builder()
            .name("anna")
            .build(),
        AccessEvaluation.ALL_PERMISSIONS,
        List.of(PermissionConstants.ADMINISTRATION));
    assertThat(actual)
        .hasValue(Acl.builder()
            .owner("anna")
            .addUsers("read", List.of("james"))
            .build());
  }

  /**
   * Modify with permission.
   */
  @Test
  void modifyWithPermission() {
    Acl target = Acl.builder().owner("anna")
        .addUsers(PermissionConstants.ADMINISTRATION, List.of("james"))
        .build();
    Optional<Acl> actual = target.modify(
        AccessControlListModifications.builder()
            .modifications(List.of(
                AccessControlEntryModifications.builder()
                    .permission("read")
                    .isGuest(true)
                    .addUsers(List.of("james"))
                    .build(),
                AccessControlEntryModifications.builder()
                    .permission("read")
                    .isGuest(true)
                    .addUsers(List.of("anna"))
                    .build()
            ))
            .build(),
        AclUserContext.builder()
            .name("james")
            .build(),
        AccessEvaluation.ANY_PERMISSION,
        List.of(PermissionConstants.ADMINISTRATION));
    assertThat(actual)
        .hasValue(Acl.builder()
            .from(target)
            .guest("read", true)
            .addUsers("read", List.of("anna", "james"))
            .build());
  }

  /**
   * Modify with no permission.
   */
  @Test
  void modifyWithNoPermission() {
    Acl target = Acl.builder().owner("anna")
        .addUsers("write", List.of("james"))
        .build();
    Optional<Acl> actual = target.modify(
        AccessControlListModifications.builder().build(),
        AclUserContext.builder().build(),
        AccessEvaluation.ANY_PERMISSION,
        List.of(PermissionConstants.ADMINISTRATION));
    assertThat(actual)
        .isEmpty();
  }

}