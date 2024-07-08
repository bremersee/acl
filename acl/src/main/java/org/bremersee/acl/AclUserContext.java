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

import java.util.Collection;
import java.util.List;
import org.immutables.value.Value;

/**
 * The acl user context.
 *
 * @author Christian Bremer
 */
@Value.Immutable
public interface AclUserContext {

  /**
   * The constant ANONYMOUS.
   */
  String ANONYMOUS = "";

  /**
   * Creates new user context builder.
   *
   * @return the user context builder
   */
  static ImmutableAclUserContext.Builder builder() {
    return ImmutableAclUserContext.builder();
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  @Value.Default
  default String getName() {
    return ANONYMOUS;
  }

  /**
   * Gets roles.
   *
   * @return the roles
   */
  @Value.Default
  default Collection<String> getRoles() {
    return List.of();
  }

  /**
   * Gets groups.
   *
   * @return the groups
   */
  @Value.Default
  default Collection<String> getGroups() {
    return List.of();
  }

}
