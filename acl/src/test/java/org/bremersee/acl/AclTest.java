package org.bremersee.acl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.bremersee.acl.model.AccessControlEntryModifications;
import org.bremersee.acl.model.AccessControlListModifications;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SoftAssertionsExtension.class)
class AclTest {

  @Test
  void with(SoftAssertions softly) {
    Acl actual = Acl.with("junit", PermissionConstants.getAll(), List.of("ROLE_ADMIN"));
    softly.assertThat(actual.getOwner())
        .isEqualTo("junit");
    for (String permission : PermissionConstants.getAll()) {
      softly.assertThat(actual.getPermissionMap().get(permission))
          .isNotNull()
          .extracting(Ace::getRoles, InstanceOfAssertFactories.collection(String.class))
          .containsExactly("ROLE_ADMIN");
    }
  }

  @Test
  void getOwner(SoftAssertions softly) {
    Acl actual = Acl.builder().owner("junit_1").build();
    softly.assertThat(actual.getOwner())
        .isEqualTo("junit_1");
    softly.assertThat(Acl.builder().from(actual).build())
        .isEqualTo(actual);
    softly.assertThat(actual.toString())
        .contains("junit_1");
  }


  @Test
  void getPermissionMap() {
  }

  @Test
  void modifyWithOwner() {
    Acl target = Acl.builder().owner("anna").build();
    Optional<Acl> actual = target.modify(
        AccessControlListModifications.builder()
            .modifications(List.of(
                AccessControlEntryModifications.builder()
                    .permission("read")
                    .addUsers(List.of("james"))
                    .build()
            ))
            .build(),
        UserContext.builder()
            .name("anna")
            .build(),
        AccessEvaluation.ALL_PERMISSIONS,
        List.of(PermissionConstants.ADMINISTRATION));
    assertThat(actual)
        .hasValue(Acl.builder()
            .owner("anna")
            .addUsers("read", List.of("james"))
            .build());
  }

  @Test
  void modifyWithPermission() {
    Acl target = Acl.builder().owner("anna")
        .addUsers(PermissionConstants.ADMINISTRATION, List.of("james"))
        .build();
    Optional<Acl> actual = target.modify(
        AccessControlListModifications.builder()
            .modifications(List.of(
                AccessControlEntryModifications.builder()
                    .permission("read")
                    .isGuest(true)
                    .addUsers(List.of("james"))
                    .build(),
                AccessControlEntryModifications.builder()
                    .permission("read")
                    .isGuest(true)
                    .addUsers(List.of("anna"))
                    .build()
            ))
            .build(),
        UserContext.builder()
            .name("james")
            .build(),
        AccessEvaluation.ANY_PERMISSION,
        List.of(PermissionConstants.ADMINISTRATION));
    assertThat(actual)
        .hasValue(Acl.builder()
            .from(target)
            .guest("read", true)
            .addUsers("read", List.of("anna", "james"))
            .build());
  }

  @Test
  void modifyWithNoPermission() {
    Acl target = Acl.builder().owner("anna")
        .addUsers("write", List.of("james"))
        .build();
    Optional<Acl> actual = target.modify(
        AccessControlListModifications.builder().build(),
        UserContext.builder()
            .name("james")
            .build(),
        AccessEvaluation.ANY_PERMISSION,
        List.of(PermissionConstants.ADMINISTRATION));
    assertThat(actual)
        .isEmpty();
  }

}