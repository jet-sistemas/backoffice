package backoffice.v1.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AdminResourceSponsorTest {

  private static final String BASE_PATH = "/v1/admin/sponsor";

  /** Documento único por chamada (11 dígitos, CPF) para evitar 409 em testes que criam sponsor. */
  private static String uniqueDocument() {
    long n = Math.abs(System.nanoTime() % 1_000_000_000L);
    return "1" + String.format("%010d", n);
  }

  private static Map<String, Object> sponsorCreatePayload(String email, String code) {
    return sponsorCreatePayload(email, code, uniqueDocument());
  }

  private static Map<String, Object> sponsorCreatePayload(String email, String code, String document) {
    return Map.of(
        "user", Map.of(
            "email", email,
            "name", "Patrocinador Teste",
            "document", document,
            "code", code,
            "type", "SPONSOR"
        ),
        "publicName", "Patrocinador Public Name",
        "tier", "GOLD",
        "entityType", "COMPANY",
        "persona", "OTHER"
    );
  }

  @Nested
  @DisplayName("GET /v1/admin/sponsor - Listagem de patrocinadores")
  class ListSponsors {

    @Test
    @DisplayName("retorna 401 quando não autenticado")
    void listWithoutAuth_returns401() {
      given()
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 200 e lista paginada quando autenticado como ADM")
    void listWithAdmin_returns200AndPageable() {
      given()
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(200)
          .body("status", is("OK"))
          .body("statusCode", is(200))
          .body("data", notNullValue())
          .body("totalElements", notNullValue())
          .body("totalPages", notNullValue())
          .body("pageSize", notNullValue())
          .body("currentPage", notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("aceita filtro por tier e retorna 200")
    void listWithTierFilter_returns200() {
      given()
          .queryParam("tier", "GOLD")
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(200)
          .body("status", is("OK"))
          .body("data", notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("aceita paginação page e size e retorna 200")
    void listWithPagination_returns200() {
      given()
          .queryParam("page", 1)
          .queryParam("size", 5)
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(200)
          .body("status", is("OK"))
          .body("currentPage", is(1))
          .body("pageSize", greaterThanOrEqualTo(0))
          .body("data.size()", greaterThanOrEqualTo(0));
    }
  }

  @Nested
  @DisplayName("POST /v1/admin/sponsor - Criação de patrocinador")
  class CreateSponsor {

    @Test
    @DisplayName("retorna 401 quando não autenticado")
    void createWithoutAuth_returns401() {
      given()
          .contentType(ContentType.JSON)
          .body(sponsorCreatePayload("novo-sponsor-list@test.com", "SP001"))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 201 e dados do patrocinador quando payload válido")
    void createWithValidPayload_returns201AndSponsor() {
      String email = "sponsor-criacao-" + System.currentTimeMillis() + "@test.com";
      String code = "S" + String.format("%04d", (int) (Math.random() * 9999));

      given()
          .contentType(ContentType.JSON)
          .body(sponsorCreatePayload(email, code))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201)
          .body("status", is("OK"))
          .body("statusCode", is(201))
          .body("data.id", notNullValue())
          .body("data.publicName", is("Patrocinador Public Name"))
          .body("data.tier", is("GOLD"))
          .body("data.entityType", is("COMPANY"))
          .body("data.persona", is("OTHER"))
          .body("data.active", is(true))
          .body("data.user", notNullValue())
          .body("data.user.email", is(email));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 400 quando dados do usuário ausentes")
    void createWithoutUser_returns400() {
      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "publicName", "Nome",
              "tier", "BRONZE",
              "entityType", "PERSON",
              "persona", "OTHER"
          ))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 400 quando código não tem 5 caracteres")
    void createWithInvalidCodeLength_returns400() {
      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "user", Map.of(
                  "email", "invalid-code@test.com",
                  "name", "Nome",
                  "document", "12345678901",
                  "code", "AB",
                  "type", "SPONSOR"
              ),
              "publicName", "Nome",
              "tier", "BRONZE",
              "entityType", "PERSON",
              "persona", "OTHER"
          ))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 400 quando tier inválido")
    void createWithInvalidTier_returns400() {
      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "user", Map.of(
                  "email", "invalid-tier@test.com",
                  "name", "Nome",
                  "document", "12345678901",
                  "code", "IT001",
                  "type", "SPONSOR"
              ),
              "publicName", "Nome",
              "tier", "INVALID_TIER",
              "entityType", "PERSON",
              "persona", "OTHER"
          ))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 409 quando código já existe")
    void createWithDuplicateCode_returns409() {
      String email1 = "sponsor-dup-code-1-" + System.currentTimeMillis() + "@test.com";
      String email2 = "sponsor-dup-code-2-" + System.currentTimeMillis() + "@test.com";
      String code = "D" + String.format("%04d", (int) (Math.random() * 9999));

      given()
          .contentType(ContentType.JSON)
          .body(sponsorCreatePayload(email1, code))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201);

      given()
          .contentType(ContentType.JSON)
          .body(sponsorCreatePayload(email2, code))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(409)
          .body("status", is("ERROR"))
          .body("message", is("Já existe um usuário com o código informado."));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 409 quando e-mail já existe")
    void createWithDuplicateEmail_returns409() {
      String email = "sponsor-dup-email-" + System.currentTimeMillis() + "@test.com";
      String code1 = "E" + String.format("%04d", (int) (Math.random() * 9999));
      String code2 = "F" + String.format("%04d", (int) (Math.random() * 9999));

      given()
          .contentType(ContentType.JSON)
          .body(sponsorCreatePayload(email, code1))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201);

      given()
          .contentType(ContentType.JSON)
          .body(sponsorCreatePayload(email, code2))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(409)
          .body("status", is("ERROR"))
          .body("message", is("Já existe um usuário com o e-mail informado."));
    }
  }
}
