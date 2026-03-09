package backoffice.v1.resources;

import backoffice.common.database.Pageable;
import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.entities.enums.SponsorEntityTypeEnum;
import backoffice.v1.entities.enums.SponsorTierEnum;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/v1/admin")
@RolesAllowed({ "ADM" })
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {
  @Inject
  private AdminService service;

  @POST
  @Path("/sponsor")
  public Response createSponsor(@Valid SponsorCreateDTO dto) {
    var result = service.createAgent(dto);

    var response = ResponseModel.success(Response.Status.OK.getStatusCode(), result);

    return Response.ok(response).build();
  }

  @GET
  @Path("/agent")
  public Response listSponsors(@QueryParam("tier") SponsorTierEnum tier,
      @QueryParam("page") Integer page,
      @QueryParam("size") Integer size) {
    Pageable<SponsorDTO> result = service.listSponsors(tier, PageDTO.of(page, size));
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);

    return Response.ok(response).build();
  }
}
