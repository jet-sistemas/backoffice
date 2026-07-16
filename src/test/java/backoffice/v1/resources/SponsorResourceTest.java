package backoffice.v1.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import backoffice.common.utils.TokenUtils;
import backoffice.v1.dtos.benefit.BenefitCreateDTO;
import backoffice.v1.dtos.member.MemberDataCreateDTO;
import backoffice.v1.dtos.member.SubscriberDataCreateDTO;
import backoffice.v1.dtos.sponsor.SponsorDataCreateDTO;
import backoffice.v1.dtos.user.UserCreateDTO;
import backoffice.v1.dtos.user.UserWithSponsorCreateDTO;
import backoffice.v1.dtos.user.UserWithSponsorDTO;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.UserTypeEnum;
import backoffice.v1.services.AdminService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
class SponsorResourceTest {

  private static final String PREVIEW_PATH = "/v1/sponsor/checkins/member-preview";
  private static final String CHECKINS_PATH = "/v1/sponsor/checkins";
  private static final String BENEFITS_PATH = "/v1/sponsor/benefits";

  @Inject
  AdminService adminService;

  private static String uniqueDocument() {
    long t = System.currentTimeMillis() % 100_000_000_000L;
    int r = ThreadLocalRandom.current().nextInt(0, 1_000_000);
    return "1" + String.format("%010d", (t + r) % 10_000_000_000L);
  }

  private static String uniqueEmail(String prefix) {
    return prefix + "-" + System.currentTimeMillis() + "@test.com";
  }

  private static String uniqueCode(String prefix) {
    int r = ThreadLocalRandom.current().nextInt(0, 10_000);
    long t = System.currentTimeMillis() % 10_000;
    String body = String.format("%04d", (r + t) % 10_000);
    return (prefix + body).substring(0, 5);
  }

  private static String uniqueWhatsapp() {
    int suffix = ThreadLocalRandom.current().nextInt(100_000_000, 1_000_000_000);
    return "119" + suffix;
  }

  private UserWithSponsorDTO createSponsor() {
    UserWithSponsorCreateDTO dto = UserWithSponsorCreateDTO.builder()
        .user(UserCreateDTO.builder()
            .email(uniqueEmail("sp-chk"))
            .name("Patrocinador Checkin")
            .document(uniqueDocument())
            .code(uniqueCode("S"))
            .type("SPONSOR")
            .build())
        .sponsor(SponsorDataCreateDTO.builder()
            .publicName("Pub Checkin")
            .tier("GOLD")
            .entityType("COMPANY")
            .persona("OTHER")
            .whatsapp(uniqueWhatsapp())
            .build())
        .build();
    UserWithSponsorDTO created = adminService.createUser(dto, null);
    adminService.activateUser(created.getId());
    return adminService.findUserById(created.getId());
  }

  private UserWithSponsorDTO createActiveMember() {
    UserWithSponsorCreateDTO dto = memberCreateDto(uniqueEmail("mb-chk"), uniqueCode("M"), uniqueDocument());
    UserWithSponsorDTO created = adminService.createUser(dto, null);
    adminService.activateUser(created.getId());
    return adminService.findUserById(created.getId());
  }

  private UserWithSponsorDTO createInactiveMember() {
    UserWithSponsorCreateDTO dto = memberCreateDto(uniqueEmail("mb-inact"), uniqueCode("I"), uniqueDocument());
    return adminService.createUser(dto, null);
  }

  private static UserWithSponsorCreateDTO memberCreateDto(String email, String code, String document) {
    return UserWithSponsorCreateDTO.builder()
        .user(UserCreateDTO.builder()
            .email(email)
            .name("Membro Checkin")
            .document(document)
            .code(code)
            .type("MEMBER")
            .build())
        .member(MemberDataCreateDTO.builder()
            .fullname("Membro Checkin Completo")
            .whatsapp(uniqueWhatsapp())
            .type("SUBSCRIBER")
            .subscriber(SubscriberDataCreateDTO.builder()
                .monthlyFeeAmount(new BigDecimal("50.00"))
                .billingDay(10)
                .build())
            .build())
        .build();
  }

  private static String tokenFor(UserWithSponsorDTO sponsorUser) {
    User user = new User();
    user.setId(sponsorUser.getId());
    user.setEmail(sponsorUser.getEmail());
    user.setType(UserTypeEnum.SPONSOR);
    return TokenUtils.generateToken(user);
  }

  @Nested
  @DisplayName("RBAC")
  class Rbac {

    @Test
    @DisplayName("sem auth retorna 401")
    void withoutAuth_returns401() {
      given().when().get(PREVIEW_PATH).then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("ADM não acessa /v1/sponsor")
    void adm_forbidden() {
      given().when().get(PREVIEW_PATH + "?lookup=ABCDE").then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "member", roles = "MEMBER")
    @DisplayName("MEMBER não acessa /v1/sponsor")
    void member_forbidden() {
      given().when().get(PREVIEW_PATH + "?lookup=ABCDE").then().statusCode(403);
    }
  }

  @Nested
  @DisplayName("Preview e check-in")
  class PreviewAndCheckin {

    @Test
    @DisplayName("preview por código e CPF + check-in + duplicidade")
    void happyPath_andDuplicate() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      String token = tokenFor(sponsor);
      String code = member.getCode();
      Long memberId = member.getMember().getId();

      given()
          .header("Authorization", "Bearer " + token)
          .when()
          .get(PREVIEW_PATH + "?lookup=" + code)
          .then()
          .statusCode(200)
          .body("data.id", is(memberId.intValue()))
          .body("data.code", is(code))
          .body("data.eligible", is(true))
          .body("data.alreadyCheckedInToday", is(false));

      String doc = member.getDocument();
      String masked = doc.substring(0, 3) + "." + doc.substring(3, 6) + "."
          + doc.substring(6, 9) + "-" + doc.substring(9);
      given()
          .header("Authorization", "Bearer " + token)
          .when()
          .get(PREVIEW_PATH + "?lookup=" + masked)
          .then()
          .statusCode(200)
          .body("data.eligible", is(true));

      given()
          .header("Authorization", "Bearer " + token)
          .contentType(ContentType.JSON)
          .body(Map.of("lookup", code, "confirmDuplicateToday", false))
          .when()
          .post(CHECKINS_PATH)
          .then()
          .statusCode(200)
          .body("data.validated", is(true))
          .body("data.member.id", is(memberId.intValue()));

      given()
          .header("Authorization", "Bearer " + token)
          .contentType(ContentType.JSON)
          .body(Map.of("lookup", code, "confirmDuplicateToday", false))
          .when()
          .post(CHECKINS_PATH)
          .then()
          .statusCode(400);

      given()
          .header("Authorization", "Bearer " + token)
          .contentType(ContentType.JSON)
          .body(Map.of("lookup", code, "confirmDuplicateToday", true))
          .when()
          .post(CHECKINS_PATH)
          .then()
          .statusCode(200)
          .body("data.validated", is(true))
          .body("data.duplicateConfirmed", is(true));

      given()
          .header("Authorization", "Bearer " + token)
          .when()
          .get(CHECKINS_PATH + "?page=1&size=10")
          .then()
          .statusCode(200)
          .body("totalElements", greaterThanOrEqualTo(2));
    }

    @Test
    @DisplayName("membro inapto cria check-in validated=false com 200")
    void inactiveMember_createsUnvalidated() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createInactiveMember();
      String token = tokenFor(sponsor);

      given()
          .header("Authorization", "Bearer " + token)
          .contentType(ContentType.JSON)
          .body(Map.of("lookup", member.getCode()))
          .when()
          .post(CHECKINS_PATH)
          .then()
          .statusCode(200)
          .body("data.validated", is(false))
          .body("data.reason", notNullValue());
    }

    @Test
    @DisplayName("lookup inválido retorna 400; membro inexistente 404")
    void invalidLookup_andNotFound() {
      UserWithSponsorDTO sponsor = createSponsor();
      String token = tokenFor(sponsor);

      given()
          .header("Authorization", "Bearer " + token)
          .when()
          .get(PREVIEW_PATH + "?lookup=XX")
          .then()
          .statusCode(400);

      given()
          .header("Authorization", "Bearer " + token)
          .when()
          .get(PREVIEW_PATH + "?lookup=ZZ999")
          .then()
          .statusCode(404);
    }

    @Test
    @DisplayName("sponsor inativo: só histórico; preview/benefits/POST 403")
    void inactiveSponsor_onlyHistory() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      String token = tokenFor(sponsor);

      given()
          .header("Authorization", "Bearer " + token)
          .contentType(ContentType.JSON)
          .body(Map.of("lookup", member.getCode()))
          .when()
          .post(CHECKINS_PATH)
          .then()
          .statusCode(200);

      adminService.deactivateUser(sponsor.getId());

      given()
          .header("Authorization", "Bearer " + token)
          .when()
          .get(PREVIEW_PATH + "?lookup=" + member.getCode())
          .then()
          .statusCode(403);

      given()
          .header("Authorization", "Bearer " + token)
          .contentType(ContentType.JSON)
          .body(Map.of("lookup", member.getCode()))
          .when()
          .post(CHECKINS_PATH)
          .then()
          .statusCode(403);

      given()
          .header("Authorization", "Bearer " + token)
          .when()
          .get(BENEFITS_PATH)
          .then()
          .statusCode(403);

      given()
          .header("Authorization", "Bearer " + token)
          .when()
          .get(CHECKINS_PATH)
          .then()
          .statusCode(200)
          .body("totalElements", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("benefícios ativos do sponsor logado")
    void listOwnBenefits() {
      UserWithSponsorDTO sponsor = createSponsor();
      String token = tokenFor(sponsor);

      adminService.createBenefit(BenefitCreateDTO.builder()
          .name("Benefício Sponsor")
          .description("desc")
          .address("Rua 1")
          .sponsorId(sponsor.getSponsor().getId())
          .build());

      given()
          .header("Authorization", "Bearer " + token)
          .when()
          .get(BENEFITS_PATH + "?page=1&size=10")
          .then()
          .statusCode(200)
          .body("totalElements", greaterThanOrEqualTo(1))
          .body("data[0].name", is("Benefício Sponsor"));
    }
  }
}
