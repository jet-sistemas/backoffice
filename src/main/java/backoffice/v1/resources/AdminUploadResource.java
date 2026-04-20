package backoffice.v1.resources;

import java.util.Optional;

import org.eclipse.microprofile.jwt.JsonWebToken;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.upload.UploadConfirmDTO;
import backoffice.v1.dtos.upload.UploadDeleteDTO;
import backoffice.v1.dtos.upload.UploadInitDTO;
import backoffice.v1.openapi.api.AdminUploadApi;
import backoffice.v1.services.AdminUploadService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class AdminUploadResource implements AdminUploadApi {

  @Inject
  AdminUploadService service;

  @Inject
  SecurityIdentity identity;

  @Override
  public Response initUpload(@Valid UploadInitDTO dto) {
    var result = service.initUpload(dto, currentActorId());
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response confirmUpload(@Valid UploadConfirmDTO dto) {
    var result = service.confirmUpload(dto, currentActorId());
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response deleteUpload(@Valid UploadDeleteDTO dto) {
    var result = service.deleteUpload(dto, currentActorId());
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  private Optional<Long> currentActorId() {
    if (identity == null || identity.isAnonymous()) {
      return Optional.empty();
    }
    if (identity.getPrincipal() instanceof JsonWebToken jwt) {
      Object id = jwt.getClaim("id");
      if (id == null) {
        return Optional.empty();
      }
      if (id instanceof Number n) {
        return Optional.of(n.longValue());
      }
      try {
        return Optional.of(Long.parseLong(id.toString()));
      } catch (NumberFormatException e) {
        return Optional.empty();
      }
    }
    return Optional.empty();
  }
}
