package backoffice.v1.resources;

import backoffice.common.database.Pageable;
import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.benefit.BenefitCreateDTO;
import backoffice.v1.dtos.benefit.BenefitDTO;
import backoffice.v1.dtos.benefit.BenefitUpdateDTO;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.user.UserWithSponsorCreateDTO;
import backoffice.v1.dtos.user.UserWithSponsorDTO;
import backoffice.v1.dtos.user.UserWithSponsorUpdateDTO;
import backoffice.v1.entities.enums.SponsorTierEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import backoffice.v1.services.AdminService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
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
  @Path("/user")
  public Response createUser(@Valid UserWithSponsorCreateDTO dto) {
    var result = service.createUser(dto);
    var response = ResponseModel.success(Status.CREATED.getStatusCode(), result);
    return Response.status(Status.CREATED).entity(response).build();
  }

  @PUT
  @Path("/user/{id}")
  public Response updateUser(@PathParam("id") Long id, @Valid UserWithSponsorUpdateDTO dto) {
    var result = service.updateUser(id, dto);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @PATCH
  @Path("/user/{id}/deactivate")
  public Response deactivateUser(@PathParam("id") Long id) {
    service.deactivateUser(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }

  @DELETE
  @Path("/user/{id}")
  public Response deleteUser(@PathParam("id") Long id) {
    service.deleteUser(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }

  @GET
  @Path("/user/{id}")
  public Response findUserById(@PathParam("id") Long id) {
    var result = service.findUserById(id);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @GET
  @Path("/user")
  public Response listUsers(
      @QueryParam("type") UserTypeEnum type,
      @QueryParam("tier") SponsorTierEnum tier,
      @QueryParam("isActive") Boolean isActive,
      @QueryParam("page") Integer page,
      @QueryParam("size") Integer size) {
    Pageable<UserWithSponsorDTO> result = service.listUsers(type, tier, isActive, PageDTO.of(page, size));
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  // --- Benefit ---

  @POST
  @Path("/benefit")
  public Response createBenefit(@Valid BenefitCreateDTO dto) {
    var result = service.createBenefit(dto);
    var response = ResponseModel.success(Status.CREATED.getStatusCode(), result);
    return Response.status(Status.CREATED).entity(response).build();
  }

  @PUT
  @Path("/benefit/{id}")
  public Response updateBenefit(@PathParam("id") Long id, @Valid BenefitUpdateDTO dto) {
    var result = service.updateBenefit(id, dto);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @GET
  @Path("/benefit/{id}")
  public Response findBenefitById(@PathParam("id") Long id) {
    var result = service.findBenefitById(id);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @GET
  @Path("/benefit")
  public Response listBenefits(
      @QueryParam("sponsorId") Long sponsorId,
      @QueryParam("isActive") Boolean isActive,
      @QueryParam("page") Integer page,
      @QueryParam("size") Integer size) {
    Pageable<BenefitDTO> result = service.listBenefits(sponsorId, isActive, PageDTO.of(page, size));
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @PATCH
  @Path("/benefit/{id}/deactivate")
  public Response deactivateBenefit(@PathParam("id") Long id) {
    service.deactivateBenefit(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }

  @DELETE
  @Path("/benefit/{id}")
  public Response deleteBenefit(@PathParam("id") Long id) {
    service.deleteBenefit(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }
}
