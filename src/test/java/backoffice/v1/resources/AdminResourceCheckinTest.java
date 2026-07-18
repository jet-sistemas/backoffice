package backoffice.v1.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import backoffice.common.utils.TokenUtils;
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
class AdminResourceCheckinTest {

  private static final String CHECKINS_PATH = "/v1/admin/check-ins";
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
            .email(uniqueEmail("sp-adm-chk"))
            .name("Patrocinador ADM Check-in")
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
    UserWithSponsorCreateDTO dto = memberCreateDto(uniqueEmail("mb-adm-chk"), uniqueCode("M"), uniqueDocument());
    UserWithSponsorDTO created = adminService.createUser(dto, null);
    adminService.activateUser(created.getId());
    return adminService.findUserById(created.getId());
  }

  private UserWithSponsorDTO createInactiveMember() {
    UserWithSponsorCreateDTO dto = memberCreateDto(uniqueEmail("mb-inact-adm"), uniqueCode("I"), uniqueDocument());
    return adminService.createUser(dto, null);
  }

  private static UserWithSponsorCreateDTO memberCreateDto(String email, String code, String document) {
    return UserWithSponsorCreateDTO.builder()
        .user(UserCreateDTO.builder()
            .email(email)
            .name("Membro ADM Check-in")
            .document(document)
            .code(code)
            .type("MEMBER")
            .build())
        .member(MemberDataCreateDTO.builder()
            .fullname("Membro ADM Check-in Completo")
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

  private static String tokenForAdmin() {
    User user = new User();
    user.setId(1L);
    user.setEmail("admin@test.com");
    user.setType(UserTypeEnum.ADM);
    return TokenUtils.generateToken(user);
  }

  private void createValidatedCheckin(UserWithSponsorDTO sponsor, UserWithSponsorDTO member) {
    createValidatedCheckin(sponsor, member, false);
  }

  private void createValidatedCheckin(
      UserWithSponsorDTO sponsor, UserWithSponsorDTO member, boolean confirmDuplicateToday) {
    given()
        .header("Authorization", "Bearer " + tokenForSponsor(sponsor))
        .contentType(ContentType.JSON)
        .body(confirmDuplicateToday
            ? Map.of("lookup", member.getCode(), "confirmDuplicateToday", true)
            : Map.of("lookup", member.getCode()))
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
    }

    @Test
    @TestSecurity(user = "sponsor", roles = "SPONSOR")
    @DisplayName("SPONSOR recebe 403")
    void sponsor_forbidden() {
      given().when().get(CHECKINS_PATH).then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "member", roles = "MEMBER")
    @DisplayName("MEMBER recebe 403")
    void member_forbidden() {
      given().when().get(CHECKINS_PATH).then().statusCode(403);
    }
  }

  @Nested
  @DisplayName("Listagem global ADM")
  class AdminListing {

    private io.restassured.specification.RequestSpecification adminRequest() {
      return given().header("Authorization", "Bearer " + tokenForAdmin());
    }

    @Test
    @DisplayName("ADM lista validated=true e validated=false sem filtro")
    void admin_listsAllStatuses() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO activeMember = createActiveMember();
      UserWithSponsorDTO inactiveMember = createInactiveMember();

      createValidatedCheckin(sponsor, activeMember);
      createUnvalidatedCheckin(sponsor, inactiveMember);

      adminRequest()
          .when()
          .get(CHECKINS_PATH + "?page=1&size=50")
          .then()
          .statusCode(200)
          .body("data", hasSize(greaterThanOrEqualTo(2)))
          .body("data.findAll { it.validated == true }.size()", greaterThanOrEqualTo(1))
          .body("data.findAll { it.validated == false }.size()", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("filtro validated=true retorna apenas confirmados")
    void validatedTrue_returnsOnlyConfirmed() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO activeMember = createActiveMember();
      UserWithSponsorDTO inactiveMember = createInactiveMember();

      createValidatedCheckin(sponsor, activeMember);
      createUnvalidatedCheckin(sponsor, inactiveMember);

      adminRequest()
          .when()
          .get(CHECKINS_PATH + "?validated=true&page=1&size=50")
          .then()
          .statusCode(200)
          .body("data", not(empty()))
          .body("data.every { it.validated == true }", is(true));
    }

    @Test
    @DisplayName("filtro validated=false retorna apenas não confirmados")
    void validatedFalse_returnsOnlyUnconfirmed() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO activeMember = createActiveMember();
      UserWithSponsorDTO inactiveMember = createInactiveMember();

      createValidatedCheckin(sponsor, activeMember);
      createUnvalidatedCheckin(sponsor, inactiveMember);

      adminRequest()
          .when()
          .get(CHECKINS_PATH + "?validated=false&page=1&size=50")
          .then()
          .statusCode(200)
          .body("data", not(empty()))
          .body("data.every { it.validated == false }", is(true));
    }

    @Test
    @DisplayName("filtro sponsorId retorna apenas patrocinador selecionado")
    void sponsorFilter_returnsOnlySelectedSponsor() {
      UserWithSponsorDTO sponsorA = createSponsor();
      UserWithSponsorDTO sponsorB = createSponsor();
      UserWithSponsorDTO member = createActiveMember();

      createValidatedCheckin(sponsorA, member);
      createValidatedCheckin(sponsorB, member);

      Long sponsorAId = sponsorA.getSponsor().getId();

      adminRequest()
          .when()
          .get(CHECKINS_PATH + "?sponsorId=" + sponsorAId + "&page=1&size=50")
          .then()
          .statusCode(200)
          .body("data", not(empty()))
          .body("data.every { it.sponsor.id == " + sponsorAId + " }", is(true));
    }

    @Test
    @DisplayName("filtro memberUserId resolve Member e retorna apenas membro selecionado")
    void memberUserFilter_returnsOnlySelectedMember() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO memberA = createActiveMember();
      UserWithSponsorDTO memberB = createActiveMember();

      createValidatedCheckin(sponsor, memberA);
      createValidatedCheckin(sponsor, memberB);

      Long memberAEntityId = memberA.getMember().getId();

      adminRequest()
          .when()
          .get(CHECKINS_PATH + "?memberUserId=" + memberA.getId() + "&page=1&size=50")
          .then()
          .statusCode(200)
          .body("data", not(empty()))
          .body("data.every { it.member.id == " + memberAEntityId + " }", is(true));
    }

    @Test
    @DisplayName("memberUserId inexistente retorna 404")
    void memberUserNotFound_returns404() {
      adminRequest()
          .when()
          .get(CHECKINS_PATH + "?memberUserId=999999999&page=1&size=10")
          .then()
          .statusCode(404);
    }

    @Test
    @DisplayName("memberUserId de usuário não membro retorna 400")
    void memberUserNotMember_returns400() {
      UserWithSponsorDTO sponsorUser = createSponsor();

      adminRequest()
          .when()
          .get(CHECKINS_PATH + "?memberUserId=" + sponsorUser.getId() + "&page=1&size=10")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("sponsorId inexistente retorna 404")
    void sponsorNotFound_returns404() {
      adminRequest()
          .when()
          .get(CHECKINS_PATH + "?sponsorId=999999999&page=1&size=10")
          .then()
          .statusCode(404);
    }

    @Test
    @DisplayName("startDate > endDate retorna 400")
    void invalidDateRange_returns400() {
      adminRequest()
          .when()
          .get(CHECKINS_PATH + "?startDate=2026-07-10&endDate=2026-07-01&page=1&size=10")
          .then()
          .statusCode(400);
    }

    @Test
    @DisplayName("filtros combinados funcionam")
    void combinedFilters_work() {
      UserWithSponsorDTO sponsorA = createSponsor();
      UserWithSponsorDTO sponsorB = createSponsor();
      UserWithSponsorDTO memberA = createActiveMember();
      UserWithSponsorDTO memberB = createActiveMember();

      createValidatedCheckin(sponsorA, memberA);
      createValidatedCheckin(sponsorA, memberB);
      createValidatedCheckin(sponsorB, memberA);

      Long sponsorAId = sponsorA.getSponsor().getId();
      Long memberAEntityId = memberA.getMember().getId();

      adminRequest()
          .when()
          .get(CHECKINS_PATH
              + "?sponsorId=" + sponsorAId
              + "&memberUserId=" + memberA.getId()
              + "&validated=true&page=1&size=50")
          .then()
          .statusCode(200)
          .body("data", hasSize(1))
          .body("data[0].sponsor.id", is(sponsorAId.intValue()))
          .body("data[0].member.id", is(memberAEntityId.intValue()))
          .body("data[0].validated", is(true));
    }

    @Test
    @DisplayName("paginação retorna metadados corretos")
    void pagination_returnsMetadata() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createActiveMember();

      createValidatedCheckin(sponsor, member);
      createValidatedCheckin(sponsor, member, true);
      createValidatedCheckin(sponsor, member, true);

      adminRequest()
          .when()
          .get(CHECKINS_PATH + "?page=1&size=2")
          .then()
          .statusCode(200)
          .body("pageSize", is(2))
          .body("currentPage", is(1))
          .body("totalElements", greaterThanOrEqualTo(3))
          .body("totalPages", greaterThanOrEqualTo(2))
          .body("data", hasSize(2));
    }

    @Test
    @DisplayName("ordenação retorna mais recente primeiro")
    void ordering_returnsNewestFirst() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createActiveMember();

      createValidatedCheckin(sponsor, member);

      given()
          .header("Authorization", "Bearer " + tokenForSponsor(sponsor))
          .contentType(ContentType.JSON)
          .body(Map.of("lookup", member.getCode(), "confirmDuplicateToday", true))
          .when()
          .post(SPONSOR_CHECKINS_PATH)
          .then()
          .statusCode(200);

      adminRequest()
          .when()
          .get(CHECKINS_PATH + "?memberUserId=" + member.getId() + "&page=1&size=10")
          .then()
          .statusCode(200)
          .body("data.size()", greaterThanOrEqualTo(2))
          .body("data[0].checkedInAt", not(nullValue()))
          .body("data[1].checkedInAt", not(nullValue()));
    }

    @Test
    @DisplayName("CPF do membro vem mascarado")
    void memberDocument_isMasked() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      createValidatedCheckin(sponsor, member);

      adminRequest()
          .when()
          .get(CHECKINS_PATH + "?memberUserId=" + member.getId() + "&page=1&size=10")
          .then()
          .statusCode(200)
          .body("data[0].member.documentMasked", not(member.getDocument()))
          .body("data[0].member.documentMasked", not(nullValue()));
    }

    @Test
    @DisplayName("filtro por data é inclusivo")
    void dateFilter_isInclusive() {
      UserWithSponsorDTO sponsor = createSponsor();
      UserWithSponsorDTO member = createActiveMember();
      createValidatedCheckin(sponsor, member);

      String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

      adminRequest()
          .when()
          .get(CHECKINS_PATH
              + "?startDate=" + today
              + "&endDate=" + today
              + "&memberUserId=" + member.getId()
              + "&page=1&size=10")
          .then()
          .statusCode(200)
          .body("data", not(empty()));
    }
  }
}
