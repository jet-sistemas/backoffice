package backoffice.common.exceptions;

import java.util.List;

import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import backoffice.common.requests.ResponseModel;

/**
 * Converte violações de Bean Validation na borda REST (ex.: {@code @Valid} em parâmetros) para o
 * envelope {@link ResponseModel}, substituindo o payload padrão do framework ({@code title} /
 * {@code violations}).
 */
@Provider
@Priority(Priorities.USER - 100)
public class RestValidationExceptionMapper implements ExceptionMapper<ResteasyReactiveViolationException> {

  @Override
  public Response toResponse(ResteasyReactiveViolationException exception) {
    List<String> messages = exception.getConstraintViolations()
        .stream()
        .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
        .toList();

    var response = ResponseModel.error(Response.Status.BAD_REQUEST.getStatusCode(), messages);
    return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
  }
}
