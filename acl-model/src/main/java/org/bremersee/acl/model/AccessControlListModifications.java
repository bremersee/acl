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

package org.bremersee.acl.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.immutables.value.Value;

/**
 * Specifies modifications of an access control list.
 *
 * @author Christian Bremer
 */
@Value.Immutable
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

  /**
   * Gets modifications distinct.
   *
   * @return the modifications distinct
   */
  @Schema(hidden = true)
  @JsonIgnore
  @Value.Derived
  default Collection<AccessControlEntryModifications> getModificationsDistinct() {
    return getModifications()
        .stream()
        .collect(Collectors.toMap(
            AccessControlEntryModifications::getPermission,
            mod -> mod,
            (first, second) -> AccessControlEntryModifications.builder()
                .from(first)
                .isGuest(first.isGuest() && second.isGuest())
                .addUsers(Stream
                    .concat(first.getAddUsers().stream(), second.getAddUsers().stream())
                    .collect(Collectors.toSet()))
                .removeUsers(Stream
                    .concat(first.getRemoveUsers().stream(), second.getRemoveUsers().stream())
                    .collect(Collectors.toSet()))
                .addRoles(Stream
                    .concat(first.getAddRoles().stream(), second.getAddRoles().stream())
                    .collect(Collectors.toSet()))
                .removeRoles(Stream
                    .concat(first.getRemoveRoles().stream(), second.getRemoveRoles().stream())
                    .collect(Collectors.toSet()))
                .addGroups(Stream
                    .concat(first.getAddGroups().stream(), second.getAddGroups().stream())
                    .collect(Collectors.toSet()))
                .removeGroups(Stream
                    .concat(first.getRemoveGroups().stream(), second.getRemoveGroups().stream())
                    .collect(Collectors.toSet()))
                .build()))
        .values();
  }

}
