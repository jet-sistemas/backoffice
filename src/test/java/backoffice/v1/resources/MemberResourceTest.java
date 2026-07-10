package backoffice.v1.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MemberResourceTest {

  private static final String CHECKINS_PATH = "/v1/member/checkins";
  private static final String SPONSORS_PATH = "/v1/member/checkins/sponsors";
  private static final String BENEFITS_PATH = "/v1/member/benefits";
  private static final String SPONSOR_CHECKINS_PATH = "/v1/sponsor/checkins";

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
            .email(uniqueEmail("sp-mb-hist"))
            .name("Patrocinador Histórico Membro")
            .document(uniqueDocument())
            .code(uniqueCode("S"))
            .type("SPONSOR")
            .build())
        .sponsor(SponsorDataCreateDTO.builder()
            .publicName("Academia Jet Fit")
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
    UserWithSponsorCreateDTO dto = memberCreateDto(uniqueEmail("mb-hist"), uniqueCode("M"), uniqueDocument());
    UserWithSponsorDTO created = adminService.createUser(dto, null);
    adminService.activateUser(created.getId());
    return adminService.findUserById(created.getId());
  }

  private UserWithSponsorDTO createInactiveMember() {
    UserWithSponsorCreateDTO dto = memberCreateDto(uniqueEmail("mb-inact-hist"), uniqueCode("I"), uniqueDocument());
    return adminService.createUser(dto, null);
  }

  private static UserWithSponsorCreateDTO memberCreateDto(String email, String code, String document) {
    return UserWithSponsorCreateDTO.builder()
        .user(UserCreateDTO.builder()
            .email(email)
            .name("Membro Histórico")
            .document(document)
            .code(code)
            .type("MEMBER")
            .build())
        .member(MemberDataCreateDTO.builder()
            .fullname("Membro Histórico Completo")
            .whatsapp(uniqueWhatsapp())
            .type("SUBSCRIBER")
            .subscriber(SubscriberDataCreateDTO.builder()
                .monthlyFeeAmount(new BigDecimal("50.00"))
                .billingDay(10)
                .build())
            .build())
        .build();
  }

  private static String tokenForSponsor(UserWithSponsorDTO sponsorUser) {
    User user = new User();
    user.setId(sponsorUser.getId());
    user.setEmail(sponsorUser.getEmail());
    user.setType(UserTypeEnum.SPONSOR);
    return TokenUtils.generateToken(user);
  }

  private static String tokenForMember(UserWithSponsorDTO memberUser) {
    User user = new User();
    user.setId(memberUser.getId());
    user.setEmail(memberUser.getEmail());
    user.setType(UserTypeEnum.MEMBER);
    return TokenUtils.generateToken(user);
  }

  private void createValidatedCheckin(UserWithSponsorDTO sponsor, UserWithSponsorDTO member) {
    given()
        .header("Authorization", "Bearer " + tokenForSponsor(sponsor))
        .contentType(ContentType.JSON)
        .body(Map.of("lookup", member.getCode()))
        .when()
        .post(SPONSOR_CHECKINS_PATH)
        .then()
        .statusCode(200)
        .body("data.validated", is(true));
  }

  private void createUnvalidatedCheckin(UserWithSponsorDTO sponsor, UserWithSponsorDTO member) {
    given()
        .header("Authorization", "Bearer " + tokenForSponsor(sponsor))
        .contentType(ContentType.JSON)
        .body(Map.of("lookup", member.getCode()))
        .when()
        .post(SPONSOR_CHECKINS_PATH)
        .then()
        .statusCode(200)
        .body("data.validated", is(false));
  }

  @Nested
  @DisplayName("RBAC")
  class Rbac {

    @Test
    @DisplayName("sem auth retorna 401")
    void withoutAuth_returns401() {
      given().when().get(CHECKINS_PATH).then().statusCode(401);
      given().when().get(SPONSORS_PATH).then().statusCode(401);
      given().when().get(BENEFITS_PATH).then().statusCode(401);
    }

    @Test
    @TestSecurity(user = "admin", roles = "ADM")
    @DisplayName("ADM não acessa /v1/member/checkins")
    void adm_forbidden() {
      given().when().get(CHECKINS_PATH).then().statusCode(403);
      given().when().get(SPONSORS_PATH).then().statusCode(403);
      given().when().get(BENEFITS_PATH).then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "sponsor", roles = "SPONSOR")
    @DisplayName("SPONSOR não acessa /v1/member/checkins")
    void sponsor_forbidden() {
      given().when().get(CHECKINS_PATH).then().statusCode(403);
      given().when().get(SPONSORS_PATH).then().statusCode(403);
      given().when().get(BENEFITS_PATH).then().statusCode(403);
    }
  }

  @Nested
  @DisplayName("Histórico do membro")
  class MemberHistory {

    @Test
    @DisplayName("membro autenticado lista apenas check-ins validated=true")
    void member_listsValidatedOnly() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      UserWithSponsorDTO inactiveMember = createInactiveMember();
      String memberToken = tokenForMember(member);

      createValidatedCheckin(sponsor, member);
      createUnvalidatedCheckin(sponsor, inactiveMember);

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(CHECKINS_PATH + "?page=1&size=10")
          .then()
          .statusCode(200)
          .body("totalElements", is(1))
          .body("data[0].validated", is(true))
          .body("data[0].sponsor.publicName", is("Academia Jet Fit"));
    }

    @Test
    @DisplayName("histórico não retorna check-ins de outro membro")
    void member_doesNotSeeOtherMemberCheckins() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO memberA = createActiveMember();
      UserWithSponsorDTO memberB = createActiveMember();
      String tokenA = tokenForMember(memberA);

      createValidatedCheckin(sponsor, memberA);
      createValidatedCheckin(sponsor, memberB);

      given()
          .header("Authorization", "Bearer " + tokenA)
          .when()
          .get(CHECKINS_PATH + "?page=1&size=10")
          .then()
          .statusCode(200)
          .body("totalElements", is(1));
    }

    @Test
    @DisplayName("filtro por sponsorId retorna apenas patrocinador selecionado")
    void filterBySponsorId() {
      UserWithSponsorDTO sponsorA = createSponsor();
      UserWithSponsorDTO sponsorB = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      String memberToken = tokenForMember(member);
      Long sponsorAId = sponsorA.getSponsor().getId();

      createValidatedCheckin(sponsorA, member);
      createValidatedCheckin(sponsorB, member);

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(CHECKINS_PATH + "?sponsorId=" + sponsorAId + "&page=1&size=10")
          .then()
          .statusCode(200)
          .body("totalElements", is(1))
          .body("data[0].sponsor.id", is(sponsorAId.intValue()));
    }

    @Test
    @DisplayName("sponsorId sem registros retorna lista vazia")
    void sponsorIdWithoutRecords_returnsEmpty() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      String memberToken = tokenForMember(member);

      createValidatedCheckin(sponsor, member);

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(CHECKINS_PATH + "?sponsorId=999999&page=1&size=10")
          .then()
          .statusCode(200)
          .body("totalElements", is(0))
          .body("data", empty());
    }

    @Test
    @DisplayName("startDate > endDate retorna 400")
    void invalidDateRange_returns400() {
      UserWithSponsorDTO member = createActiveMember();
      String memberToken = tokenForMember(member);

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(CHECKINS_PATH + "?startDate=2026-07-31&endDate=2026-07-01&page=1&size=10")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("paginação retorna metadados corretos")
    void paginationMetadata() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      String memberToken = tokenForMember(member);

      createValidatedCheckin(sponsor, member);
      given()
          .header("Authorization", "Bearer " + tokenForSponsor(sponsor))
          .contentType(ContentType.JSON)
          .body(Map.of("lookup", member.getCode(), "confirmDuplicateToday", true))
          .when()
          .post(SPONSOR_CHECKINS_PATH)
          .then()
          .statusCode(200);

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(CHECKINS_PATH + "?page=1&size=1")
          .then()
          .statusCode(200)
          .body("totalElements", greaterThanOrEqualTo(2))
          .body("pageSize", is(1))
          .body("currentPage", is(1))
          .body("data", hasSize(1));
    }

    @Test
    @DisplayName("endpoint de sponsors retorna patrocinadores do histórico do membro")
    void sponsorsEndpoint_returnsDistinctSponsors() {
      UserWithSponsorDTO sponsorA = createSponsor();
      UserWithSponsorDTO sponsorB = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      String memberToken = tokenForMember(member);

      createValidatedCheckin(sponsorA, member);
      createValidatedCheckin(sponsorB, member);

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(SPONSORS_PATH)
          .then()
          .statusCode(200)
          .body("data", hasSize(2));
    }

    @Test
    @DisplayName("filtro por intervalo de datas inclusivo")
    void dateRangeFilter_inclusive() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      String memberToken = tokenForMember(member);

      createValidatedCheckin(sponsor, member);

      LocalDate today = LocalDate.now();
      String start = today.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
      String end = today.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(CHECKINS_PATH + "?startDate=" + start + "&endDate=" + end + "&page=1&size=10")
          .then()
          .statusCode(200)
          .body("totalElements", greaterThanOrEqualTo(1));
    }
  }

  @Nested
  @DisplayName("Benefícios do membro")
  class MemberBenefits {

    @Test
    @DisplayName("membro autenticado lista benefício geral ativo")
    void member_listsGeneralActiveBenefit() {
      UserWithSponsorDTO member = createActiveMember();
      String memberToken = tokenForMember(member);

      adminService.createBenefit(BenefitCreateDTO.builder()
          .name("Evento exclusivo")
          .description("Entrada gratuita para membros.")
          .sponsorId(null)
          .build());

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(BENEFITS_PATH + "?page=1&size=10")
          .then()
          .statusCode(200)
          .body("totalElements", greaterThanOrEqualTo(1))
          .body("data.find { it.name == 'Evento exclusivo' }.sponsor", empty());
    }

    @Test
    @DisplayName("lista benefício de sponsor ativo com dados mínimos do patrocinador")
    void member_listsSponsoredActiveBenefit() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      String memberToken = tokenForMember(member);
      Long sponsorId = sponsor.getSponsor().getId();

      adminService.createBenefit(BenefitCreateDTO.builder()
          .name("Desconto na mensalidade")
          .description("10% de desconto para membros Jet.")
          .address("Rua Exemplo, 123")
          .sponsorId(sponsorId)
          .build());

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(BENEFITS_PATH + "?page=1&size=10")
          .then()
          .statusCode(200)
          .body("totalElements", greaterThanOrEqualTo(1))
          .body("data.find { it.name == 'Desconto na mensalidade' }.sponsor.publicName", is("Academia Jet Fit"))
          .body("data.find { it.name == 'Desconto na mensalidade' }.sponsor.id", is(sponsorId.intValue()));
    }

    @Test
    @DisplayName("não retorna benefício inativo")
    void excludesInactiveBenefit() {
      UserWithSponsorDTO member = createActiveMember();
      String memberToken = tokenForMember(member);

      var created = adminService.createBenefit(BenefitCreateDTO.builder()
          .name("Benefício Inativo")
          .description("Não deve aparecer")
          .sponsorId(null)
          .build());
      adminService.deactivateBenefit(created.getId());

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(BENEFITS_PATH + "?page=1&size=50")
          .then()
          .statusCode(200)
          .body("data.find { it.name == 'Benefício Inativo' }", empty());
    }

    @Test
    @DisplayName("não retorna benefício de sponsor inativo")
    void excludesInactiveSponsorBenefit() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      String memberToken = tokenForMember(member);

      adminService.createBenefit(BenefitCreateDTO.builder()
          .name("Benefício Sponsor Inativo")
          .description("Não deve aparecer")
          .sponsorId(sponsor.getSponsor().getId())
          .build());

      adminService.deactivateUser(sponsor.getId());

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(BENEFITS_PATH + "?page=1&size=50")
          .then()
          .statusCode(200)
          .body("data.find { it.name == 'Benefício Sponsor Inativo' }", empty());
    }

    @Test
    @DisplayName("filtro por sponsorId retorna apenas benefícios do patrocinador")
    void filterBySponsorId() {
      UserWithSponsorDTO sponsorA = createSponsor();
      UserWithSponsorDTO sponsorB = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      String memberToken = tokenForMember(member);
      Long sponsorAId = sponsorA.getSponsor().getId();

      adminService.createBenefit(BenefitCreateDTO.builder()
          .name("Benefício A")
          .sponsorId(sponsorAId)
          .build());
      adminService.createBenefit(BenefitCreateDTO.builder()
          .name("Benefício B")
          .sponsorId(sponsorB.getSponsor().getId())
          .build());
      adminService.createBenefit(BenefitCreateDTO.builder()
          .name("Benefício Geral")
          .sponsorId(null)
          .build());

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(BENEFITS_PATH + "?sponsorId=" + sponsorAId + "&page=1&size=10")
          .then()
          .statusCode(200)
          .body("totalElements", is(1))
          .body("data[0].name", is("Benefício A"))
          .body("data[0].sponsor.id", is(sponsorAId.intValue()));
    }

    @Test
    @DisplayName("filtro por sponsorId inativo retorna lista vazia")
    void sponsorIdInactive_returnsEmpty() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      String memberToken = tokenForMember(member);
      Long sponsorId = sponsor.getSponsor().getId();

      adminService.createBenefit(BenefitCreateDTO.builder()
          .name("Benefício Sponsor")
          .sponsorId(sponsorId)
          .build());
      adminService.deactivateUser(sponsor.getId());

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(BENEFITS_PATH + "?sponsorId=" + sponsorId + "&page=1&size=10")
          .then()
          .statusCode(200)
          .body("totalElements", is(0))
          .body("data", empty());
    }

    @Test
    @DisplayName("paginação retorna metadados corretos")
    void paginationMetadata() {
      UserWithSponsorDTO member = createActiveMember();
      String memberToken = tokenForMember(member);

      for (int i = 0; i < 3; i++) {
        adminService.createBenefit(BenefitCreateDTO.builder()
            .name("Benefício Geral " + i)
            .sponsorId(null)
            .build());
      }

      given()
          .header("Authorization", "Bearer " + memberToken)
          .when()
          .get(BENEFITS_PATH + "?page=1&size=2")
          .then()
          .statusCode(200)
          .body("totalElements", greaterThanOrEqualTo(3))
          .body("pageSize", is(2))
          .body("currentPage", is(1))
          .body("data", hasSize(2));
    }
  }
}
