/*
 * Copyright 2020 the original author or authors.
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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.immutables.value.Value;

/**
 * @author Christian Bremer
 */
@Value.Immutable
@Valid
public interface UserContext {

  String ANONYMOUS = "";

  static ImmutableUserContext.Builder builder() {
    return ImmutableUserContext.builder();
  }

  @Value.Default
  @NotNull
  default String getName() {
    return ANONYMOUS;
  }

  @Value.Default
  @NotNull
  default Collection<String> getRoles() {
    return List.of();
  }

  @Value.Default
  @NotNull
  default Collection<String> getGroups() {
    return List.of();
  }

}
