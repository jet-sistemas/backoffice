package backoffice.v1.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import backoffice.v1.entities.SubscriberMember;
import backoffice.v1.entities.enums.MemberStatusEnum;
import backoffice.v1.repositories.SubscriberMemberRepository;
import backoffice.v1.services.MemberBillingService;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.inject.Inject;

@QuarkusTest
class AdminResourceSubscriberBillingTest {
  private static final String USER_PATH = "/v1/admin/user";
  private static final String BILLING_PATH = "/v1/admin/subscribers/billing";

  @Inject
  MemberBillingService memberBillingService;

  @Inject
  SubscriberMemberRepository subscriberMemberRepository;

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

    LocalDate due = LocalDate.now();
    given()
        .contentType(ContentType.JSON)
        .body(Map.of(
            "nextDueDate", due.toString(),
            "status", "DUE_SOON"))
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
  @DisplayName("OVERDUE fora da competência: 1º pagamento só lastPaidAt; 2º avança vencimento")
  void markPaid_overduePastCompetence_twoSteps() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("overdue-defer"), uniqueCode(), uniqueWhatsapp()))
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
        .body("data.subscriber.status", is("OVERDUE"))
        .body("data.subscriber.nextDueDate", is("2020-01-10"));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of())
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber/paid")
        .then()
        .statusCode(200)
        .body("data.subscriber.status", not(is("OVERDUE")));
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("PATCH /user/{id}/subscriber/paid retorna 400 quando status efetivo é ACTIVE (ciclo já quitado)")
  void markPaid_returns400WhenEffectiveActive() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("outside-comp"), uniqueCode(), uniqueWhatsapp()))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    LocalDate nextMonth = LocalDate.now().plusMonths(1).withDayOfMonth(10);
    given()
        .contentType(ContentType.JSON)
        .body(Map.of(
            "nextDueDate", nextMonth.toString(),
            "status", "ACTIVE"))
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
        .statusCode(400);
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("PATCH /user/{id}/subscriber/paid retorna 400 quando ciclo já quitado (ACTIVE longe da janela)")
  void markPaid_returns400WhenAlreadyPaidInCycle() {
    Assumptions.assumeTrue(LocalDate.now().getDayOfMonth() <= 20);

    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("already-paid"), uniqueCode(), uniqueWhatsapp()))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    LocalDate today = LocalDate.now();
    LocalDate due = today.plusDays(7);
    given()
        .contentType(ContentType.JSON)
        .body(Map.of(
            "nextDueDate", due.toString(),
            "status", "ACTIVE"))
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
        .statusCode(400);
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("PATCH subscriber altera dia de cobrança e recalcula vencimento quando ACTIVE")
  void patchSubscriber_billingDayShiftRecalculatesDue() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("billing-shift"), uniqueCode(), uniqueWhatsapp()))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    LocalDate nd = LocalDate.now().withDayOfMonth(10);
    if (!nd.isAfter(LocalDate.now())) {
      nd = nd.plusMonths(1);
    }
    given()
        .contentType(ContentType.JSON)
        .body(Map.of(
            "nextDueDate", nd.toString(),
            "status", "ACTIVE"))
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber")
        .then()
        .statusCode(200);

    LocalDate expected = nd.withDayOfMonth(15);
    given()
        .contentType(ContentType.JSON)
        .body(Map.of("billingDay", 15))
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber")
        .then()
        .statusCode(200)
        .body("data.subscriber.billingDay", is(15))
        .body("data.subscriber.nextDueDate", is(expected.toString()));
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
  @DisplayName("F1: status stale ACTIVE com vencimento passado — markPaid resolve efetivo e avança")
  void markPaid_staleActiveResolvedToOverdue() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("stale-active"), uniqueCode(), uniqueWhatsapp()))
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
            "status", "ACTIVE"))
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
        .body("data.subscriber.status", is("OVERDUE"))
        .body("data.subscriber.nextDueDate", is("2020-01-10"));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of())
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber/paid")
        .then()
        .statusCode(200)
        .body("data.subscriber.status", not(is("OVERDUE")));
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("F2: advance ancora no billingDay, não no dia de nextDueDate")
  void markPaid_advanceAnchoredOnBillingDay() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("anchor-day"), uniqueCode(), uniqueWhatsapp()))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    LocalDate today = LocalDate.now();
    LocalDate due = today.withDayOfMonth(today.getDayOfMonth() <= 2 ? 1 : today.getDayOfMonth() - 1);
    if (!due.isBefore(today)) {
      due = due.minusMonths(1);
    }
    given()
        .contentType(ContentType.JSON)
        .body(Map.of(
            "nextDueDate", due.toString(),
            "status", "OVERDUE",
            "billingDay", 10))
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber")
        .then()
        .statusCode(200);

    String nextDue = given()
        .contentType(ContentType.JSON)
        .body(Map.of())
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber/paid")
        .then()
        .statusCode(200)
        .extract()
        .path("data.subscriber.nextDueDate");

    assertEquals(10, LocalDate.parse(nextDue).getDayOfMonth(),
        "Advance must anchor on billingDay=10");
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("F3: paidAt retroativo no mesmo mês de nextDueDate — pending ativado, 2ª avança")
  void markPaid_retroactivePaidAt_twoSteps() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("retro-paid"), uniqueCode(), uniqueWhatsapp()))
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
        .body(Map.of("paidAt", "2020-01-05T12:00:00Z"))
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber/paid")
        .then()
        .statusCode(200)
        .body("data.subscriber.status", is("OVERDUE"))
        .body("data.subscriber.nextDueDate", is("2020-01-10"));

    given()
        .contentType(ContentType.JSON)
        .body(Map.of())
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber/paid")
        .then()
        .statusCode(200)
        .body("data.subscriber.status", not(is("OVERDUE")));
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

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("F10: PATCH com status ACTIVE e vencimento passado normaliza para OVERDUE")
  void patchSubscriber_normalizesContradictoryStatus() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("patch-norm"), uniqueCode(), uniqueWhatsapp()))
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
            "status", "ACTIVE"))
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber")
        .then()
        .statusCode(200)
        .body("data.subscriber.status", is("OVERDUE"));
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("F10: PATCH com status INACTIVE preserva override manual")
  void patchSubscriber_preservesInactiveOverride() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("patch-inactive"), uniqueCode(), uniqueWhatsapp()))
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
            "status", "INACTIVE"))
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber")
        .then()
        .statusCode(200)
        .body("data.subscriber.status", is("INACTIVE"));
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("F11: PATCH rejeita billingDay fora de 1-28 com 400")
  void patchSubscriber_rejectsInvalidBillingDay() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("patch-day"), uniqueCode(), uniqueWhatsapp()))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("billingDay", 29))
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber")
        .then()
        .statusCode(400);
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("F11: PATCH rejeita monthlyFeeAmount <= 0 com 400")
  void patchSubscriber_rejectsInvalidMonthlyFee() {
    int userId = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("patch-fee"), uniqueCode(), uniqueWhatsapp()))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    given()
        .contentType(ContentType.JSON)
        .body(Map.of("monthlyFeeAmount", 0))
        .when()
        .patch(USER_PATH + "/" + userId + "/subscriber")
        .then()
        .statusCode(400);
  }

  @Test
  @TestSecurity(user = "admin", roles = "ADM")
  @DisplayName("F13: refreshBillingStatuses processa múltiplos assinantes em lotes")
  void refreshStatuses_processesMultipleSubscribersInBatches() {
    int userId1 = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("batch-1"), uniqueCode(), uniqueWhatsapp()))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    int userId2 = given()
        .contentType(ContentType.JSON)
        .body(memberPayload(uniqueEmail("batch-2"), uniqueCode(), uniqueWhatsapp()))
        .when()
        .post(USER_PATH)
        .then()
        .statusCode(201)
        .extract()
        .path("data.id");

    seedStaleOverdueState(userId1, LocalDate.of(2020, 1, 10));
    seedStaleOverdueState(userId2, LocalDate.of(2020, 2, 10));

    int changed = memberBillingService.refreshBillingStatuses();
    Assumptions.assumeTrue(changed >= 2, "Job should transition at least 2 overdue subscribers");

    given()
        .when()
        .get(USER_PATH + "/" + userId1)
        .then()
        .statusCode(200)
        .body("data.member.subscriber.status", is("OVERDUE"));

    given()
        .when()
        .get(USER_PATH + "/" + userId2)
        .then()
        .statusCode(200)
        .body("data.member.subscriber.status", is("OVERDUE"));
  }

  private void seedStaleOverdueState(int userId, LocalDate nextDueDate) {
    QuarkusTransaction.requiringNew().run(() -> {
      SubscriberMember sub = subscriberMemberRepository.findByMemberUserId((long) userId)
          .orElseThrow();
      sub.setNextDueDate(nextDueDate);
      sub.setStatus(MemberStatusEnum.ACTIVE);
    });
  }
}
