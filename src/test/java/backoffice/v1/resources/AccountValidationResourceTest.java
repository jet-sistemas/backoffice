package backoffice.v1.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import backoffice.common.services.mail.LogMailProvider;
import backoffice.v1.entities.AccountValidationCode;
import backoffice.v1.repositories.AccountValidationCodeRepository;
import backoffice.v1.repositories.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AccountValidationResourceTest {

  private static final String ADMIN_USER_PATH = "/v1/admin/user";
  private static final String VALIDATION_PATH = "/v1/account-validation/confirm";
  private static final String AUTH_PATH = "/v1/auth";
  private static final String AUTH_ME_PATH = "/v1/auth/me";
  private static final String CHANGE_PASSWORD_PATH = "/v1/auth/change-password";

  @Inject
  LogMailProvider logMailProvider;

  @Inject
  UserRepository userRepository;

  @Inject
  AccountValidationCodeRepository accountValidationCodeRepository;

  @BeforeEach
  void clearMail() {
    logMailProvider.clear();
  }

  private static String uniqueDocument() {
    long n = Math.abs(System.nanoTime() % 1_000_000_000L);
    return "1" + String.format("%010d", n);
  }

  private static String uniqueEmail(String prefix) {
    return prefix + "-" + System.nanoTime() + "@test.com";
  }

  private static String uniqueCode() {
    return "M" + String.format("%04d", (int) (Math.random() * 9999));
  }

  private static Map<String, Object> memberPayload(String email, String code, String document) {
    var payload = new HashMap<String, Object>();
    payload.put("user", Map.of(
        "email", email,
        "name", "Membro Validação",
        "document", document,
        "code", code,
        "type", "MEMBER"));
    payload.put("member", Map.of(
        "fullname", "Membro Validação Completo",
        "whatsapp", "119" + String.format("%09d", Math.abs(System.nanoTime() % 1_000_000_000L)),
        "type", "SUBSCRIBER",
        "subscriber", Map.of("monthlyFeeAmount", 50.0, "billingDay", 10)));
    return payload;
  }

  private String extractTokenFromUrl(String url) {
    return url.substring(url.lastIndexOf('/') + 1);
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("criação de membro gera conta inativa e envia e-mail")
  void createMember_inactiveAndEmailSent() {
    String email = uniqueEmail("create-member");

    given()
        .contentType(ContentType.JSON)
        .body(memberPayload(email, uniqueCode(), uniqueDocument()))
        .when()
        .post(ADMIN_USER_PATH)
        .then()
        .statusCode(201)
        .body("data.accountActive", is(false))
        .body("data.accountValidationStatus", is("PENDING"));

    assertNotNull(logMailProvider.getLastSent());
    assertEquals(email, logMailProvider.getLastSent().toEmail());
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("login antes da validação retorna 403")
  void loginBeforeValidation_forbidden() {
    String document = uniqueDocument();
    String email = uniqueEmail("login-block");

    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(email, uniqueCode(), document))
        .when()
        .post(ADMIN_USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    var mail = logMailProvider.getLastSent();

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("email", email, "password", mail.temporaryPassword()))
        .when()
        .post(AUTH_PATH)
        .then()
        .statusCode(403);
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("validação correta ativa conta")
  void confirmAccount_success() {
    String document = uniqueDocument();
    String email = uniqueEmail("confirm");

    given()
        .contentType(ContentType.JSON)
        .body(memberPayload(email, uniqueCode(), document))
        .when()
        .post(ADMIN_USER_PATH)
        .then()
        .statusCode(201);

    var mail = logMailProvider.getLastSent();
    String token = extractTokenFromUrl(mail.validationUrl());

    given()
        .contentType(ContentType.JSON)
        .body(Map.of(
            "token", token,
            "code", mail.validationCode(),
            "document", document))
        .when()
        .post(VALIDATION_PATH)
        .then()
        .statusCode(200)
        .body("data.accountActive", is(true))
        .body("data.mustChangePassword", is(true))
        .body("data.email", is(email));
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("documento divergente retorna 400")
  void confirmAccount_wrongDocument() {
    String document = uniqueDocument();

    given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("doc"), uniqueCode(), document))
        .when()
        .post(ADMIN_USER_PATH)
        .then()
        .statusCode(201);

    var mail = logMailProvider.getLastSent();
    String token = extractTokenFromUrl(mail.validationUrl());

    given()
        .contentType(ContentType.JSON)
        .body(Map.of(
            "token", token,
            "code", mail.validationCode(),
            "document", "99999999999"))
        .when()
        .post(VALIDATION_PATH)
        .then()
        .statusCode(400);
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("código inválido incrementa tentativa")
  void confirmAccount_invalidCode_incrementsAttempt() {
    String document = uniqueDocument();

    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("attempt"), uniqueCode(), document))
        .when()
        .post(ADMIN_USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    var mail = logMailProvider.getLastSent();
    String token = extractTokenFromUrl(mail.validationUrl());

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("token", token, "code", "ZZZZZ", "document", document))
        .when()
        .post(VALIDATION_PATH)
        .then()
        .statusCode(400);

    AccountValidationCode invite = accountValidationCodeRepository.findActiveByUserId((long) userId).orElseThrow();
    assertEquals(1, invite.getAttemptCount());
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("usuário com troca pendente bloqueia rota admin")
  void mustChangePassword_blocksAdminRoute() {
    String document = uniqueDocument();
    String email = uniqueEmail("must-change");

    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(email, uniqueCode(), document))
        .when()
        .post(ADMIN_USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    var mail = logMailProvider.getLastSent();
    String token = extractTokenFromUrl(mail.validationUrl());

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("token", token, "code", mail.validationCode(), "document", document))
        .when()
        .post(VALIDATION_PATH)
        .then()
        .statusCode(200);

    String accessToken = given()
        .contentType(ContentType.JSON)
        .body(Map.of("email", email, "password", mail.temporaryPassword()))
        .when()
        .post(AUTH_PATH)
        .then()
        .statusCode(200)
        .extract()
        .path("data.accessToken");

    given()
        .header("Authorization", "Bearer " + accessToken)
        .when()
        .get(ADMIN_USER_PATH)
        .then()
        .statusCode(403);
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("troca de senha remove mustChangePassword")
  void changePassword_success() {
    String document = uniqueDocument();
    String email = uniqueEmail("change-pass");

    given()
        .contentType(ContentType.JSON)
        .body(memberPayload(email, uniqueCode(), document))
        .when()
        .post(ADMIN_USER_PATH)
        .then()
        .statusCode(201);

    var mail = logMailProvider.getLastSent();
    String token = extractTokenFromUrl(mail.validationUrl());

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("token", token, "code", mail.validationCode(), "document", document))
        .when()
        .post(VALIDATION_PATH)
        .then()
        .statusCode(200);

    String accessToken = given()
        .contentType(ContentType.JSON)
        .body(Map.of("email", email, "password", mail.temporaryPassword()))
        .when()
        .post(AUTH_PATH)
        .then()
        .statusCode(200)
        .extract()
        .path("data.accessToken");

    given()
        .header("Authorization", "Bearer " + accessToken)
        .contentType(ContentType.JSON)
        .body(Map.of(
            "currentPassword", mail.temporaryPassword(),
            "newPassword", "NovaSenha1!",
            "confirmPassword", "NovaSenha1!"))
        .when()
        .post(CHANGE_PASSWORD_PATH)
        .then()
        .statusCode(200)
        .body("data.mustChangePassword", is(false));
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("reenvio antes da expiração falha")
  void resendBeforeExpiration_fails() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("resend-fail"), uniqueCode(), uniqueDocument()))
        .when()
        .post(ADMIN_USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    given()
        .when()
        .post(ADMIN_USER_PATH + "/" + userId + "/resend-account-validation")
        .then()
        .statusCode(400);
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("reenvio após expiração envia novo convite")
  void resendAfterExpiration_success() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("resend-ok"), uniqueCode(), uniqueDocument()))
        .when()
        .post(ADMIN_USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    logMailProvider.clear();
    accountValidationCodeRepository.expireActiveInviteForUser((long) userId);

    given()
        .when()
        .post(ADMIN_USER_PATH + "/" + userId + "/resend-account-validation")
        .then()
        .statusCode(200)
        .body("data.sent", is(true))
        .body("data.accountValidationStatus", is("PENDING"));

    assertNotNull(logMailProvider.getLastSent());
  }
}
