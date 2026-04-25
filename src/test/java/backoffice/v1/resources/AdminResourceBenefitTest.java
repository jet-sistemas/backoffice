package backoffice.v1.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AdminResourceBenefitTest {

  private static final String USER_PATH = "/v1/admin/user";
  private static final String BENEFIT_PATH = "/v1/admin/benefit";

  private static String uniqueDocument() {
    long n = Math.abs(System.nanoTime() % 1_000_000_000L);
    return "1" + String.format("%010d", n);
  }

  private static String uniqueEmail(String prefix) {
    return prefix + "-" + System.currentTimeMillis() + "@test.com";
  }

  private static String uniqueCode() {
    return "S" + String.format("%04d", (int) (Math.random() * 9999));
  }

  private static Map<String, Object> sponsorUserPayload(String email, String code) {
    var payload = new HashMap<String, Object>();
    payload.put("user", Map.of(
        "email", email,
        "name", "Patrocinador Teste",
        "document", uniqueDocument(),
        "code", code,
        "type", "SPONSOR"
    ));
    payload.put("sponsor", Map.of(
        "publicName", "Patroc Public",
        "tier", "GOLD",
        "entityType", "COMPANY",
        "persona", "OTHER"
    ));
    return payload;
  }

  @Nested
  @DisplayName("PATCH /v1/admin/benefit/{id}/activate")
  class ActivateBenefit {

    @Test
    @DisplayName("retorna 401 quando não autenticado")
    void withoutAuth_returns401() {
      given()
          .when()
          .patch(BENEFIT_PATH + "/1/activate")
          .then()
          .statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 404 quando benefício não existe")
    void notFound_returns404() {
      given()
          .when()
          .patch(BENEFIT_PATH + "/999999/activate")
          .then()
          .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("reativa benefício geral inativo (200) e idempotente")
    void generalInactive_reactivates200_idempotent() {
      Long id = given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "name", "Benef geral reativar",
              "description", "d"
          ))
          .when()
          .post(BENEFIT_PATH)
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      given()
          .when()
          .patch(BENEFIT_PATH + "/" + id + "/deactivate")
          .then()
          .statusCode(200);

      given()
          .when()
          .get(BENEFIT_PATH + "/" + id)
          .then()
          .statusCode(200)
          .body("data.active", is(false));

      given()
          .when()
          .patch(BENEFIT_PATH + "/" + id + "/activate")
          .then()
          .statusCode(200)
          .body("status", is("OK"));

      given()
          .when()
          .get(BENEFIT_PATH + "/" + id)
          .then()
          .statusCode(200)
          .body("data.active", is(true));

      given()
          .when()
          .patch(BENEFIT_PATH + "/" + id + "/activate")
          .then()
          .statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("reativa benefício com patrocinador ativo (200)")
    void linkedToActiveSponsor_reactivates200() {
      String email = uniqueEmail("spon-act-ben");
      String code = uniqueCode();
      Long userId = given()
          .contentType(ContentType.JSON)
          .body(sponsorUserPayload(email, code))
          .when()
          .post(USER_PATH)
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      Long sponsorId = given()
          .when()
          .get(USER_PATH + "/" + userId)
          .then()
          .statusCode(200)
          .extract()
          .jsonPath()
          .getLong("data.sponsor.id");

      Long benefitId = given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "name", "Ben com sponsor ativo",
              "description", "x",
              "sponsorId", sponsorId
          ))
          .when()
          .post(BENEFIT_PATH)
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      given()
          .when()
          .patch(BENEFIT_PATH + "/" + benefitId + "/deactivate")
          .then()
          .statusCode(200);

      given()
          .when()
          .patch(BENEFIT_PATH + "/" + benefitId + "/activate")
          .then()
          .statusCode(200);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 400 quando patrocinador do benefício está inativo")
    void sponsorInactive_returns400() {
      String email = uniqueEmail("spon-inact-ben");
      String code = uniqueCode();
      Long userId = given()
          .contentType(ContentType.JSON)
          .body(sponsorUserPayload(email, code))
          .when()
          .post(USER_PATH)
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      Long sponsorId = given()
          .when()
          .get(USER_PATH + "/" + userId)
          .then()
          .statusCode(200)
          .extract()
          .jsonPath()
          .getLong("data.sponsor.id");

      Long benefitId = given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "name", "Ben sponsor vai inativar",
              "description", "x",
              "sponsorId", sponsorId
          ))
          .when()
          .post(BENEFIT_PATH)
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      given()
          .when()
          .patch(USER_PATH + "/" + userId + "/deactivate")
          .then()
          .statusCode(200);

      given()
          .when()
          .get(BENEFIT_PATH + "/" + benefitId)
          .then()
          .statusCode(200)
          .body("data.active", is(false));

      given()
          .when()
          .patch(BENEFIT_PATH + "/" + benefitId + "/activate")
          .then()
          .statusCode(400)
          .body("message", is("Este patrocinador não está ativo."));
    }
  }

  @Nested
  @DisplayName("PATCH /v1/admin/benefit/{id}/deactivate (idempotência)")
  class DeactivateBenefitIdempotent {

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("desativar benefício já inativo retorna 200")
    void alreadyInactive_still200() {
      Long id = given()
          .contentType(ContentType.JSON)
          .body(Map.of("name", "Ben idem desativ", "description", "d"))
          .when()
          .post(BENEFIT_PATH)
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      given()
          .when()
          .patch(BENEFIT_PATH + "/" + id + "/deactivate")
          .then()
          .statusCode(200);

      given()
          .when()
          .patch(BENEFIT_PATH + "/" + id + "/deactivate")
          .then()
          .statusCode(200);
    }
  }
}
