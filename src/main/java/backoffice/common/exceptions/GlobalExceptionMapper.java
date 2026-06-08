package backoffice.common.exceptions;

import java.util.List;

import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import backoffice.common.exceptions.customs.BadRequestException;
import backoffice.common.exceptions.customs.BusinessException;
import backoffice.common.exceptions.customs.ConflictException;
import backoffice.common.exceptions.customs.ForbiddenException;
import backoffice.common.exceptions.customs.NotFoundException;
import backoffice.common.requests.ResponseModel;
import org.hibernate.StaleObjectStateException;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

  @Override
  public Response toResponse(Throwable exception) {
    exception.printStackTrace();

    ResponseModel<Object> response;

    if (exception instanceof BusinessException be) {
      response = ResponseModel.error(be.getStatusCode(), be.getMessage());
      return Response.status(be.getStatusCode()).entity(response).build();
    }

    if (exception instanceof BadRequestException bre) {
      response = ResponseModel.error(bre.getStatusCode(), bre.getMessage());
      return Response.status(bre.getStatusCode()).entity(response).build();
    }

    if (exception instanceof NotFoundException nfe) {
      response = ResponseModel.error(nfe.getStatusCode(), nfe.getMessage());
      return Response.status(nfe.getStatusCode()).entity(response).build();
    }

    if (exception instanceof ConflictException ce) {
      response = ResponseModel.error(ce.getStatusCode(), ce.getMessage());
      return Response.status(ce.getStatusCode()).entity(response).build();
    }

    if (exception instanceof ForbiddenException fe) {
      response = ResponseModel.error(fe.getStatusCode(), fe.getMessage());
      return Response.status(fe.getStatusCode()).entity(response).build();
    }

    if (isOptimisticLock(exception)) {
      response = ResponseModel.error(
          Response.Status.CONFLICT.getStatusCode(),
          MessageErrorEnum.SUBSCRIBER_PAYMENT_CONCURRENT_UPDATE.getMessage());
      return Response.status(Response.Status.CONFLICT).entity(response).build();
    }

    response = ResponseModel.error(MessageErrorEnum.INTERNAL_ERROR.getMessage());

    // TODO: Delete this in production
    response.setStackTrace(List.of(exception.getMessage()));

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
  }

  private static boolean isOptimisticLock(Throwable exception) {
    Throwable current = exception;
    while (current != null) {
      if (current instanceof OptimisticLockException || current instanceof StaleObjectStateException) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }
}
