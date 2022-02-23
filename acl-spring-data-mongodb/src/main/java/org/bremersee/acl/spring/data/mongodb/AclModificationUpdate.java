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

package org.bremersee.acl.spring.data.mongodb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;
import org.springframework.data.mongodb.core.query.Update;

/**
 * The acl modification update.
 *
 * @author Christian Bremer
 */
@Value.Immutable
@Value.Style(visibility = ImplementationVisibility.PACKAGE)
public interface AclModificationUpdate {

  /**
   * Creates acl modification update builder.
   *
   * @return the acl modification update builder
   */
  static ImmutableAclModificationUpdate.Builder builder() {
    return ImmutableAclModificationUpdate.builder();
  }

  /**
   * Gets preparation updates.
   *
   * @return the preparation updates
   */
  Collection<Update> getPreparationUpdates();

  /**
   * Gets final update.
   *
   * @return the final update
   */
  Update getFinalUpdate();

  /**
   * Gets updates.
   *
   * @return the updates
   */
  @Value.Derived
  default Collection<Update> getUpdates() {
    List<Update> updates = new ArrayList<>(getPreparationUpdates());
    updates.add(getFinalUpdate());
    return Collections.unmodifiableCollection(updates);
  }

}
