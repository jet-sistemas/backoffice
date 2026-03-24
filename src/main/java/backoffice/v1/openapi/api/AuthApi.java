package backoffice.v1.openapi.api;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import backoffice.v1.dtos.auth.AuthCreateDTO;
import backoffice.v1.openapi.dto.EnvelopeAuthDTO;
import backoffice.v1.openapi.dto.EnvelopeAuthExtDTO;
import backoffice.v1.openapi.dto.EnvelopeErrorDTO;
import io.quarkus.security.Authenticated;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Auth", description = "Autenticação e dados do usuário logado")
@APIResponses({
		@APIResponse(responseCode = "400", description = "Validação (Bean Validation), requisição inválida (BadRequestException) ou regra de negócio com HTTP 400 (BusinessException)", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeErrorDTO.class))),
		@APIResponse(responseCode = "403", description = "Acesso negado (ForbiddenException)", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeErrorDTO.class))),
		@APIResponse(responseCode = "404", description = "Recurso não encontrado (NotFoundException)", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeErrorDTO.class))),
		@APIResponse(responseCode = "409", description = "Conflito (ConflictException)", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeErrorDTO.class))),
		@APIResponse(responseCode = "500", description = "Erro interno ou exceção não mapeada; corpo com mensagem padronizada e, em desenvolvimento, `stackTrace`", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeErrorDTO.class))),
})
public interface AuthApi {

	@POST
	@Operation(summary = "Login", description = "Autentica o usuário e retorna um token JWT")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Autenticação bem-sucedida", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeAuthDTO.class)))
	})
	Response signIn(@Valid AuthCreateDTO dto);

	@GET
	@Path("/me")
	@Authenticated
	@Operation(summary = "Dados do usuário logado", description = "Retorna os dados do usuário autenticado via token JWT")
	@APIResponses({
			@APIResponse(responseCode = "200", description = "Dados do usuário", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeAuthExtDTO.class)))
	})
	Response me(@Context SecurityContext ctx);
}
