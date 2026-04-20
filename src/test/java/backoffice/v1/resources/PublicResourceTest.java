package backoffice.v1.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PublicResourceTest {

  private static final String BASE_PATH = "/v1/public/sponsors";

  @Nested
  @DisplayName("GET /v1/public/sponsors")
  class ListPublicSponsors {

    @Test
    @DisplayName("retorna 200 sem autenticação")
    void listWithoutAuth_returns200() {
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
    @DisplayName("aceita filtro tier válido e retorna 200")
    void listWithTier_returns200() {
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
    @DisplayName("retorna 400 quando tier inválido")
    void listWithInvalidTier_returns400() {
      given()
          .queryParam("tier", "INVALID")
          .when()
          .get(BASE_PATH)
          .then()
          .statusCode(400)
          .body("status", is("ERROR"));
    }

    @Test
    @DisplayName("aceita paginação page e size")
    void listWithPagination_reflectsPageMetadata() {
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
}
