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
import org.bremersee.acl.Ace;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;

/**
 * The document to ace converter.
 *
 * @author Christian Bremer
 */
@ReadingConverter
public class DocumentToAceConverter implements Converter<Document, Ace> {

  @Override
  public Ace convert(@NonNull Document source) {
    return Ace.builder()
        .guest(source.getBoolean(Ace.GUEST, false))
        .users(source.getList(Ace.USERS, String.class, List.of()))
        .roles(source.getList(Ace.ROLES, String.class, List.of()))
        .groups(source.getList(Ace.GROUPS, String.class, List.of()))
        .build();
  }
}
