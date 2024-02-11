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

package org.bremersee.acl;

import static java.util.Objects.isNull;
import static org.bremersee.acl.AclUserContext.ANONYMOUS;

import java.util.Collection;

/**
 * The access evaluator.
 *
 * @author Christian Bremer
 */
public interface AccessEvaluator {

  /**
   * Creates an access evaluator from the given access control list.
   *
   * @param acl the access control list
   * @return the access evaluator
   */
  static AccessEvaluator of(Acl acl) {
    return new Impl(acl);
  }

  /**
   * Determines whether the given user with the given roles and groups has the specified
   * permission.
   *
   * @param userContext the user context
   * @param permission the permission
   * @return {@code true} if the user has the permission, otherwise {@code false}
   */
  boolean hasPermission(
      AclUserContext userContext,
      String permission);

  /**
   * Determines whether the given user with the given roles and groups has the specified permissions
   * according to the given access evaluation type.
   *
   * @param userContext the user context
   * @param accessEvaluation the access evaluation type
   * @param permissions the permissions
   * @return {@code true} if the user has the permissions, otherwise {@code false}
   */
  default boolean hasPermissions(
      AclUserContext userContext,
      AccessEvaluation accessEvaluation,
      Collection<String> permissions) {

    if (accessEvaluation.isAnyPermission()) {
      return permissions.stream()
          .anyMatch(permission -> hasPermission(userContext, permission));
    } else {
      return !permissions.isEmpty() && permissions.stream()
          .allMatch(permission -> hasPermission(userContext, permission));
    }
  }

  /**
   * The default access evaluator implementation.
   *
   * @author Christian Bremer
   */
  class Impl implements AccessEvaluator {

    private final Acl acl;

    /**
     * Instantiates a new access evaluator.
     *
     * @param acl the acl
     */
    private Impl(Acl acl) {
      this.acl = acl;
    }

    @Override
    public boolean hasPermission(
        AclUserContext userContext,
        String permission) {

      if (isNull(acl)) {
        return false;
      }
      if (isNull(permission)) {
        return false;
      }
      if (!ANONYMOUS.equals(userContext.getName())
          && acl.getOwner().equals(userContext.getName())) {
        return true;
      }
      Ace ace = acl.getPermissionMap().getOrDefault(permission, Ace.empty());
      if (ace.isGuest()) {
        return true;
      }
      if (ace.getUsers().contains(userContext.getName())) {
        return true;
      }
      if (userContext.getRoles().stream().anyMatch(role -> ace.getRoles().contains(role))) {
        return true;
      }
      return userContext.getGroups().stream().anyMatch(group -> ace.getGroups().contains(group));
    }
  }

}
