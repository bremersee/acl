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

package org.bremersee.acl.spring.data.mongo.convert;

import java.util.List;
import org.springframework.core.convert.converter.Converter;

/**
 * The type Acl converters.
 *
 * @author Christian Bremer
 */
public abstract class AclConverters {

  private AclConverters() {
  }

  /**
   * Gets converters to register.
   *
   * @return the converters to register
   */
  public static List<Converter<?, ?>> getConvertersToRegister() {
    return List.of(
        new AceToDocumentConverter(),
        new AclToDocumentConverter(),
        new DocumentToAceConverter(),
        new DocumentToAclConverter()
    );
  }

}
