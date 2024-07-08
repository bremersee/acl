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

import java.util.Collection;
import java.util.Optional;
import org.bremersee.acl.AccessEvaluation;
import org.bremersee.acl.Acl;
import org.bremersee.acl.AclUserContext;
import org.bremersee.acl.model.AccessControlListModifications;

/**
 * The example entity repository custom.
 *
 * @author Christian Bremer
 */
public interface ExampleEntityRepositoryCustom {

  /**
   * Find by other content optional.
   *
   * @param otherContent the other content
   * @param userContext the user context
   * @param accessEvaluation the access evaluation
   * @param permissions the permissions
   * @return the optional
   */
  Optional<ExampleEntity> findByOtherContent(
      String otherContent,
      AclUserContext userContext,
      AccessEvaluation accessEvaluation,
      Collection<String> permissions);

  /**
   * Modify acl by other content optional.
   *
   * @param otherContent the other content
   * @param userContext the user context
   * @param modifications the modifications
   * @return the optional
   */
  Optional<ExampleEntity> modifyAclByOtherContent(
      String otherContent,
      AclUserContext userContext,
      AccessControlListModifications modifications);

  /**
   * Replace acl by other content optional.
   *
   * @param otherContent the other content
   * @param newAcl the new acl
   * @return the optional
   */
  Optional<ExampleEntity> replaceAclByOtherContent(String otherContent, Acl newAcl);

  /**
   * Change owner by other content optional.
   *
   * @param otherContent the other content
   * @param userContext the user context
   * @param newOwner the new owner
   * @return the optional
   */
  Optional<ExampleEntity> changeOwnerByOtherContent(
      String otherContent,
      AclUserContext userContext,
      String newOwner);

}
