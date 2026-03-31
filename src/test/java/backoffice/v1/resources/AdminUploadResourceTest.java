package backoffice.v1.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import backoffice.common.services.StorageService;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;

@QuarkusTest
class AdminUploadResourceTest {

  private static final String BASE = "/v1/admin/uploads";

  private StorageService storageService;

  @BeforeEach
  void stubStorage() {
    storageService = Mockito.mock(StorageService.class);
    lenient().when(storageService.isConfigured()).thenReturn(true);
    lenient().when(storageService.presignPut(anyString(), anyString(), any(Duration.class)))
        .thenReturn("https://r2.example/presigned-put");
    lenient().when(storageService.objectExists(anyString())).thenReturn(true);
    lenient().when(storageService.keyMatchesEntityPrefix(anyString(), anyString()))
        .thenAnswer(invocation -> {
          String key = invocation.getArgument(0);
          String prefix = invocation.getArgument(1);
          return key != null && prefix != null && key.startsWith(prefix);
        });
    QuarkusMock.installMockForType(storageService, StorageService.class);
  }

  private static String uniqueEmail() {
    return "upload-test-" + System.currentTimeMillis() + "@test.com";
  }

  private static String uniqueDocument() {
    long n = Math.abs(System.nanoTime() % 1_000_000_000L);
    return "2" + String.format("%010d", n);
  }

  private static String uniqueCode() {
    return "U" + String.format("%04d", (int) (Math.random() * 9999));
  }

  private static Map<String, Object> admUserPayload() {
    var payload = new HashMap<String, Object>();
    payload.put("user", Map.of(
        "email", uniqueEmail(),
        "name", "Admin Upload Test",
        "document", uniqueDocument(),
        "code", uniqueCode(),
        "type", "ADM"));
    return payload;
  }

  private static Map<String, Object> sponsorUserPayload() {
    var payload = new HashMap<String, Object>();
    payload.put("user", Map.of(
        "email", uniqueEmail(),
        "name", "Sponsor Upload Test",
        "document", uniqueDocument(),
        "code", uniqueCode(),
        "type", "SPONSOR"));
    payload.put("sponsor", Map.of(
        "publicName", "Sponsor Public",
        "tier", "BRONZE",
        "entityType", "COMPANY",
        "persona", "OTHER"));
    return payload;
  }

  @Nested
  @DisplayName("POST /v1/admin/uploads/init")
  class Init {

    @Test
    @DisplayName("401 sem autenticação")
    void withoutAuth_returns401() {
      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", 1,
              "contentType", "image/png",
              "size", 1000))
          .when()
          .post(BASE + "/init")
          .then()
          .statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("415 quando content type não permitido")
    void invalidContentType_returns415() {
      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", 1,
              "contentType", "application/pdf",
              "size", 1000))
          .when()
          .post(BASE + "/init")
          .then()
          .statusCode(415)
          .body("status", is("ERROR"));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("413 quando tamanho excede o máximo")
    void sizeTooLarge_returns413() {
      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", 1,
              "contentType", "image/png",
              "size", 99_000_000))
          .when()
          .post(BASE + "/init")
          .then()
          .statusCode(413)
          .body("status", is("ERROR"));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("503 quando storage não configurado")
    void storageNotConfigured_returns503() {
      when(storageService.isConfigured()).thenReturn(false);
      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", 1,
              "contentType", "image/png",
              "size", 1000))
          .when()
          .post(BASE + "/init")
          .then()
          .statusCode(503)
          .body("status", is("ERROR"));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("404 quando usuário não existe")
    void userNotFound_returns404() {
      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", 999999999,
              "contentType", "image/png",
              "size", 1000))
          .when()
          .post(BASE + "/init")
          .then()
          .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("200 retorna objectKey e uploadUrl para avatar")
    void initAvatar_returns200() {
      Long userId = given()
          .contentType(ContentType.JSON)
          .body(admUserPayload())
          .when()
          .post("/v1/admin/user")
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", userId,
              "fileName", "foto.png",
              "contentType", "image/png",
              "size", 2048))
          .when()
          .post(BASE + "/init")
          .then()
          .statusCode(200)
          .body("status", is("OK"))
          .body("data.objectKey", notNullValue())
          .body("data.uploadUrl", is("https://r2.example/presigned-put"))
          .body("data.expiresIn", notNullValue());

      verify(storageService).presignPut(anyString(), anyString(), any(Duration.class));
    }
  }

  @Nested
  @DisplayName("POST /v1/admin/uploads/confirm")
  class Confirm {

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("400 quando objectKey não pertence ao prefixo da entidade")
    void invalidKey_returns400() {
      Long userId = given()
          .contentType(ContentType.JSON)
          .body(admUserPayload())
          .when()
          .post("/v1/admin/user")
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", userId,
              "objectKey", "outro/prefixo/avatar.png"))
          .when()
          .post(BASE + "/confirm")
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("404 quando objeto não existe no bucket")
    void objectMissing_returns404() {
      Long userId = given()
          .contentType(ContentType.JSON)
          .body(admUserPayload())
          .when()
          .post("/v1/admin/user")
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      String objectKey = given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", userId,
              "contentType", "image/jpeg",
              "size", 1000))
          .when()
          .post(BASE + "/init")
          .then()
          .statusCode(200)
          .extract()
          .path("data.objectKey");

      when(storageService.objectExists(objectKey)).thenReturn(false);

      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", userId,
              "objectKey", objectKey))
          .when()
          .post(BASE + "/confirm")
          .then()
          .statusCode(404);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("200 persiste avatar_url")
    void confirmAvatar_returns200() {
      Long userId = given()
          .contentType(ContentType.JSON)
          .body(admUserPayload())
          .when()
          .post("/v1/admin/user")
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      String objectKey = given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", userId,
              "contentType", "image/webp",
              "size", 500))
          .when()
          .post(BASE + "/init")
          .then()
          .statusCode(200)
          .extract()
          .path("data.objectKey");

      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", userId,
              "objectKey", objectKey))
          .when()
          .post(BASE + "/confirm")
          .then()
          .statusCode(200)
          .body("data.objectKey", is(objectKey))
          .body("data.entity", is("user"));

      given()
          .when()
          .get("/v1/admin/user/" + userId)
          .then()
          .statusCode(200)
          .body("data.avatarUrl", is(objectKey));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("200 persiste logo_url para sponsor")
    void confirmSponsorLogo_returns200() {
      var created = given()
          .contentType(ContentType.JSON)
          .body(sponsorUserPayload())
          .when()
          .post("/v1/admin/user")
          .then()
          .statusCode(201)
          .extract()
          .jsonPath();
      Long sponsorId = created.getLong("data.sponsor.id");
      Long userId = created.getLong("data.id");

      String objectKey = given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "sponsor",
              "entityId", sponsorId,
              "contentType", "image/png",
              "size", 800))
          .when()
          .post(BASE + "/init")
          .then()
          .statusCode(200)
          .body("data.objectKey", containsString("/sponsors/logo/"))
          .extract()
          .path("data.objectKey");

      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "sponsor",
              "entityId", sponsorId,
              "objectKey", objectKey))
          .when()
          .post(BASE + "/confirm")
          .then()
          .statusCode(200)
          .body("data.entity", is("sponsor"));

      given()
          .when()
          .get("/v1/admin/user/" + userId)
          .then()
          .statusCode(200)
          .body("data.sponsor.logoUrl", is(objectKey));
    }
  }

  @Nested
  @DisplayName("DELETE /v1/admin/uploads")
  class DeleteUpload {

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("200 idempotente quando não há imagem")
    void noImage_returns200() {
      Long userId = given()
          .contentType(ContentType.JSON)
          .body(admUserPayload())
          .when()
          .post("/v1/admin/user")
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", userId,
              "objectKey", "dev/users/avatar/" + userId + "/2026/03/x_y.png"))
          .when()
          .delete(BASE)
          .then()
          .statusCode(200)
          .body("data.success", is(true));
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("400 quando chave não coincide com a atual")
    void keyMismatch_returns400() {
      Long userId = given()
          .contentType(ContentType.JSON)
          .body(admUserPayload())
          .when()
          .post("/v1/admin/user")
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      String objectKey = given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", userId,
              "contentType", "image/png",
              "size", 100))
          .when()
          .post(BASE + "/init")
          .then()
          .statusCode(200)
          .extract()
          .path("data.objectKey");

      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", userId,
              "objectKey", objectKey))
          .when()
          .post(BASE + "/confirm")
          .then()
          .statusCode(200);

      String otherKey = "dev/users/avatar/" + userId + "/2099/01/0_deadbeef00.png";

      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", userId,
              "objectKey", otherKey))
          .when()
          .delete(BASE)
          .then()
          .statusCode(400);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("200 remove avatar e chama delete no storage")
    void deleteCurrent_removesAndCallsStorage() {
      Long userId = given()
          .contentType(ContentType.JSON)
          .body(admUserPayload())
          .when()
          .post("/v1/admin/user")
          .then()
          .statusCode(201)
          .extract()
          .jsonPath()
          .getLong("data.id");

      String objectKey = given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", userId,
              "contentType", "image/png",
              "size", 200))
          .when()
          .post(BASE + "/init")
          .then()
          .statusCode(200)
          .extract()
          .path("data.objectKey");

      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", userId,
              "objectKey", objectKey))
          .when()
          .post(BASE + "/confirm")
          .then()
          .statusCode(200);

      given()
          .contentType(ContentType.JSON)
          .body(Map.of(
              "entity", "user",
              "entityId", userId,
              "objectKey", objectKey))
          .when()
          .delete(BASE)
          .then()
          .statusCode(200)
          .body("data.success", is(true));

      verify(storageService).deleteObject(objectKey);

      given()
          .when()
          .get("/v1/admin/user/" + userId)
          .then()
          .statusCode(200)
          .body("data.avatarUrl", nullValue());
    }
  }
}
