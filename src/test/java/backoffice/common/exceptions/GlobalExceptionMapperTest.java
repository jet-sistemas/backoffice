package backoffice.common.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.Response;

import org.hibernate.StaleObjectStateException;
import org.junit.jupiter.api.Test;

class GlobalExceptionMapperTest {

  private final GlobalExceptionMapper mapper = new GlobalExceptionMapper();

  @Test
  void optimisticLockException_returns409() {
    Response response = mapper.toResponse(new OptimisticLockException());
    assertEquals(409, response.getStatus());
  }

  @Test
  void staleObjectStateExceptionInCause_returns409() {
    Response response = mapper.toResponse(
        new RuntimeException(new StaleObjectStateException("SubscriberMember", null)));
    assertEquals(409, response.getStatus());
  }
}
