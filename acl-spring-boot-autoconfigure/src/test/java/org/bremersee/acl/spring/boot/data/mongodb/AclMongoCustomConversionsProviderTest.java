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

package org.bremersee.acl.spring.boot.data.mongodb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.bremersee.acl.spring.data.mongodb.convert.AclConverters;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;

/**
 * The acl mongo custom conversions provider test.
 *
 * @author Christian Bremer
 */
class AclMongoCustomConversionsProviderTest {

  /**
   * Init.
   */
  @Test
  void init() {
    AclMongoCustomConversionsProvider target = new AclMongoCustomConversionsProvider();
    target.init();
  }

  /**
   * Gets custom conversions.
   */
  @Test
  void getCustomConversions() {
    AclMongoCustomConversionsProvider target = new AclMongoCustomConversionsProvider();
    List<Converter<?, ?>> actual = target.getCustomConversions();
    assertThat(actual)
        .isNotNull()
        .hasSize(AclConverters.getConvertersToRegister().size());
  }
}