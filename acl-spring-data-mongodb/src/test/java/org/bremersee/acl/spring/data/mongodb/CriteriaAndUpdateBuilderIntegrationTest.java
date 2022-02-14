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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.acl.AccessEvaluation;
import org.bremersee.acl.Acl;
import org.bremersee.acl.PermissionConstants;
import org.bremersee.acl.UserContext;
import org.bremersee.acl.model.AccessControlEntryModifications;
import org.bremersee.acl.model.AccessControlListModifications;
import org.bremersee.acl.spring.data.mongodb.app.ExampleConfiguration;
import org.bremersee.acl.spring.data.mongodb.app.ExampleEntity;
import org.bremersee.acl.spring.data.mongodb.app.ExampleEntityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

/**
 * The criteria and update builder integration test.
 *
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
public class CriteriaAndUpdateBuilderIntegrationTest {

  /**
   * The repository.
   */
  @Autowired
  ExampleEntityRepository repository;

  /**
   * Save and find.
   *
   * @param softly the softly
   */
  @Test
  void saveAndFind(SoftAssertions softly) {
    Acl acl = Acl.builder()
        .owner("junit")
        .addUsers(PermissionConstants.WRITE, List.of("james"))
        .addRoles(PermissionConstants.READ, List.of("ROLE_USER"))
        .addGroups(PermissionConstants.DELETE, List.of("cron"))
        .build();
    String content = UUID.randomUUID().toString();
    ExampleEntity entity = new ExampleEntity();
    entity.setAcl(acl);
    entity.setOtherContent(content);
    entity = repository.save(entity);

    ExampleEntity expected = new ExampleEntity();
    expected.setId(entity.getId());
    expected.setAcl(acl);
    expected.setOtherContent(content);

    Optional<ExampleEntity> actual = repository.findByOtherContent(
        content,
        UserContext.builder()
            .name("anna")
            .roles(List.of("ROLE_USER"))
            .build(),
        AccessEvaluation.ANY_PERMISSION,
        List.of(PermissionConstants.READ));
    softly.assertThat(actual)
        .as("Anna can read, because she has role 'ROLE_USER'.")
        .hasValue(expected);

    actual = repository.findByOtherContent(
        content,
        UserContext.builder()
            .name("max")
            .roles(List.of("ROLE_GUEST"))
            .build(),
        AccessEvaluation.ANY_PERMISSION,
        List.of(PermissionConstants.READ));
    softly.assertThat(actual)
        .as("Mx can not read, because he has only role 'ROLE_GUEST'.")
        .isEmpty();

    actual = repository.findByOtherContent(
        content,
        UserContext.builder()
            .name("james")
            .roles(List.of("ROLE_USER"))
            .build(),
        AccessEvaluation.ALL_PERMISSIONS,
        List.of(PermissionConstants.READ, PermissionConstants.WRITE));
    softly.assertThat(actual)
        .as("James can read and write, because he has role 'ROLE_USER' "
            + "and is user of permission 'write'.")
        .hasValue(expected);

    actual = repository.findByOtherContent(
        content,
        UserContext.builder()
            .name("james")
            .roles(List.of("ROLE_USER"))
            .build(),
        AccessEvaluation.ALL_PERMISSIONS,
        List.of(PermissionConstants.READ, PermissionConstants.WRITE, PermissionConstants.DELETE));
    softly.assertThat(actual)
        .as("James can not delete, because he is not in group 'cron'.")
        .isEmpty();

    actual = repository.findByOtherContent(
        content,
        UserContext.builder()
            .name("")
            .groups(List.of("cron"))
            .build(),
        AccessEvaluation.ALL_PERMISSIONS,
        List.of(PermissionConstants.DELETE));
    softly.assertThat(actual)
        .as("Cron can delete, it's member of group 'cron'.")
        .hasValue(expected);

    actual = repository.findByOtherContent(
        content,
        UserContext.builder()
            .name("junit")
            .build(),
        AccessEvaluation.ALL_PERMISSIONS,
        PermissionConstants.getAll());
    softly.assertThat(actual)
        .as("Junit can do everything, because it is the owner.")
        .hasValue(expected);
  }

  /**
   * Modify acl.
   *
   * @param softly the softly
   */
  @Test
  void modifyAcl(SoftAssertions softly) {
    Acl acl = Acl.builder()
        .owner("junit")
        .addUsers(PermissionConstants.WRITE, List.of("james"))
        .addRoles(PermissionConstants.READ, List.of("ROLE_USER"))
        .addGroups(PermissionConstants.DELETE, List.of("cron"))
        .addUsers(PermissionConstants.ADMINISTRATION, List.of("anna"))
        .build();
    String content = UUID.randomUUID().toString();
    ExampleEntity entity = new ExampleEntity();
    entity.setAcl(acl);
    entity.setOtherContent(content);
    entity = repository.save(entity);

    AccessControlListModifications modifications = AccessControlListModifications.builder()
        .modifications(List.of(
            AccessControlEntryModifications.builder()
                .permission(PermissionConstants.WRITE)
                .addRemoveUsers("james", "not_exists")
                .addAddUsers("anna", "stephen")
                .build(),
            AccessControlEntryModifications.builder()
                .permission(PermissionConstants.READ)
                .isGuest(true)
                .addRemoveRoles("ROLE_USER")
                .build(),
            AccessControlEntryModifications.builder()
                .permission(PermissionConstants.DELETE)
                .addAddGroups("net", "dev", "cron")
                .build()
        ))
        .build();

    Acl expectedAcl = Acl.builder()
        .owner("junit")
        .addUsers(PermissionConstants.WRITE, List.of("anna", "stephen"))
        .guest(PermissionConstants.READ, true)
        .addRoles(PermissionConstants.READ, List.of())
        .addGroups(PermissionConstants.DELETE, List.of("cron", "dev", "net"))
        .addUsers(PermissionConstants.ADMINISTRATION, List.of("anna"))
        .build();

    softly
        .assertThat(acl
            .modify(
                modifications,
                UserContext.builder()
                    .name("anna")
                    .build(),
                AccessEvaluation.ANY_PERMISSION,
                List.of(PermissionConstants.ADMINISTRATION)))
        .hasValue(expectedAcl);

    ExampleEntity expected = new ExampleEntity();
    expected.setId(entity.getId());
    expected.setAcl(expectedAcl);
    expected.setOtherContent(content);

    Optional<ExampleEntity> actual = repository.modifyAclByOtherContent(
        content,
        UserContext.builder()
            .name("anna")
            .build(),
        modifications);

    softly.assertThat(actual)
        .hasValue(expected);
  }

  /**
   * Replace acl.
   *
   * @param softly the softly
   */
  @Test
  void replaceAcl(SoftAssertions softly) {
    Acl acl = Acl.builder()
        .owner("junit")
        .addUsers(PermissionConstants.WRITE, List.of("james"))
        .addRoles(PermissionConstants.READ, List.of("ROLE_USER"))
        .addGroups(PermissionConstants.DELETE, List.of("cron"))
        .build();
    String content = UUID.randomUUID().toString();
    ExampleEntity entity = new ExampleEntity();
    entity.setAcl(acl);
    entity.setOtherContent(content);
    entity = repository.save(entity);

    Acl expectedAcl = Acl.builder().from(acl)
        .addPermissions(PermissionConstants.getAll())
        .build();

    ExampleEntity expected = new ExampleEntity();
    expected.setId(entity.getId());
    expected.setAcl(expectedAcl);
    expected.setOtherContent(content);

    Optional<ExampleEntity> actual = repository.replaceAclByOtherContent(content, expectedAcl);
    softly.assertThat(actual)
        .hasValue(expected);

    expected.setAcl(Acl.builder().build());
    actual = repository.replaceAclByOtherContent(content, null);
    softly.assertThat(actual)
        .hasValue(expected);
    softly.assertThat(actual)
        .hasValue(expected);
  }

  /**
   * Change owner.
   *
   * @param softly the softly
   */
  @Test
  void changeOwner(SoftAssertions softly) {
    Acl acl = Acl.builder()
        .owner("junit")
        .addUsers(PermissionConstants.WRITE, List.of("james"))
        .addRoles(PermissionConstants.READ, List.of("ROLE_USER"))
        .addGroups(PermissionConstants.DELETE, List.of("cron"))
        .build();
    String content = UUID.randomUUID().toString();
    ExampleEntity entity = new ExampleEntity();
    entity.setAcl(acl);
    entity.setOtherContent(content);
    entity = repository.save(entity);

    Acl expectedAcl = Acl.builder().from(acl)
        .owner("java")
        .build();

    ExampleEntity expected = new ExampleEntity();
    expected.setId(entity.getId());
    expected.setAcl(expectedAcl);
    expected.setOtherContent(content);

    Optional<ExampleEntity> actual = repository.changeOwnerByOtherContent(
        content,
        UserContext.builder()
            .name("junit")
            .build(), "java");
    softly.assertThat(actual)
        .hasValue(expected);

    actual = repository.changeOwnerByOtherContent(
        content,
        UserContext.builder()
            .name("someone")
            .build(), "swift");
    softly.assertThat(actual)
        .isEmpty();
  }

}
