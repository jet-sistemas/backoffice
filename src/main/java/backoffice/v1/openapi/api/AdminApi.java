package backoffice.v1.openapi.api;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import backoffice.v1.dtos.benefit.BenefitCreateDTO;
import backoffice.v1.dtos.benefit.BenefitUpdateDTO;
import backoffice.v1.dtos.member.SubscriberMemberUpdateDTO;
import backoffice.v1.dtos.user.ListUsersQueryDTO;
import backoffice.v1.dtos.user.UserWithSponsorCreateDTO;
import backoffice.v1.dtos.user.UserWithSponsorUpdateDTO;
import backoffice.v1.openapi.dto.EnvelopeBenefitDTO;
import backoffice.v1.openapi.dto.EnvelopeBenefitListDTO;
import backoffice.v1.openapi.dto.EnvelopeErrorDTO;
import backoffice.v1.openapi.dto.EnvelopeMemberDTO;
import backoffice.v1.openapi.dto.EnvelopeUserWithSponsorDTO;
import backoffice.v1.openapi.dto.EnvelopeUserWithSponsorListDTO;
import backoffice.v1.openapi.dto.EnvelopeVoid;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/admin")
@RolesAllowed({ "ADM" })
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@APIResponses({
// @APIResponse(responseCode = "400", description = "Validação (Bean
// Validation), requisição inválida (BadRequestException) ou regra de negócio
// com HTTP 400 (BusinessException)", content = @Content(mediaType =
// MediaType.APPLICATION_JSON, schema = @Schema(implementation =
// EnvelopeErrorDTO.class))),
// @APIResponse(responseCode = "403", description = "Acesso negado
// (ForbiddenException)", content = @Content(mediaType =
// MediaType.APPLICATION_JSON, schema = @Schema(implementation =
// EnvelopeErrorDTO.class))),
// @APIResponse(responseCode = "404", description = "Recurso não encontrado
// (NotFoundException)", content = @Content(mediaType =
// MediaType.APPLICATION_JSON, schema = @Schema(implementation =
// EnvelopeErrorDTO.class))),
// @APIResponse(responseCode = "409", description = "Conflito
// (ConflictException)", content = @Content(mediaType =
// MediaType.APPLICATION_JSON, schema = @Schema(implementation =
// EnvelopeErrorDTO.class))),
// @APIResponse(responseCode = "500", description = "Erro interno ou exceção não
// mapeada; corpo com mensagem padronizada e, em desenvolvimento, `stackTrace`",
// content = @Content(mediaType = MediaType.APPLICATION_JSON, schema =
// @Schema(implementation = EnvelopeErrorDTO.class))),
})
public interface AdminApi {

	@POST
	@Path("/user")
	@Tag(name = "Admin - Usuários")
	@Operation(summary = "Criar usuário", description = "Cria um novo usuário com dados opcionais de patrocinador ou membro, de acordo com o tipo.")
	@APIResponses({
			@APIResponse(responseCode = "201", description = "Usuário criado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeUserWithSponsorDTO.class)))
	})
	Response createUser(@Valid UserWithSponsorCreateDTO dto);

	@PUT
	@Path("/user/{id}")
	@Tag(name = "Admin - Usuários")
	@Operation(summary = "Atualizar usuário", description = "Atualização parcial do usuário e do patrocinador vinculado, quando aplicável.")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Usuário atualizado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeUserWithSponsorDTO.class)))
	})
	Response updateUser(@PathParam("id") Long id, @Valid UserWithSponsorUpdateDTO dto);

	@PATCH
	@Path("/user/{id}/deactivate")
	@Tag(name = "Admin - Usuários")
	@Operation(summary = "Desativar usuário", description = "Desativação lógica. Se SPONSOR, desativa também o patrocinador e benefícios. Se MEMBER, desativa o registro de membro e status de assinante quando houver.")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Usuário desativado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeVoid.class)))
	})
	Response deactivateUser(@PathParam("id") Long id);

	@PATCH
	@Path("/user/{id}/activate")
	@Tag(name = "Admin - Usuários")
	@Operation(summary = "Ativar usuário", description = "Reativa a conta (`isAccountActive = true`). Se o tipo for SPONSOR ou SPONSOR_MEMBER, reativa o registro de patrocinador e os benefícios vinculados a esse patrocinador.")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Usuário ativado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeVoid.class)))
	})
	Response activateUser(@PathParam("id") Long id);

	@DELETE
	@Path("/user/{id}")
	@Tag(name = "Admin - Usuários")
	@Operation(summary = "Excluir usuário", description = "Exclusão física e irreversível. Remove em cascata patrocinador/benefícios ou dados de membro conforme o tipo.")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Usuário excluído", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeVoid.class)))
	})
	Response deleteUser(@PathParam("id") Long id);

	@GET
	@Path("/user/{id}")
	@Tag(name = "Admin - Usuários")
	@Operation(summary = "Buscar usuário por ID", description = "Retorna o usuário com patrocinador e/ou membro embutidos quando aplicável.")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Usuário encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeUserWithSponsorDTO.class)))
	})
	Response findUserById(@PathParam("id") Long id);

	@GET
	@Path("/user")
	@Tag(name = "Admin - Usuários")
	@Operation(summary = "Listar usuários", description = "Listagem paginada com filtros por tipo, tier, entityType e persona do patrocinador, memberType (apenas com type=MEMBER), status da conta e busca textual. Com type=MEMBER, a busca inclui nome completo e WhatsApp do membro.")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Lista paginada de usuários", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeUserWithSponsorListDTO.class)))
	})
	Response listUsers(@Valid @BeanParam ListUsersQueryDTO query);

	@PATCH
	@Path("/user/{id}/subscriber")
	@Tag(name = "Admin - Usuários")
	@Operation(summary = "Atualizar mensalidade do assinante", description = "Atualização parcial de subscriber_member. O id é o do usuário (tipo MEMBER, subtipo SUBSCRIBER).")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Assinante atualizado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeMemberDTO.class)))
	})
	Response patchSubscriberMemberByUserId(@PathParam("id") Long id, @Valid SubscriberMemberUpdateDTO dto);

	@POST
	@Path("/benefit")
	@Tag(name = "Admin - Benefícios")
	@Operation(summary = "Criar benefício", description = "Cria um benefício geral ou vinculado a um patrocinador. Se sponsorId informado, valida existência.")
	@APIResponses({
			@APIResponse(responseCode = "201", description = "Benefício criado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeBenefitDTO.class)))
	})
	Response createBenefit(@Valid BenefitCreateDTO dto);

	@PUT
	@Path("/benefit/{id}")
	@Tag(name = "Admin - Benefícios")
	@Operation(summary = "Atualizar benefício", description = "Atualização parcial. Permite vincular/desvincular patrocinador via sponsorId.")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Benefício atualizado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeBenefitDTO.class)))
	})
	Response updateBenefit(@PathParam("id") Long id, @Valid BenefitUpdateDTO dto);

	@GET
	@Path("/benefit/{id}")
	@Tag(name = "Admin - Benefícios")
	@Operation(summary = "Buscar benefício por ID", description = "Retorna o benefício com dados mínimos do patrocinador quando vinculado.")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Benefício encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeBenefitDTO.class)))
	})
	Response findBenefitById(@PathParam("id") Long id);

	@GET
	@Path("/benefit")
	@Tag(name = "Admin - Benefícios")
	@Operation(summary = "Listar benefícios", description = "Listagem paginada com filtros por patrocinador e status de ativação.")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Lista paginada de benefícios", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeBenefitListDTO.class)))
	})
	Response listBenefits(
			@Parameter(description = "ID do patrocinador") @QueryParam("sponsorId") Long sponsorId,
			@Parameter(description = "Filtrar por benefício ativo/inativo") @QueryParam("isActive") Boolean isActive,
			@Parameter(description = "Número da página (0-based)") @QueryParam("page") Integer page,
			@Parameter(description = "Itens por página") @QueryParam("size") Integer size);

	@PATCH
	@Path("/benefit/{id}/deactivate")
	@Tag(name = "Admin - Benefícios")
	@Operation(summary = "Desativar benefício", description = "Desativação lógica (soft delete). Define isActive como false.")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Benefício desativado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeVoid.class)))
	})
	Response deactivateBenefit(@PathParam("id") Long id);

	@PATCH
	@Path("/benefit/{id}/activate")
	@Tag(name = "Admin - Benefícios")
	@Operation(summary = "Reativar benefício", description = "Reativa o benefício (isActive = true) se puder: benefício geral, ou vinculado a patrocinador ativo. Idempotente se já ativo.")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Benefício reativado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeVoid.class)))
	})
	Response activateBenefit(@PathParam("id") Long id);

	@DELETE
	@Path("/benefit/{id}")
	@Tag(name = "Admin - Benefícios")
	@Operation(summary = "Excluir benefício", description = "Exclusão física e irreversível do benefício.")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Benefício excluído", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeVoid.class)))
	})
	Response deleteBenefit(@PathParam("id") Long id);
}
