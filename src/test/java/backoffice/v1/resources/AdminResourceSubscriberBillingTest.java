package backoffice.v1.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import backoffice.v1.services.MemberBillingService;
import jakarta.inject.Inject;

@QuarkusTest
class AdminResourceSubscriberBillingTest {
  private static final String USER_PATH = "/v1/admin/user";
  private static final String BILLING_PATH = "/v1/admin/subscribers/billing";

  @Inject
  MemberBillingService memberBillingService;

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
    m.put("fullname", "Assinante Billing Test");
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
        "name", "Membro Billing",
        "document", uniqueDocument(),
        "code", code,
        "type", "MEMBER"));
    payload.put("member", subscriberMember(whatsapp));
    return payload;
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("PATCH /user/{id}/subscriber/paid avança vencimento e registra evento")
  void markPaid_returns200AndEvent() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("mark-paid"), uniqueCode(), uniqueWhatsapp()))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    given()
        .contentType(ContentType.JSON)
        .body(Map.of(
            "nextDueDate", "2020-01-10",
            "status", "OVERDUE"))
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber")
        .then()
        .statusCode(200);

    given()
        .contentType(ContentType.JSON)
        .body(Map.of())
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber/paid")
        .then()
        .statusCode(200)
        .body("data.subscriber.status", is("ACTIVE"))
        .body("data.subscriber.lastPaidAt", notNullValue());

    given()
        .queryParam("page", 1)
        .queryParam("size", 10)
        .when()
        .get(USER_PATH + "/" + userId + "/subscriber/events")
        .then()
        .statusCode(200)
        .body("data[0].eventType", is("PAYMENT_MARKED_PAID"));
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("GET /subscribers/billing retorna resumo e linhas")
  void listBilling_returns200() {
    given()
        .queryParam("page", 1)
        .queryParam("size", 5)
        .when()
        .get(BILLING_PATH)
        .then()
        .statusCode(200)
        .body("data.summary.activeCount", notNullValue());
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("refreshBillingStatuses coloca assinante em OVERDUE quando vencido")
  void refreshStatuses_marksOverdue() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("overdue-job"), uniqueCode(), uniqueWhatsapp()))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("nextDueDate", "2020-01-10"))
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber")
        .then()
        .statusCode(200);

    memberBillingService.refreshBillingStatuses();

    given()
        .when()
        .get(USER_PATH + "/" + userId)
        .then()
        .statusCode(200)
        .body("data.member.subscriber.status", is("OVERDUE"));
  }
}
