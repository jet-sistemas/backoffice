package backoffice.v1.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AdminResourceMemberTest {
  private static final String BASE_PATH = "/v1/admin/member";

  private static String uniqueDocument() {
    long n = Math.abs(System.nanoTime() % 1_000_000_000L);
    return "1" + String.format("%010d", n);
  }

  private static String uniqueEmail(String prefix) {
    return prefix + "-" + System.currentTimeMillis() + "@test.com";
  }

  private static String uniqueCode() {
    return "M" + String.format("%04d", (int) (Math.random() * 9999));
  }

  private static Map<String, Object> memberPayload(String email, String code, String whatsapp) {
    var payload = new HashMap<String, Object>();
    payload.put("user", Map.of(
        "email", email,
        "name", "Membro Teste",
        "document", uniqueDocument(),
        "code", code,
        "type", "MEMBER"));
    payload.put("member", Map.of(
        "fullname", "Membro Teste Completo",
        "whatsapp", whatsapp,
        "type", "SUBSCRIBER"));
    return payload;
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("cria membro com sucesso e responde envelope padronizado")
  void createMember_returns201() {
    String email = uniqueEmail("member-resource-create");
    String code = uniqueCode();
    given()
        .contentType(ContentType.JSON)
        .body(memberPayload(email, code, "11911112222"))
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .body("status", is("OK"))
        .body("statusCode", is(201))
        .body("data.id", notNullValue())
        .body("data.email", is(email))
        .body("data.type", is("SUBSCRIBER"));
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("lista membros com paginação e filtros")
  void listMembers_returns200() {
    given()
        .queryParam("type", "SUBSCRIBER")
        .queryParam("page", 1)
        .queryParam("size", 10)
        .when()
        .get(BASE_PATH)
        .then()
        .statusCode(200)
        .body("status", is("OK"))
        .body("data", notNullValue())
        .body("totalElements", notNullValue())
        .body("totalPages", notNullValue());
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("busca membro por id")
  void findMemberById_returns200() {
    String email = uniqueEmail("member-resource-find");
    String code = uniqueCode();
    Integer memberId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(email, code, "11922223333"))
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    given()
        .when()
        .get(BASE_PATH + "/" + memberId)
        .then()
        .statusCode(200)
        .body("status", is("OK"))
        .body("data.id", is(memberId))
        .body("data.email", is(email));
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("retorna 409 quando whatsapp do membro já existe")
  void createDuplicateWhatsapp_returns409() {
    String whatsapp = "11933334444";
    given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("member-dup-1"), uniqueCode(), whatsapp))
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("member-dup-2"), uniqueCode(), whatsapp))
        .when()
        .post(BASE_PATH)
        .then()
        .statusCode(409);
  }
}
