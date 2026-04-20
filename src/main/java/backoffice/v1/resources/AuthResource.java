package backoffice.v1.resources;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.auth.AuthCreateDTO;
import backoffice.v1.dtos.auth.AuthDTO;
import backoffice.v1.dtos.auth.AuthExtDTO;
import backoffice.v1.openapi.api.AuthApi;
import backoffice.v1.services.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

public class AuthResource implements AuthApi {
  @Inject
  private AuthService service;

  @Override
  public Response signIn(AuthCreateDTO dto) {
    AuthDTO result = service.signIn(dto);
    var response = ResponseModel.success(Response.Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response me(@Context SecurityContext ctx) {
    AuthExtDTO result = service.me(ctx.getUserPrincipal().getName());
    var response = ResponseModel.success(Response.Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }
}
