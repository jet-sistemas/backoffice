package backoffice.v1.openapi.api;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import backoffice.v1.dtos.accountvalidation.AccountValidationRequestDTO;
import backoffice.v1.openapi.dto.EnvelopeAccountValidationResultDTO;
import backoffice.v1.openapi.dto.EnvelopeErrorDTO;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/account-validation")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Account Validation", description = "Validação pública de conta por convite")
public interface AccountValidationApi {

  @POST
  @Path("/confirm")
  @Operation(summary = "Confirmar validação de conta", description = "Valida token, código e documento; ativa a conta.")
  @APIResponses({
      @APIResponse(responseCode = "200", description = "Conta validada", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeAccountValidationResultDTO.class))),
      @APIResponse(responseCode = "400", description = "Dados inválidos", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeErrorDTO.class))),
      @APIResponse(responseCode = "403", description = "Convite bloqueado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeErrorDTO.class)))
  })
  Response confirm(@Valid AccountValidationRequestDTO dto);
}
