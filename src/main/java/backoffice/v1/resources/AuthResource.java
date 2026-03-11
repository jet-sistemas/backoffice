package backoffice.v1.resources;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.auth.AuthCreateDTO;
import backoffice.v1.dtos.auth.AuthDTO;
import backoffice.v1.dtos.auth.AuthExtDTO;
import backoffice.v1.services.AuthService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Auth", description = "Autenticação e dados do usuário logado")
public class AuthResource {
  @Inject
  private AuthService service;

  @POST
  @Operation(summary = "Login", description = "Autentica o usuário e retorna um token JWT")
  public Response signIn(@Valid AuthCreateDTO dto) {
    AuthDTO result = service.signIn(dto);

    var response = ResponseModel.success(Response.Status.OK.getStatusCode(), result);

    return Response.ok(response).build();
  }

  @GET
  @Path("/me")
  @Authenticated
  @Operation(summary = "Dados do usuário logado", description = "Retorna os dados do usuário autenticado via token JWT")
  public Response me(@Context SecurityContext ctx) {
    AuthExtDTO result = service.me(ctx.getUserPrincipal().getName());

    var response = ResponseModel.success(Response.Status.OK.getStatusCode(), result);

    return Response.ok(response).build();
  }
}
