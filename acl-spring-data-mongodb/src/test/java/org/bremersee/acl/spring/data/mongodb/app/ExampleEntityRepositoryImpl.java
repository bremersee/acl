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
import java.util.List;
import java.util.Optional;
import org.bremersee.acl.AccessEvaluation;
import org.bremersee.acl.Acl;
import org.bremersee.acl.PermissionConstants;
import org.bremersee.acl.AclUserContext;
import org.bremersee.acl.model.AccessControlListModifications;
import org.bremersee.acl.spring.data.mongodb.AclCriteriaAndUpdateBuilder;
import org.bremersee.acl.spring.data.mongodb.AclIndexOperations;
import org.bremersee.acl.spring.data.mongodb.AclModificationUpdate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

/**
 * The example entity repository.
 *
 * @author Christian Bremer
 */
public class ExampleEntityRepositoryImpl implements ExampleEntityRepositoryCustom {

  private final AclCriteriaAndUpdateBuilder builder;

  private final MongoTemplate mongoTemplate;

  /**
   * Instantiates a new example entity repository.
   *
   * @param mongoTemplate the mongo template
   */
  public ExampleEntityRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
    builder = new AclCriteriaAndUpdateBuilder(ExampleEntity.class);
  }

  /**
   * Init.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void init() {
    new AclIndexOperations(this.mongoTemplate).ensureAclIndexes(
        ExampleEntity.class,
        PermissionConstants.getAll(),
        false);
  }

  @Override
  public Optional<ExampleEntity> findByOtherContent(
      String otherContent,
      AclUserContext userContext,
      AccessEvaluation accessEvaluation,
      Collection<String> permissions) {

    Criteria accessCriteria = builder.buildPermissionCriteria(
        userContext,
        accessEvaluation,
        permissions);
    Criteria otherContentCriteria = Criteria.where(ExampleEntity.OTHER_CONTENT).is(otherContent);
    Query query = Query.query(new Criteria().andOperator(accessCriteria, otherContentCriteria));
    return Optional.ofNullable(mongoTemplate.findOne(query, ExampleEntity.class));
  }

  @Override
  public Optional<ExampleEntity> modifyAclByOtherContent(
      String otherContent,
      AclUserContext userContext,
      AccessControlListModifications modifications) {

    Criteria accessCriteria = builder.buildPermissionCriteria(
        userContext,
        AccessEvaluation.ANY_PERMISSION,
        List.of(PermissionConstants.ADMINISTRATION));
    Criteria otherContentCriteria = Criteria.where(ExampleEntity.OTHER_CONTENT).is(otherContent);
    Query query = Query.query(new Criteria().andOperator(accessCriteria, otherContentCriteria));
    AclModificationUpdate updates = builder.buildUpdate(modifications);
    updates.getPreparationUpdates()
        .forEach(preparationUpdate -> mongoTemplate
            .updateFirst(query, preparationUpdate, ExampleEntity.class));
    FindAndModifyOptions options = new FindAndModifyOptions()
        .returnNew(true);
    return Optional.ofNullable(mongoTemplate.findAndModify(
        query,
        updates.getFinalUpdate(),
        options,
        ExampleEntity.class));
  }

  @Override
  public Optional<ExampleEntity> replaceAclByOtherContent(String otherContent, Acl newAcl) {
    Criteria otherContentCriteria = Criteria.where(ExampleEntity.OTHER_CONTENT).is(otherContent);
    Query query = Query.query(otherContentCriteria);
    Update update = builder.buildUpdate(newAcl);
    FindAndModifyOptions options = new FindAndModifyOptions()
        .returnNew(true);
    return Optional
        .ofNullable(mongoTemplate.findAndModify(query, update, options, ExampleEntity.class));
  }

  @Override
  public Optional<ExampleEntity> changeOwnerByOtherContent(
      String otherContent,
      AclUserContext userContext,
      String newOwner) {

    Criteria accessCriteria = builder.buildUpdateOwnerCriteria(userContext);
    Criteria otherContentCriteria = Criteria.where(ExampleEntity.OTHER_CONTENT).is(otherContent);
    Query query = Query.query(new Criteria().andOperator(accessCriteria, otherContentCriteria));
    Update update = builder.buildUpdate(newOwner);
    FindAndModifyOptions options = new FindAndModifyOptions()
        .returnNew(true);
    return Optional
        .ofNullable(mongoTemplate.findAndModify(query, update, options, ExampleEntity.class));
  }

}
