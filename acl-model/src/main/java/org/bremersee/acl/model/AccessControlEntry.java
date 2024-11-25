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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.util.Collection;
import java.util.List;
import org.immutables.value.Value;

/**
 * Specifies a permission and who is granted.
 *
 * @author Christian Bremer
 */
@Value.Immutable
@Schema(description = "Specifies a permission and who is granted.")
@JsonDeserialize(builder = ImmutableAccessControlEntry.Builder.class)
public interface AccessControlEntry extends Comparable<AccessControlEntry> {

  /**
   * The constant PERMISSION.
   */
  String PERMISSION = "permission";

  /**
   * The constant GUEST.
   */
  String GUEST = "guest";

  /**
   * The constant USERS.
   */
  String USERS = "users";

  /**
   * The constant ROLES.
   */
  String ROLES = "roles";

  /**
   * The constant GROUPS.
   */
  String GROUPS = "groups";

  /**
   * Creates new builder.
   *
   * @return the access control entry builder
   */
  static ImmutableAccessControlEntry.Builder builder() {
    return ImmutableAccessControlEntry.builder();
  }

  /**
   * Specifies the permission.
   *
   * @return the permission
   */
  @Schema(
      description = "Specifies the permission.",
      requiredMode = RequiredMode.REQUIRED,
      example = "read")
  @JsonProperty(value = PERMISSION, required = true)
  String getPermission();

  /**
   * Specifies whether anybody is granted or not.
   *
   * @return whether anybody is granted (true) or not (false)
   */
  @Schema(description = "Specifies whether anybody is granted.", defaultValue = "false")
  @JsonProperty(value = GUEST, defaultValue = "false")
  @Value.Default
  default boolean isGuest() {
    return false;
  }

  /**
   * Specifies the granted users.
   *
   * @return the users
   */
  @Schema(description = "Specifies the granted users.")
  @JsonProperty(USERS)
  @Value.Default
  default Collection<String> getUsers() {
    return List.of();
  }

  /**
   * Specifies the granted roles.
   *
   * @return the roles
   */
  @Schema(description = "Specifies the granted roles.")
  @JsonProperty(ROLES)
  @Value.Default
  default Collection<String> getRoles() {
    return List.of();
  }

  /**
   * Specifies the granted groups.
   *
   * @return the groups
   */
  @Schema(description = "Specifies the granted groups.")
  @JsonProperty(GROUPS)
  @Value.Default
  default Collection<String> getGroups() {
    return List.of();
  }

  @Override
  default int compareTo(AccessControlEntry o) {
    return getPermission().compareToIgnoreCase(o.getPermission());
  }

}
