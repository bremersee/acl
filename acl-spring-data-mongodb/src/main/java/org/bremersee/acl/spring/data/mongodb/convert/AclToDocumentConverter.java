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

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bremersee.acl.Acl;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

/**
 * The acl to document converter.
 *
 * @author Christian Bremer
 */
@WritingConverter
public class AclToDocumentConverter implements Converter<Acl, Document> {

  private final AceToDocumentConverter aceConverter = new AceToDocumentConverter();

  @Override
  public Document convert(@NonNull Acl source) {
    String owner = source.getOwner();
    Map<String, Document> entries = source.getPermissionMap().entrySet().stream()
        .collect(Collectors.toMap(
            Entry::getKey,
            entry -> requireNonNull(aceConverter.convert(entry.getValue())),
            (a, b) -> a,
            LinkedHashMap::new));
    Document target = new Document();
    target.put(Acl.OWNER, owner);
    if (!entries.isEmpty()) {
      target.put(Acl.ENTRIES, entries);
    }
    return target;
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
