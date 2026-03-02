package project.common.exceptions;

import java.util.List;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import project.common.exceptions.customs.BadRequestException;
import project.common.exceptions.customs.BusinessException;
import project.common.exceptions.customs.ConflictException;
import project.common.exceptions.customs.ForbiddenException;
import project.common.exceptions.customs.NotFoundException;
import project.common.requests.ResponseModel;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

  @Override
  public Response toResponse(Throwable exception) {
    exception.printStackTrace();

    ResponseModel<Object> response;

    if (exception instanceof ConstraintViolationException validationEx) {
      List<String> messages = validationEx.getConstraintViolations()
          .stream()
          .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
          .toList();

      response = ResponseModel.error(Response.Status.BAD_REQUEST.getStatusCode(), messages);
      return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }

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

    response = ResponseModel.error(MessageErrorEnum.INTERNAL_ERROR.getMessage());

    // TODO: Delete this in production
    response.setStackTrace(List.of(exception.getMessage()));

    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
  }
}
