package backoffice.v1.openapi.api;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import backoffice.v1.dtos.sponsor.ListSponsorBenefitsQueryDTO;
import backoffice.v1.dtos.sponsor.ListSponsorCheckinsQueryDTO;
import backoffice.v1.dtos.sponsor.SponsorCheckinCreateDTO;
import backoffice.v1.dtos.sponsor.SponsorMemberLookupQueryDTO;
import backoffice.v1.openapi.dto.EnvelopeBenefitListDTO;
import backoffice.v1.openapi.dto.EnvelopeSponsorCheckinDTO;
import backoffice.v1.openapi.dto.EnvelopeSponsorCheckinListDTO;
import backoffice.v1.openapi.dto.EnvelopeSponsorMemberPreviewDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/sponsor")
@RolesAllowed({ "SPONSOR", "SPONSOR_MEMBER" })
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Sponsor", description = "Área logada do patrocinador: check-in, histórico e benefícios")
public interface SponsorApi {

  @GET
  @Path("/checkins/member-preview")
  @Operation(summary = "Pré-visualizar membro para check-in", description = "Busca membro por código ou CPF e retorna dados mínimos, aptidão e check-in de hoje.")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Preview do membro", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeSponsorMemberPreviewDTO.class)))
  })
  Response previewMember(@Valid @BeanParam SponsorMemberLookupQueryDTO query);

  @POST
  @Path("/checkins")
  @Operation(summary = "Registrar check-in", description = "Registra check-in validado ou tentativa não validada. Exige confirmação se já houver check-in hoje.")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Check-in registrado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeSponsorCheckinDTO.class)))
  })
  Response createCheckin(@Valid SponsorCheckinCreateDTO dto);

  @GET
  @Path("/checkins")
  @Operation(summary = "Histórico de check-ins", description = "Lista check-ins do patrocinador logado com filtro de data e paginação.")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Lista paginada", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeSponsorCheckinListDTO.class)))
  })
  Response listCheckins(@Valid @BeanParam ListSponsorCheckinsQueryDTO query);

  @GET
  @Path("/benefits")
  @Operation(summary = "Benefícios do patrocinador", description = "Lista benefícios ativos vinculados ao patrocinador logado (somente leitura).")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Lista paginada", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeBenefitListDTO.class)))
  })
  Response listBenefits(@Valid @BeanParam ListSponsorBenefitsQueryDTO query);
}
