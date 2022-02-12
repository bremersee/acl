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

package org.bremersee.acl.spring.data.mongodb.app;

import java.util.Collection;
import java.util.Optional;
import org.bremersee.acl.AccessEvaluation;
import org.bremersee.acl.UserContext;
import org.bremersee.acl.spring.data.mongodb.CriteriaAndUpdateBuilder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

/**
 * @author Christian Bremer
 */
public class ExampleEntityRepositoryImpl implements ExampleEntityRepositoryCustom {

  private final CriteriaAndUpdateBuilder builder = new CriteriaAndUpdateBuilder(ExampleEntity.ACL);

  private final MongoTemplate mongoTemplate;

  public ExampleEntityRepositoryImpl(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  public Optional<ExampleEntity> findByOtherContent(
      String otherContent,
      UserContext userContext,
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

}
