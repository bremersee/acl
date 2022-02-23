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

package org.bremersee.acl.spring.data.mongodb.app;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bremersee.acl.Acl;
import org.bremersee.acl.annotation.AclHolder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * The example entity.
 *
 * @author Christian Bremer
 */
@Document(collection = "alc-example-collection")
@AclHolder(path = "acl")
@Data
@NoArgsConstructor
public class ExampleEntity {

  /**
   * The constant ACL.
   */
  public static final String ACL = "acl";

  /**
   * The constant OTHER_CONTENT.
   */
  public static final String OTHER_CONTENT = "otherContent";

  @Id
  private String id;

  @Field(ACL)
  private Acl acl;

  @Field(OTHER_CONTENT)
  private String otherContent;

}
