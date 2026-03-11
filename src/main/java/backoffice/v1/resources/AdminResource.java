package backoffice.v1.resources;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

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

  // ===================== User =====================

  @POST
  @Path("/user")
  @Tag(name = "Admin - Usuários")
  @Operation(summary = "Criar usuário", description = "Cria um novo usuário com dados opcionais de patrocinador. Tipo MEMBER e SPONSOR_MEMBER ainda não implementados.")
  public Response createUser(@Valid UserWithSponsorCreateDTO dto) {
    var result = service.createUser(dto);
    var response = ResponseModel.success(Status.CREATED.getStatusCode(), result);
    return Response.status(Status.CREATED).entity(response).build();
  }

  @PUT
  @Path("/user/{id}")
  @Tag(name = "Admin - Usuários")
  @Operation(summary = "Atualizar usuário", description = "Atualização parcial do usuário e do patrocinador vinculado, quando aplicável.")
  public Response updateUser(@PathParam("id") Long id, @Valid UserWithSponsorUpdateDTO dto) {
    var result = service.updateUser(id, dto);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @PATCH
  @Path("/user/{id}/deactivate")
  @Tag(name = "Admin - Usuários")
  @Operation(summary = "Desativar usuário", description = "Desativação lógica. Se SPONSOR, desativa também o patrocinador e seus benefícios.")
  public Response deactivateUser(@PathParam("id") Long id) {
    service.deactivateUser(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }

  @DELETE
  @Path("/user/{id}")
  @Tag(name = "Admin - Usuários")
  @Operation(summary = "Excluir usuário", description = "Exclusão física e irreversível. Remove em cascata sponsor e benefícios vinculados.")
  public Response deleteUser(@PathParam("id") Long id) {
    service.deleteUser(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }

  @GET
  @Path("/user/{id}")
  @Tag(name = "Admin - Usuários")
  @Operation(summary = "Buscar usuário por ID", description = "Retorna o usuário com dados de patrocinador embutidos quando aplicável.")
  public Response findUserById(@PathParam("id") Long id) {
    var result = service.findUserById(id);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @GET
  @Path("/user")
  @Tag(name = "Admin - Usuários")
  @Operation(summary = "Listar usuários", description = "Listagem paginada com filtros por tipo, tier e status de ativação.")
  public Response listUsers(
      @Parameter(description = "Tipo do usuário") @QueryParam("type") UserTypeEnum type,
      @Parameter(description = "Tier do patrocinador") @QueryParam("tier") SponsorTierEnum tier,
      @Parameter(description = "Filtrar por conta ativa/inativa") @QueryParam("isActive") Boolean isActive,
      @Parameter(description = "Número da página (0-based)") @QueryParam("page") Integer page,
      @Parameter(description = "Itens por página") @QueryParam("size") Integer size) {
    Pageable<UserWithSponsorDTO> result = service.listUsers(type, tier, isActive, PageDTO.of(page, size));
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  // ===================== Benefit =====================

  @POST
  @Path("/benefit")
  @Tag(name = "Admin - Benefícios")
  @Operation(summary = "Criar benefício", description = "Cria um benefício geral ou vinculado a um patrocinador. Se sponsorId informado, valida existência.")
  public Response createBenefit(@Valid BenefitCreateDTO dto) {
    var result = service.createBenefit(dto);
    var response = ResponseModel.success(Status.CREATED.getStatusCode(), result);
    return Response.status(Status.CREATED).entity(response).build();
  }

  @PUT
  @Path("/benefit/{id}")
  @Tag(name = "Admin - Benefícios")
  @Operation(summary = "Atualizar benefício", description = "Atualização parcial. Permite vincular/desvincular patrocinador via sponsorId.")
  public Response updateBenefit(@PathParam("id") Long id, @Valid BenefitUpdateDTO dto) {
    var result = service.updateBenefit(id, dto);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @GET
  @Path("/benefit/{id}")
  @Tag(name = "Admin - Benefícios")
  @Operation(summary = "Buscar benefício por ID", description = "Retorna o benefício com dados mínimos do patrocinador quando vinculado.")
  public Response findBenefitById(@PathParam("id") Long id) {
    var result = service.findBenefitById(id);
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @GET
  @Path("/benefit")
  @Tag(name = "Admin - Benefícios")
  @Operation(summary = "Listar benefícios", description = "Listagem paginada com filtros por patrocinador e status de ativação.")
  public Response listBenefits(
      @Parameter(description = "ID do patrocinador") @QueryParam("sponsorId") Long sponsorId,
      @Parameter(description = "Filtrar por benefício ativo/inativo") @QueryParam("isActive") Boolean isActive,
      @Parameter(description = "Número da página (0-based)") @QueryParam("page") Integer page,
      @Parameter(description = "Itens por página") @QueryParam("size") Integer size) {
    Pageable<BenefitDTO> result = service.listBenefits(sponsorId, isActive, PageDTO.of(page, size));
    var response = ResponseModel.success(Status.OK.getStatusCode(), result);
    return Response.ok(response).build();
  }

  @PATCH
  @Path("/benefit/{id}/deactivate")
  @Tag(name = "Admin - Benefícios")
  @Operation(summary = "Desativar benefício", description = "Desativação lógica (soft delete). Define isActive como false.")
  public Response deactivateBenefit(@PathParam("id") Long id) {
    service.deactivateBenefit(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }

  @DELETE
  @Path("/benefit/{id}")
  @Tag(name = "Admin - Benefícios")
  @Operation(summary = "Excluir benefício", description = "Exclusão física e irreversível do benefício.")
  public Response deleteBenefit(@PathParam("id") Long id) {
    service.deleteBenefit(id);
    var response = ResponseModel.success(Status.OK.getStatusCode());
    return Response.ok(response).build();
  }
}
