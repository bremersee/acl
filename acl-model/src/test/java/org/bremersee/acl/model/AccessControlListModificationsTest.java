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

import java.util.List;
import java.util.Set;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The access control list modifications test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class AccessControlListModificationsTest {

  /**
   * Builder defaults.
   *
   * @param softly the softly
   */
  @Test
  void builderDefaults(SoftAssertions softly) {
    AccessControlListModifications actual = AccessControlListModifications.builder().build();
    softly.assertThat(actual.getModifications())
        .isEmpty();
    softly.assertThat(actual.getModificationsDistinct())
        .isEmpty();
  }

  /**
   * Gets modifications distinct.
   */
  @Test
  void getModificationsDistinct() {
    AccessControlListModifications target = AccessControlListModifications.builder()
        .modifications(List.of(
            AccessControlEntryModifications.builder()
                .permission("write")
                .isGuest(true)
                .addUsers(Set.of("a"))
                .removeUsers(Set.of("1"))
                .addRoles(Set.of("a"))
                .removeRoles(Set.of("1"))
                .addGroups(Set.of("a"))
                .removeGroups(Set.of("1"))
                .build(),
            AccessControlEntryModifications.builder()
                .permission("write")
                .isGuest(true)
                .addUsers(Set.of("b"))
                .removeUsers(Set.of("2"))
                .addRoles(Set.of("b"))
                .removeRoles(Set.of("2"))
                .addGroups(Set.of("b"))
                .removeGroups(Set.of("2"))
                .build()
        ))
        .build();

    assertThat(target.getModificationsDistinct())
        .hasSize(1)
        .allMatch(actual -> actual.isGuest()
            && actual.getPermission().equals("write")
            && actual.getAddUsers().containsAll(List.of("a", "b"))
            && actual.getAddRoles().containsAll(List.of("a", "b"))
            && actual.getAddGroups().containsAll(List.of("a", "b"))
            && actual.getRemoveUsers().containsAll(List.of("1", "2"))
            && actual.getRemoveRoles().containsAll(List.of("1", "2"))
            && actual.getRemoveGroups().containsAll(List.of("1", "2"))
        );
  }
}