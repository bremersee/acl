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

package org.bremersee.acl.spring.data.mongodb;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.acl.AccessEvaluation;
import org.bremersee.acl.Acl;
import org.bremersee.acl.PermissionConstants;
import org.bremersee.acl.UserContext;
import org.bremersee.acl.spring.data.mongodb.app.ExampleConfiguration;
import org.bremersee.acl.spring.data.mongodb.app.ExampleEntity;
import org.bremersee.acl.spring.data.mongodb.app.ExampleEntityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * @author Christian Bremer
 */
@SpringBootTest(
    classes = {ExampleConfiguration.class},
    webEnvironment = WebEnvironment.NONE,
    properties = {
        "security.basic.enabled=false",
        "spring.data.mongodb.uri=mongodb://localhost:27017/test",
        "spring.data.mongodb.auto-index-creation=false",
        "spring.mongodb.embedded.version=3.6.2"
    })
@ExtendWith(SoftAssertionsExtension.class)
public class AclExampleIntegrationTest {

  @Autowired
  ExampleEntityRepository repository;

  @Test
  void saveAndFind(SoftAssertions softly) {
    Acl acl = Acl.builder()
        .owner("junit")
        .addRoles(PermissionConstants.READ, List.of("ROLE_USER"))
        .build();
    String content = UUID.randomUUID().toString();
    ExampleEntity entity = new ExampleEntity();
    entity.setAcl(acl);
    entity.setOtherContent(content);
    entity = repository.save(entity);

    Optional<ExampleEntity> actual = repository.findByOtherContent(
        content,
        UserContext.builder()
            .name("anna")
            .roles(List.of("ROLE_USER"))
            .build(),
        AccessEvaluation.ANY_PERMISSION,
        List.of(PermissionConstants.READ));

    ExampleEntity expected = new ExampleEntity();
    expected.setId(entity.getId());
    expected.setAcl(acl);
    expected.setOtherContent(content);
    softly.assertThat(actual)
        .hasValue(expected);

    actual = repository.findByOtherContent(
        content,
        UserContext.builder()
            .name("anna")
            .roles(List.of("ROLE_GUEST"))
            .build(),
        AccessEvaluation.ANY_PERMISSION,
        List.of(PermissionConstants.READ));

    softly.assertThat(actual)
        .isEmpty();
  }

}
