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

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The access control entry test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class AccessControlEntryTest {

  /**
   * Builder defaults.
   *
   * @param softly the softly
   */
  @Test
  void builderDefaults(SoftAssertions softly) {
    AccessControlEntry actual = AccessControlEntry.builder()
        .permission("write")
        .build();
    softly.assertThat(actual.getPermission())
        .isEqualTo("write");
    softly.assertThat(actual.isGuest())
        .isFalse();
    softly.assertThat(actual.getUsers())
        .isEqualTo(List.of());
    softly.assertThat(actual.getRoles())
        .isEqualTo(List.of());
    softly.assertThat(actual.getGroups())
        .isEqualTo(List.of());
  }

  /**
   * Compare to.
   *
   * @param softly the softly
   */
  @Test
  void compareTo(SoftAssertions softly) {
    AccessControlEntry actual = AccessControlEntry.builder()
        .permission("write")
        .build();
    softly.assertThat(actual.compareTo(AccessControlEntry.builder().permission("a").build()))
        .isGreaterThan(0);
    softly.assertThat(actual.compareTo(AccessControlEntry.builder().permission("z").build()))
        .isLessThan(0);
    softly.assertThat(actual.compareTo(AccessControlEntry.builder().permission("WRITE").build()))
        .isEqualTo(0);
  }
}