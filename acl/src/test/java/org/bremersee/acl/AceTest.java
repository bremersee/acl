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

import java.util.List;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * The ace test.
 *
 * @author Christian Bremer
 */
@ExtendWith(SoftAssertionsExtension.class)
class AceTest {

  /**
   * Empty.
   *
   * @param softly the softly
   */
  @Test
  void empty(SoftAssertions softly) {
    Ace actual = Ace.empty();
    softly.assertThat(actual)
        .extracting(Ace::isGuest, InstanceOfAssertFactories.BOOLEAN)
        .isFalse();
    softly.assertThat(actual)
        .extracting(Ace::getUsers, InstanceOfAssertFactories.collection(String.class))
        .isEmpty();
    softly.assertThat(actual)
        .extracting(Ace::getRoles, InstanceOfAssertFactories.collection(String.class))
        .isEmpty();
    softly.assertThat(actual)
        .extracting(Ace::getGroups, InstanceOfAssertFactories.collection(String.class))
        .isEmpty();

    actual = Ace.builder().from(null).build();
    softly.assertThat(actual)
        .isEqualTo(Ace.empty());

    softly.assertThat(actual.toString())
        .contains("false");
  }

  /**
   * Is guest.
   *
   * @param softly the softly
   */
  @Test
  void isGuest(SoftAssertions softly) {
    Ace actual = Ace.builder().guest(true).build();
    softly.assertThat(actual.isGuest()).isTrue();
    softly.assertThat(Ace.builder().from(actual).build())
        .isEqualTo(actual);

    softly.assertThat(actual.toString())
        .contains("true");
  }

  /**
   * Gets users.
   *
   * @param softly the softly
   */
  @Test
  void getUsers(SoftAssertions softly) {
    Ace actual = Ace.builder()
        .users(List.of("5", "7", "9"))
        .addUsers(List.of("2", "8"))
        .removeUsers(List.of("7"))
        .build();
    softly.assertThat(actual.getUsers())
        .containsExactly("2", "5", "8", "9");
    softly.assertThat(Ace.builder().from(actual).build())
        .isEqualTo(actual);
    softly.assertThat(actual.toString())
        .contains("2");
  }

  /**
   * Gets roles.
   *
   * @param softly the softly
   */
  @Test
  void getRoles(SoftAssertions softly) {
    Ace actual = Ace.builder()
        .roles(List.of("5", "7", "9"))
        .addRoles(List.of("2", "8"))
        .removeRoles(List.of("7"))
        .build();
    softly.assertThat(actual.getRoles())
        .containsExactly("2", "5", "8", "9");
    softly.assertThat(Ace.builder().from(actual).build())
        .isEqualTo(actual);
    softly.assertThat(actual.toString())
        .contains("2");
  }

  /**
   * Gets groups.
   *
   * @param softly the softly
   */
  @Test
  void getGroups(SoftAssertions softly) {
    Ace actual = Ace.builder()
        .groups(List.of("5", "7", "9"))
        .addGroups(List.of("2", "8"))
        .removeGroups(List.of("7"))
        .build();
    softly.assertThat(actual.getGroups())
        .containsExactly("2", "5", "8", "9");
    softly.assertThat(Ace.builder().from(actual).build())
        .isEqualTo(actual);
    softly.assertThat(actual.toString())
        .contains("2");
  }
}