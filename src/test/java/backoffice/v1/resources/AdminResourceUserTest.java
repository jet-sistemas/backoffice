package backoffice.v1.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AdminResourceUserTest {

  private static final String BASE_PATH = "/v1/admin/user";

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
    return sponsorUserPayload(email, code, uniqueDocument());
  }

  private static Map<String, Object> sponsorUserPayload(String email, String code, String document) {
    return sponsorUserPayload(email, code, document, "Patrocinador Public Name");
  }

  private static Map<String, Object> sponsorUserPayload(String email, String code, String document,
      String publicName) {
    var payload = new HashMap<String, Object>();
    payload.put("user", Map.of(
        "email", email,
        "name", "Patrocinador Teste",
        "document", document,
        "code", code,
        "type", "SPONSOR"
    ));
    payload.put("sponsor", Map.of(
        "publicName", publicName,
        "tier", "GOLD",
        "entityType", "COMPANY",
        "persona", "OTHER"
    ));
    return payload;
  }

  private static Map<String, Object> admUserPayload(String email, String code) {
    var payload = new HashMap<String, Object>();
    payload.put("user", Map.of(
        "email", email,
        "name", "Admin Teste",
        "document", uniqueDocument(),
        "code", code,
        "type", "ADM"
    ));
    return payload;
  }

  private static Map<String, Object> memberUserPayload(String email, String code) {
    var payload = new HashMap<String, Object>();
    payload.put("user", Map.of(
        "email", email,
        "name", "Membro Teste",
        "document", uniqueDocument(),
        "code", code,
        "type", "MEMBER"));
    var member = new HashMap<String, Object>();
    member.put("fullname", "Membro Teste Completo");
    member.put("whatsapp", "119" + String.format("%09d", Math.abs(System.nanoTime() % 1_000_000_000L)));
    member.put("type", "SUBSCRIBER");
    member.put("subscriber", Map.of(
        "monthlyFeeAmount", 50.00,
        "billingDay", 10));
    payload.put("member", member);
    return payload;
  }

  @Nested
  @DisplayName("GET /v1/admin/user - Listagem de usuários")
  class ListUsers {

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
    @DisplayName("aceita filtro por type e retorna 200")
    void listWithTypeFilter_returns200() {
      given()
          .queryParam("type", "SPONSOR")
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

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 400 quando search excede o tamanho máximo")
    void listWithSearchTooLong_returns400() {
      String tooLong = "x".repeat(101);
      given()
          .queryParam("search", tooLong)
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("aceita parâmetro search com type=SPONSOR e retorna 200")
    void listWithSearchParam_returns200() {
      given()
          .queryParam("type", "SPONSOR")
          .queryParam("search", "nom")
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(200)
          .body("status", is("OK"))
          .body("data", notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("search encontra patrocinador criado por nome público, documento e código")
    void listSearch_findsCreatedSponsorByPublicNameDocumentAndCode() {
      String marker = "SrchMrk" + (System.currentTimeMillis() % 1_000_000);
      String publicName = "Empresa " + marker;
      String document = uniqueDocument();
      String code = "K" + String.format("%04d", (int) (System.currentTimeMillis() % 10000));
      String email = uniqueEmail("search-sponsor");

      int userId = given()
          .contentType(ContentType.JSON)
          .body(sponsorUserPayload(email, code, document, publicName))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201)
          .extract()
          .path("data.id");

      given()
          .queryParam("type", "SPONSOR")
          .queryParam("search", marker)
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(200)
          .body("data.id", hasItem(userId));

      String docTail = document.substring(Math.max(0, document.length() - 6));
      given()
          .queryParam("type", "SPONSOR")
          .queryParam("search", docTail)
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(200)
          .body("data.id", hasItem(userId));

      given()
          .queryParam("type", "SPONSOR")
          .queryParam("search", code)
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(200)
          .body("data.id", hasItem(userId));

      List<Integer> idsUnrelated = given()
          .queryParam("type", "SPONSOR")
          .queryParam("search", "Qy7vBw9kLm2NoHit" + marker)
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(200)
          .extract()
          .path("data.id");
      assertFalse(idsUnrelated.contains(userId));
    }
  }

  @Nested
  @DisplayName("POST /v1/admin/user - Criação de usuário")
  class CreateUser {

    @Test
    @DisplayName("retorna 401 quando não autenticado")
    void createWithoutAuth_returns401() {
      given()
          .contentType(ContentType.JSON)
          .body(sponsorUserPayload("novo-sponsor@test.com", "SP001"))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 201 e dados do usuário sponsor quando payload válido")
    void createSponsorUser_returns201() {
      String email = uniqueEmail("sponsor-create");
      String code = uniqueCode();

      given()
          .contentType(ContentType.JSON)
          .body(sponsorUserPayload(email, code))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201)
          .body("status", is("OK"))
          .body("statusCode", is(201))
          .body("data.id", notNullValue())
          .body("data.email", is(email))
          .body("data.type", is("SPONSOR"))
          .body("data.sponsor", notNullValue())
          .body("data.sponsor.publicName", is("Patrocinador Public Name"))
          .body("data.sponsor.tier", is("GOLD"))
          .body("data.sponsor.entityType", is("COMPANY"));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 201 e dados do usuário ADM sem sponsor")
    void createAdmUser_returns201() {
      String email = uniqueEmail("adm-create");
      String code = uniqueCode();

      given()
          .contentType(ContentType.JSON)
          .body(admUserPayload(email, code))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201)
          .body("status", is("OK"))
          .body("statusCode", is(201))
          .body("data.id", notNullValue())
          .body("data.email", is(email))
          .body("data.type", is("ADM"))
          .body("data.sponsor", nullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 400 quando dados do usuário ausentes")
    void createWithoutUser_returns400() {
      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "sponsor", Map.of(
                  "publicName", "Nome",
                  "tier", "BRONZE",
                  "entityType", "PERSON",
                  "persona", "OTHER"
              )
          ))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 400 quando type=SPONSOR e sponsor ausente")
    void createSponsorWithoutSponsorData_returns400() {
      var payload = new HashMap<String, Object>();
      payload.put("user", Map.of(
          "email", uniqueEmail("no-sponsor-data"),
          "name", "Nome",
          "document", uniqueDocument(),
          "code", uniqueCode(),
          "type", "SPONSOR"
      ));

      given()
          .contentType(ContentType.JSON)
          .body(payload)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 400 quando código não tem 5 caracteres")
    void createWithInvalidCodeLength_returns400() {
      var payload = new HashMap<String, Object>();
      payload.put("user", Map.of(
          "email", "invalid-code@test.com",
          "name", "Nome",
          "document", "12345678901",
          "code", "AB",
          "type", "SPONSOR"
      ));
      payload.put("sponsor", Map.of(
          "publicName", "Nome",
          "tier", "BRONZE",
          "entityType", "PERSON",
          "persona", "OTHER"
      ));

      given()
          .contentType(ContentType.JSON)
          .body(payload)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 400 quando tier inválido")
    void createWithInvalidTier_returns400() {
      var payload = new HashMap<String, Object>();
      payload.put("user", Map.of(
          "email", uniqueEmail("invalid-tier"),
          "name", "Nome",
          "document", uniqueDocument(),
          "code", uniqueCode(),
          "type", "SPONSOR"
      ));
      payload.put("sponsor", Map.of(
          "publicName", "Nome",
          "tier", "INVALID_TIER",
          "entityType", "PERSON",
          "persona", "OTHER"
      ));

      given()
          .contentType(ContentType.JSON)
          .body(payload)
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 409 quando código já existe")
    void createWithDuplicateCode_returns409() {
      String code = uniqueCode();

      given()
          .contentType(ContentType.JSON)
          .body(sponsorUserPayload(uniqueEmail("dup-code-1"), code))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201);

      given()
          .contentType(ContentType.JSON)
          .body(sponsorUserPayload(uniqueEmail("dup-code-2"), code))
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
      String email = uniqueEmail("dup-email");

      given()
          .contentType(ContentType.JSON)
          .body(sponsorUserPayload(email, uniqueCode()))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201);

      given()
          .contentType(ContentType.JSON)
          .body(sponsorUserPayload(email, uniqueCode()))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(409)
          .body("status", is("ERROR"))
          .body("message", is("Já existe um usuário com o e-mail informado."));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 201 quando type=MEMBER com dados de membro válidos")
    void createMemberUser_returns201() {
      String email = uniqueEmail("member-create");
      String code = uniqueCode();
      given()
          .contentType(ContentType.JSON)
          .body(memberUserPayload(email, code))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201)
          .body("status", is("OK"))
          .body("data.email", is(email))
          .body("data.type", is("MEMBER"))
          .body("data.member", notNullValue());
    }
  }

  @Nested
  @DisplayName("PUT /v1/admin/user/{id} - Atualização de usuário")
  class UpdateUser {

    @Test
    @DisplayName("retorna 401 quando não autenticado")
    void updateWithoutAuth_returns401() {
      given()
          .contentType(ContentType.JSON)
          .body(Map.of("name", "Novo Nome"))
          .when()
          .put(BASE_PATH + "/1")
          .then()
          .statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 200 ao atualizar nome do usuário sponsor")
    void updateSponsorUserName_returns200() {
      String email = uniqueEmail("update-sponsor");
      String code = uniqueCode();

      Long userId = given()
          .contentType(ContentType.JSON)
          .body(sponsorUserPayload(email, code))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      given()
          .contentType(ContentType.JSON)
          .body(Map.of("name", "Nome Atualizado"))
          .when()
          .put(BASE_PATH + "/" + userId)
          .then()
          .statusCode(200)
          .body("status", is("OK"))
          .body("data.name", is("Nome Atualizado"))
          .body("data.sponsor", notNullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 200 ao atualizar dados do sponsor junto com user")
    void updateSponsorData_returns200() {
      String email = uniqueEmail("update-sponsor-data");
      String code = uniqueCode();

      Long userId = given()
          .contentType(ContentType.JSON)
          .body(sponsorUserPayload(email, code))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      var updatePayload = new HashMap<String, Object>();
      updatePayload.put("name", "Nome Atualizado");
      updatePayload.put("sponsor", Map.of(
          "publicName", "Novo Public Name",
          "tier", "SILVER"
      ));

      given()
          .contentType(ContentType.JSON)
          .body(updatePayload)
          .when()
          .put(BASE_PATH + "/" + userId)
          .then()
          .statusCode(200)
          .body("status", is("OK"))
          .body("data.name", is("Nome Atualizado"))
          .body("data.sponsor.publicName", is("Novo Public Name"))
          .body("data.sponsor.tier", is("SILVER"));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 404 quando usuário não existe")
    void updateNonExistentUser_returns404() {
      given()
          .contentType(ContentType.JSON)
          .body(Map.of("name", "Novo Nome"))
          .when()
          .put(BASE_PATH + "/999999")
          .then()
          .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 200 ao atualizar apenas dados do membro (PUT parcial)")
    void updateMemberUserOnlyMemberData_returns200() {
      String email = uniqueEmail("update-member-profile");
      String code = uniqueCode();

      Long userId = given()
          .contentType(ContentType.JSON)
          .body(memberUserPayload(email, code))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      String newWhatsapp = "11" + String.format("%09d", ThreadLocalRandom.current().nextInt(0, 1_000_000_000));

      given()
          .contentType(ContentType.JSON)
          .body(Map.of("member", Map.of(
              "fullname", "Nome Completo Atualizado",
              "whatsapp", newWhatsapp)))
          .when()
          .put(BASE_PATH + "/" + userId)
          .then()
          .statusCode(200)
          .body("status", is("OK"))
          .body("data.member.fullname", is("Nome Completo Atualizado"))
          .body("data.member.whatsapp", is(newWhatsapp));
    }
  }

  @Nested
  @DisplayName("PATCH /v1/admin/user/{id}/activate e /deactivate")
  class ActivateDeactivateUser {

    private static final String BENEFIT_PATH = "/v1/admin/benefit";

    @Test
    @DisplayName("activate retorna 401 quando não autenticado")
    void activateWithoutAuth_returns401() {
      given()
          .when()
          .patch(BASE_PATH + "/1/activate")
          .then()
          .statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("activate retorna 404 quando usuário não existe")
    void activateNonExistentUser_returns404() {
      given()
          .when()
          .patch(BASE_PATH + "/999999/activate")
          .then()
          .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("desativar e reativar usuário ADM restaura conta ativa")
    void deactivateThenActivateAdmUser_restoresAccount() {
      String email = uniqueEmail("adm-activate");
      String code = uniqueCode();

      Long userId = given()
          .contentType(ContentType.JSON)
          .body(admUserPayload(email, code))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      given()
          .when()
          .patch(BASE_PATH + "/" + userId + "/deactivate")
          .then()
          .statusCode(200)
          .body("status", is("OK"));

      given()
          .when()
          .get(BASE_PATH + "/" + userId)
          .then()
          .statusCode(200)
          .body("data.accountActive", is(false));

      given()
          .when()
          .patch(BASE_PATH + "/" + userId + "/activate")
          .then()
          .statusCode(200)
          .body("status", is("OK"));

      given()
          .when()
          .get(BASE_PATH + "/" + userId)
          .then()
          .statusCode(200)
          .body("data.accountActive", is(true));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("desativar e reativar patrocinador restaura conta, sponsor e benefícios")
    void deactivateThenActivateSponsorUser_restoresSponsorAndBenefits() {
      String email = uniqueEmail("sponsor-activate");
      String code = uniqueCode();

      Long userId = given()
          .contentType(ContentType.JSON)
          .body(sponsorUserPayload(email, code))
          .when()
          .post(BASE_PATH)
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      Long sponsorId = given()
          .when()
          .get(BASE_PATH + "/" + userId)
          .then()
          .statusCode(200)
          .extract()
          .jsonPath()
          .getLong("data.sponsor.id");

      Long benefitId = given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "name", "Benefício ciclo ativar",
              "description", "Teste",
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
          .patch(BASE_PATH + "/" + userId + "/deactivate")
          .then()
          .statusCode(200);

      given()
          .when()
          .get(BASE_PATH + "/" + userId)
          .then()
          .statusCode(200)
          .body("data.accountActive", is(false))
          .body("data.sponsor.active", is(false));

      given()
          .when()
          .get(BENEFIT_PATH + "/" + benefitId)
          .then()
          .statusCode(200)
          .body("data.active", is(false));

      given()
          .when()
          .patch(BASE_PATH + "/" + userId + "/activate")
          .then()
          .statusCode(200);

      given()
          .when()
          .get(BASE_PATH + "/" + userId)
          .then()
          .statusCode(200)
          .body("data.accountActive", is(true))
          .body("data.sponsor.active", is(true));

      given()
          .when()
          .get(BENEFIT_PATH + "/" + benefitId)
          .then()
          .statusCode(200)
          .body("data.active", is(true));
    }
  }
}
