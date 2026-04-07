package backoffice.v1.openapi.api;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import backoffice.v1.dtos.publics.ListPublicSponsorsQueryDTO;
import backoffice.v1.openapi.dto.EnvelopePublicSponsorListDTO;
import backoffice.v1.openapi.dto.EnvelopeErrorDTO;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/public")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Público", description = "Endpoints sem autenticação JWT")
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
public interface PublicApi {

  @GET
  @Path("/sponsors")
  @Operation(
      summary = "Listar patrocinadores ativos (público)",
      description = "Listagem paginada de usuários tipo SPONSOR com conta ativa, patrocinador ativo e filtro opcional por tier. "
          + "Sem dados sensíveis do usuário; apenas campos públicos acordados.")
  @APIResponses({
      @APIResponse(
          responseCode = "200",
          description = "Lista paginada",
          content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopePublicSponsorListDTO.class))),
      @APIResponse(
          responseCode = "400",
          description = "Validação de query params",
          content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeErrorDTO.class))),
  })
  Response listSponsors(@Valid @BeanParam ListPublicSponsorsQueryDTO query);
}
