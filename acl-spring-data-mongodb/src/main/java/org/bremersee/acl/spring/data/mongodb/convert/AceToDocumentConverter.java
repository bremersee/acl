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

import java.util.Objects;
import org.bremersee.acl.Ace;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.lang.NonNull;

/**
 * The ace to document converter.
 *
 * @author Christian Bremer
 */
@WritingConverter
public class AceToDocumentConverter implements Converter<Ace, Document> {

  @Override
  public Document convert(@NonNull Ace source) {
    Document target = new Document();
    target.put(Ace.GUEST, source.isGuest());
    target.put(Ace.USERS, source.getUsers());
    target.put(Ace.ROLES, source.getRoles());
    target.put(Ace.GROUPS, source.getGroups());
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
