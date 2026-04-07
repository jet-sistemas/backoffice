package backoffice.v1.resources;

import backoffice.common.database.Pageable;
import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.publics.ListPublicSponsorsQueryDTO;
import backoffice.v1.dtos.publics.PublicSponsorItemDTO;
import backoffice.v1.openapi.api.PublicApi;
import backoffice.v1.services.PublicService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class PublicResource implements PublicApi {
  @Inject
  private PublicService service;

  @Override
  public Response listSponsors(@Valid @BeanParam ListPublicSponsorsQueryDTO query) {
    Pageable<PublicSponsorItemDTO> result = service.listActiveSponsors(
        query.resolveTier(),
        PageDTO.of(query.getPage(), query.getSize()));
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }
}
