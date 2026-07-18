package backoffice.v1.openapi.api;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import backoffice.v1.dtos.member.ListMemberBenefitsQueryDTO;
import backoffice.v1.dtos.member.ListMemberCheckinsQueryDTO;
import backoffice.v1.dtos.member.ListMemberPaymentsQueryDTO;
import backoffice.v1.openapi.dto.EnvelopeMemberBenefitListDTO;
import backoffice.v1.openapi.dto.EnvelopeMemberCardDTO;
import backoffice.v1.openapi.dto.EnvelopeMemberAccountStatusDTO;
import backoffice.v1.openapi.dto.EnvelopeMemberCheckinHistoryListDTO;
import backoffice.v1.openapi.dto.EnvelopeMemberCheckinSponsorOptionListDTO;
import backoffice.v1.openapi.dto.EnvelopeMemberPaymentHistoryListDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/member")
@RolesAllowed("MEMBER")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Member", description = "Área logada do membro")
public interface MemberApi {

  @GET
  @Path("/me/card")
  @Operation(summary = "Carteirinha do membro logado", description = "Retorna dados da carteirinha digital do membro autenticado.")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Carteirinha do membro", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeMemberCardDTO.class)))
  })
  Response getMyCard();

  @GET
  @Path("/me/account")
  @Operation(summary = "Situação da conta do membro logado", description = "Retorna status da mensalidade do membro assinante autenticado.")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Situação da conta", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeMemberAccountStatusDTO.class)))
  })
  Response getMyAccount();

  @GET
  @Path("/me/account/payments")
  @Operation(summary = "Histórico de pagamentos do membro logado", description = "Lista pagamentos registrados pelo ADM para o membro assinante autenticado.")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Lista paginada", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeMemberPaymentHistoryListDTO.class)))
  })
  Response listMyPayments(@Valid @BeanParam ListMemberPaymentsQueryDTO query);

  @GET
  @Path("/checkins")
  @Operation(summary = "Histórico de check-ins do membro", description = "Lista check-ins validados do membro logado com filtro por patrocinador, data e paginação.")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Lista paginada", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeMemberCheckinHistoryListDTO.class)))
  })
  Response listCheckins(@Valid @BeanParam ListMemberCheckinsQueryDTO query);

  @GET
  @Path("/checkins/sponsors")
  @Operation(summary = "Patrocinadores do histórico", description = "Lista patrocinadores presentes no histórico de check-ins validados do membro logado.")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Lista de patrocinadores", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeMemberCheckinSponsorOptionListDTO.class)))
  })
  Response listCheckinSponsors();

  @GET
  @Path("/benefits")
  @Operation(summary = "Benefícios do membro", description = "Lista benefícios ativos disponíveis no catálogo da plataforma para o membro logado.")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Lista paginada", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeMemberBenefitListDTO.class)))
  })
  Response listBenefits(@Valid @BeanParam ListMemberBenefitsQueryDTO query);
}
