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

package org.bremersee.acl.spring.data.mongodb.convert;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bremersee.acl.Ace;
import org.bremersee.acl.Acl;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;

/**
 * The document to acl converter.
 *
 * @author Christian Bremer
 */
@ReadingConverter
public class DocumentToAclConverter implements Converter<Document, Acl> {

  private final DocumentToAceConverter aceConverter = new DocumentToAceConverter();

  @Override
  public Acl convert(@NonNull Document source) {
    String owner = source.getString(Acl.OWNER);
    Object entries = source.get(Acl.ENTRIES);
    Map<String, Ace> permissionMap = new HashMap<>();
    if (entries instanceof Map) {
      //noinspection unchecked
      Map<String, Object> entryMap = (Map<String, Object>) entries;
      for (Map.Entry<String, Object> entry : entryMap.entrySet()) {
        String permission = entry.getKey();
        Object aceObj = entry.getValue();
        if (aceObj instanceof Ace) {
          permissionMap.put(permission, (Ace) aceObj);
        } else if (aceObj instanceof Map) {
          //noinspection unchecked
          Map<String, Object> aceMap = (Map<String, Object>) aceObj;
          Ace ace = aceConverter.convert(new Document(aceMap));
          permissionMap.put(permission, ace);
        }
      }
    }
    return Acl.builder()
        .owner(owner)
        .permissionMap(permissionMap)
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o != null && getClass() == o.getClass();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getClass());
  }

}
