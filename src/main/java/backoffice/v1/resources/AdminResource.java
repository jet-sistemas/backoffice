package backoffice.v1.resources;

import backoffice.common.database.Pageable;
import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.benefit.BenefitCreateDTO;
import backoffice.v1.dtos.benefit.BenefitDTO;
import backoffice.v1.dtos.benefit.BenefitUpdateDTO;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.member.ListMembersQueryDTO;
import backoffice.v1.dtos.member.MemberCreateDTO;
import backoffice.v1.dtos.member.MemberDTO;
import backoffice.v1.dtos.user.ListUsersQueryDTO;
import backoffice.v1.dtos.user.UserWithSponsorCreateDTO;
import backoffice.v1.dtos.user.UserWithSponsorDTO;
import backoffice.v1.dtos.user.UserWithSponsorUpdateDTO;
import backoffice.v1.openapi.api.AdminApi;
import backoffice.v1.services.AdminService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

public class AdminResource implements AdminApi {
  @Inject
  private AdminService service;

  @Override
  public Response createUser(UserWithSponsorCreateDTO dto) {
    var result = service.createUser(dto);
    var response = ResponseModel.success(Status.CREATED.getStatusCode(), result);
    return Response.status(Status.CREATED).entity(response).build();
  }

  @Override
  public Response updateUser(Long id, UserWithSponsorUpdateDTO dto) {
    var result = service.updateUser(id, dto);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response deactivateUser(Long id) {
    service.deactivateUser(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }

  @Override
  public Response activateUser(Long id) {
    service.activateUser(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }

  @Override
  public Response deleteUser(Long id) {
    service.deleteUser(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }

  @Override
  public Response findUserById(Long id) {
    var result = service.findUserById(id);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response listUsers(@Valid @BeanParam ListUsersQueryDTO query) {
    Pageable<UserWithSponsorDTO> result = service.listUsers(
        query.resolveType(),
        query.resolveTier(),
        query.resolveEntityType(),
        query.resolvePersona(),
        query.getIsActive(),
        query.resolveSearch(),
        PageDTO.of(query.getPage(), query.getSize()));
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response createMember(MemberCreateDTO dto) {
    MemberDTO result = service.createMember(dto);
    var response = ResponseModel.success(Status.CREATED.getStatusCode(), result);
    return Response.status(Status.CREATED).entity(response).build();
  }

  @Override
  public Response findMemberById(Long id) {
    MemberDTO result = service.findMemberById(id);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response listMembers(@Valid @BeanParam ListMembersQueryDTO query) {
    Pageable<MemberDTO> result = service.listMembers(
        query.resolveType(),
        query.getIsActive(),
        query.resolveSearch(),
        PageDTO.of(query.getPage(), query.getSize()));
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response createBenefit(BenefitCreateDTO dto) {
    var result = service.createBenefit(dto);
    var response = ResponseModel.success(Status.CREATED.getStatusCode(), result);
    return Response.status(Status.CREATED).entity(response).build();
  }

  @Override
  public Response updateBenefit(Long id, BenefitUpdateDTO dto) {
    var result = service.updateBenefit(id, dto);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response findBenefitById(Long id) {
    var result = service.findBenefitById(id);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response listBenefits(Long sponsorId, Boolean isActive, Integer page, Integer size) {
    Pageable<BenefitDTO> result = service.listBenefits(sponsorId, isActive, PageDTO.of(page, size));
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @Override
  public Response deactivateBenefit(Long id) {
    service.deactivateBenefit(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }

  @Override
  public Response activateBenefit(Long id) {
    service.activateBenefit(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }

  @Override
  public Response deleteBenefit(Long id) {
    service.deleteBenefit(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }
}
