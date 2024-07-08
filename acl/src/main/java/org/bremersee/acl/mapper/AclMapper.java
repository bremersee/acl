/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.acl.mapper;

import org.bremersee.acl.Acl;
import org.bremersee.acl.model.AccessControlList;

/**
 * The acl mapper.
 *
 * @param <T> the acl type
 * @author Christian Bremer
 */
public interface AclMapper<T extends Acl> {

  /**
   * Map access control list (entity) to dto.
   *
   * @param acl the acl
   * @return the access control list
   */
  AccessControlList map(T acl);

  /**
   * Map access control list dto to business object.
   *
   * @param accessControlList the access control list
   * @return the acl of the specified type
   */
  T map(AccessControlList accessControlList);

}
