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

package org.bremersee.acl.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The access control entry modifications test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class AccessControlEntryModificationsTest {

  /**
   * Builder defaults.
   *
   * @param softly the softly
   */
  @Test
  void builderDefaults(SoftAssertions softly) {
    AccessControlEntryModifications actual = AccessControlEntryModifications.builder()
        .permission("write")
        .build();
    softly.assertThat(actual.getPermission())
        .isEqualTo("write");
    softly.assertThat(actual.isGuest())
        .isFalse();
    softly.assertThat(actual.getAddUsers())
        .isEqualTo(Set.of());
    softly.assertThat(actual.getRemoveUsers())
        .isEqualTo(Set.of());
    softly.assertThat(actual.getAddRoles())
        .isEqualTo(Set.of());
    softly.assertThat(actual.getRemoveRoles())
        .isEqualTo(Set.of());
    softly.assertThat(actual.getAddGroups())
        .isEqualTo(Set.of());
    softly.assertThat(actual.getRemoveGroups())
        .isEqualTo(Set.of());
  }

  /**
   * From.
   */
  @Test
  void from() {
    AccessControlEntryModifications actual = AccessControlEntryModifications
        .from(AccessControlEntry.builder()
            .permission("read")
            .isGuest(true)
            .build())
        .build();
    AccessControlEntryModifications expected = AccessControlEntryModifications.builder()
        .permission("read")
        .isGuest(true)
        .build();
    assertThat(actual)
        .isEqualTo(expected);
  }

  /**
   * Compare to.
   *
   * @param softly the softly
   */
  @Test
  void compareTo(SoftAssertions softly) {
    AccessControlEntryModifications actual = AccessControlEntryModifications.builder()
        .permission("write")
        .build();
    softly.assertThat(actual.compareTo(AccessControlEntryModifications.builder()
            .permission("a")
            .build()))
        .isGreaterThan(0);
    softly.assertThat(actual.compareTo(AccessControlEntryModifications.builder()
            .permission("z")
            .build()))
        .isLessThan(0);
    softly.assertThat(actual.compareTo(AccessControlEntryModifications.builder()
            .permission("WRITE")
            .build()))
        .isEqualTo(0);
  }

}