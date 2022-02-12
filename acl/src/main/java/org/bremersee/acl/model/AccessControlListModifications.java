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

package org.bremersee.acl.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.List;
import javax.validation.Valid;
import org.immutables.value.Value;

/**
 * Specifies modifications of an access control list.
 *
 * @author Christian Bremer
 */
@Value.Immutable
@Valid
@Schema(description = "Specifies modifications of an access control list.")
@JsonDeserialize(builder = ImmutableAccessControlListModifications.Builder.class)
public interface AccessControlListModifications {

  /**
   * Creates new builder.
   *
   * @return the access control list modifications builder
   */
  static ImmutableAccessControlListModifications.Builder builder() {
    return ImmutableAccessControlListModifications.builder();
  }

  /**
   * Get modifications.
   *
   * @return the modifications
   */
  @Schema(description = "The access control entry modifications.")
  @Value.Default
  default Collection<AccessControlEntryModifications> getModifications() {
    return List.of();
  }

}
