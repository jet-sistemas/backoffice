package backoffice.v1.resources;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.accountvalidation.AccountValidationRequestDTO;
import backoffice.v1.openapi.api.AccountValidationApi;
import backoffice.v1.services.AccountValidationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class AccountValidationResource implements AccountValidationApi {

  @Inject
  private AccountValidationService service;

  @Override
  public Response confirm(AccountValidationRequestDTO dto) {
    var result = service.confirmAccount(dto.getToken(), dto.getCode(), dto.getDocument());
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }
}
