package backoffice.v1.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import backoffice.common.utils.TokenUtils;
import backoffice.v1.entities.User;
import backoffice.v1.services.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AdminResourceMemberTest {
  private static final String USER_PATH = "/v1/admin/user";

  @Inject
  UserService userService;

  private static String uniqueDocument() {
    long t = System.currentTimeMillis() % 100_000_000_000L;
    int r = ThreadLocalRandom.current().nextInt(0, 1_000_000);
    return "1" + String.format("%010d", (t + r) % 10_000_000_000L);
  }

  private static String uniqueEmail(String prefix) {
    return prefix + "-" + System.currentTimeMillis() + "@test.com";
  }

  private static String uniqueCode() {
    int r = ThreadLocalRandom.current().nextInt(0, 10_000);
    long t = System.currentTimeMillis() % 10_000;
    return "M" + String.format("%04d", (r + t) % 10_000);
  }

  private static String uniqueWhatsapp() {
    int suffix = ThreadLocalRandom.current().nextInt(100_000_000, 1_000_000_000);
    return "119" + suffix;
  }

  private static Map<String, Object> subscriberMember(String whatsapp) {
    var m = new HashMap<String, Object>();
    m.put("fullname", "Membro Teste Completo");
    m.put("whatsapp", whatsapp);
    m.put("type", "SUBSCRIBER");
    m.put("subscriber", Map.of(
        "monthlyFeeAmount", 50.00,
        "billingDay", 10));
    return m;
  }

  private static Map<String, Object> memberPayload(String email, String code, String whatsapp) {
    var payload = new HashMap<String, Object>();
    payload.put("user", Map.of(
        "email", email,
        "name", "Membro Teste",
        "document", uniqueDocument(),
        "code", code,
        "type", "MEMBER"));
    payload.put("member", subscriberMember(whatsapp));
    return payload;
  }

  private static Map<String, Object> adminUserPayload(String email, String code) {
    var payload = new HashMap<String, Object>();
    payload.put("user", Map.of(
        "email", email,
        "name", "Admin Teste",
        "document", uniqueDocument(),
        "code", code,
        "type", "ADM"));
    return payload;
  }

  private String bearerTokenForUserId(long userId) {
    User user = userService.findById(userId)
        .orElseThrow(() -> new AssertionError("Usuário não encontrado: " + userId));
    return TokenUtils.generateToken(user);
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("POST /user cria MEMBER assinante e retorna member.subscriber no corpo")
  void createMemberUser_returns201() {
    String email = uniqueEmail("member-resource-create");
    String code = uniqueCode();
    given()
        .contentType(ContentType.JSON)
        .body(memberPayload(email, code, uniqueWhatsapp()))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(201)
        .body("status", is("OK"))
        .body("statusCode", is(201))
        .body("data.id", notNullValue())
        .body("data.email", is(email))
        .body("data.type", is("MEMBER"))
        .body("data.member", notNullValue())
        .body("data.member.type", is("SUBSCRIBER"))
        .body("data.member.subscriber", notNullValue())
        .body("data.member.subscriber.billingDay", is(10));
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("GET /user?type=MEMBER lista com paginação e metadados")
  void listMembers_returns200() {
    given()
        .queryParam("type", "MEMBER")
        .queryParam("memberType", "SUBSCRIBER")
        .queryParam("page", 1)
        .queryParam("size", 10)
        .when()
        .get(USER_PATH)
        .then()
        .statusCode(200)
        .body("status", is("OK"))
        .body("data", notNullValue())
        .body("totalElements", notNullValue())
        .body("totalPages", notNullValue())
        .body("pageSize", greaterThanOrEqualTo(0))
        .body("currentPage", is(1));
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("GET /user/{userId} retorna membro embutido")
  void findMemberUserById_returns200() {
    String email = uniqueEmail("member-resource-find");
    String code = uniqueCode();
    Integer userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(email, code, uniqueWhatsapp()))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    given()
        .when()
        .get(USER_PATH + "/" + userId)
        .then()
        .statusCode(200)
        .body("status", is("OK"))
        .body("data.id", is(userId))
        .body("data.email", is(email))
        .body("data.member", notNullValue())
        .body("data.member.subscriber", notNullValue());
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("retorna 409 quando whatsapp do membro já existe")
  void createDuplicateWhatsapp_returns409() {
    String whatsapp = uniqueWhatsapp();
    given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("member-dup-1"), uniqueCode(), whatsapp))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("member-dup-2"), uniqueCode(), whatsapp))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(409)
        .body("status", is("ERROR"));
  }

  @Nested
  @DisplayName("Casos negativos e fluxo patrocinado")
  class NegativesAndSponsored {

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 400 quando SUBSCRIBER sem bloco subscriber")
    void createSubscriberWithoutBlock_returns400() {
      var m = new HashMap<String, Object>();
      m.put("fullname", "X");
      m.put("whatsapp", uniqueWhatsapp());
      m.put("type", "SUBSCRIBER");
      var payload = new HashMap<String, Object>();
      payload.put("user", Map.of(
          "email", uniqueEmail("no-sub"),
          "name", "N",
          "document", uniqueDocument(),
          "code", uniqueCode(),
          "type", "MEMBER"));
      payload.put("member", m);

      given()
          .contentType(ContentType.JSON)
          .body(payload)
          .when()
          .post(USER_PATH)
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("GET /user/{id} com usuário inexistente retorna data nulo")
    void findUserById_whenMissing_returns200WithNullData() {
      given()
          .when()
          .get(USER_PATH + "/999999999")
          .then()
          .statusCode(200)
          .body("data", nullValue());
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("cria membro SPONSORED concedido pelo ADM autenticado (JWT)")
    void createSponsoredMember_returns201() {
      String adminEmail = uniqueEmail("admin-for-sponsored");
      String adminCode = "A" + String.format("%04d", (int) (Math.random() * 9999));
      int adminUserId = given()
          .contentType(ContentType.JSON)
          .body(adminUserPayload(adminEmail, adminCode))
          .when()
          .post(USER_PATH)
          .then()
          .statusCode(201)
          .extract()
          .path("data.id");

      var sponsored = new HashMap<String, Object>();
      sponsored.put("startAt", LocalDate.now().toString());
      var m = new HashMap<String, Object>();
      m.put("fullname", "Patrocinado Teste");
      m.put("whatsapp", uniqueWhatsapp());
      m.put("type", "SPONSORED");
      m.put("sponsored", sponsored);
      var payload = new HashMap<String, Object>();
      payload.put("user", Map.of(
          "email", uniqueEmail("sponsored-m"),
          "name", "Membro P",
          "document", uniqueDocument(),
          "code", uniqueCode(),
          "type", "MEMBER"));
      payload.put("member", m);

      given()
          .contentType(ContentType.JSON)
          .auth().oauth2(bearerTokenForUserId(adminUserId))
          .body(payload)
          .when()
          .post(USER_PATH)
          .then()
          .statusCode(201)
          .body("data.member.type", is("SPONSORED"))
          .body("data.member.sponsored", notNullValue())
          .body("data.member.sponsored.grantedByUserId", is(adminUserId))
          .body("data.member.sponsored.grantedByUser.id", is(adminUserId))
          .body("data.member.sponsored.grantedByUser.email", is(adminEmail))
          .body("data.member.sponsored.grantedByUser.name", is("Admin Teste"))
          .body("data.member.sponsored.grantedByUser.type", is("ADM"));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("retorna 400 ao criar SPONSORED sem identificação do ADM no JWT")
    void createSponsoredMember_withoutJwtActor_returns400() {
      var sponsored = new HashMap<String, Object>();
      sponsored.put("startAt", LocalDate.now().toString());
      var m = new HashMap<String, Object>();
      m.put("fullname", "Patrocinado Sem Actor");
      m.put("whatsapp", uniqueWhatsapp());
      m.put("type", "SPONSORED");
      m.put("sponsored", sponsored);
      var payload = new HashMap<String, Object>();
      payload.put("user", Map.of(
          "email", uniqueEmail("sponsored-no-actor"),
          "name", "Membro Sem Actor",
          "document", uniqueDocument(),
          "code", uniqueCode(),
          "type", "MEMBER"));
      payload.put("member", m);

      given()
          .contentType(ContentType.JSON)
          .body(payload)
          .when()
          .post(USER_PATH)
          .then()
          .statusCode(400)
          .body("status", is("ERROR"));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("PATCH /user/{userId}/subscriber atualiza mensalidade")
    void patchSubscriber_returns200() {
      Integer userId = given()
          .contentType(ContentType.JSON)
          .body(memberPayload(uniqueEmail("patch-sub"), uniqueCode(), uniqueWhatsapp()))
          .when()
          .post(USER_PATH)
          .then()
          .statusCode(201)
          .extract()
          .path("data.id");

      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "monthlyFeeAmount", 99.99,
              "billingDay", 15))
          .when()
          .patch(USER_PATH + "/" + userId + "/subscriber")
          .then()
          .statusCode(200)
          .body("data.subscriber.billingDay", is(15));
    }
  }
}
