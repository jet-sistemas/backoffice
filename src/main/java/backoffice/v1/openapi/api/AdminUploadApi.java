package backoffice.v1.openapi.api;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import backoffice.v1.dtos.upload.UploadConfirmDTO;
import backoffice.v1.dtos.upload.UploadDeleteDTO;
import backoffice.v1.dtos.upload.UploadInitDTO;
import backoffice.v1.openapi.dto.EnvelopeErrorDTO;
import backoffice.v1.openapi.dto.EnvelopeUploadConfirmDTO;
import backoffice.v1.openapi.dto.EnvelopeUploadDeleteDTO;
import backoffice.v1.openapi.dto.EnvelopeUploadInitDTO;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/admin/uploads")
@RolesAllowed({ "ADM" })
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@APIResponses({
// @APIResponse(responseCode = "400", description = "Validação ou requisição
// inválida", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema
// = @Schema(implementation = EnvelopeErrorDTO.class))),
// @APIResponse(responseCode = "401", description = "Não autenticado"),
// @APIResponse(responseCode = "403", description = "Sem permissão"),
// @APIResponse(responseCode = "404", description = "Recurso ou objeto não
// encontrado", content = @Content(mediaType = MediaType.APPLICATION_JSON,
// schema = @Schema(implementation = EnvelopeErrorDTO.class))),
// @APIResponse(responseCode = "413", description = "Arquivo excede o tamanho
// máximo", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema =
// @Schema(implementation = EnvelopeErrorDTO.class))),
// @APIResponse(responseCode = "415", description = "Content-Type não
// suportado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema
// = @Schema(implementation = EnvelopeErrorDTO.class))),
// @APIResponse(responseCode = "429", description = "Limite de requisições de
// upload", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema =
// @Schema(implementation = EnvelopeErrorDTO.class))),
// @APIResponse(responseCode = "503", description = "Armazenamento não
// configurado", content = @Content(mediaType = MediaType.APPLICATION_JSON,
// schema = @Schema(implementation = EnvelopeErrorDTO.class))),
// @APIResponse(responseCode = "500", description = "Erro interno", content =
// @Content(mediaType = MediaType.APPLICATION_JSON, schema =
// @Schema(implementation = EnvelopeErrorDTO.class))),
})
public interface AdminUploadApi {

    @POST
    @Path("/init")
    @Tag(name = "Admin - Uploads")
    @Operation(summary = "Iniciar upload", description = "Retorna URL assinada (PUT) para envio direto ao R2. entity: user (avatar) ou sponsor (logo); entityId: id do User ou do Sponsor.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "URL gerada", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeUploadInitDTO.class))),
    })
    Response initUpload(@Valid UploadInitDTO dto);

    @POST
    @Path("/confirm")
    @Tag(name = "Admin - Uploads")
    @Operation(summary = "Confirmar upload", description = "Valida o objeto no bucket e persiste avatar_url ou logo_url (path relativo).")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Vínculo atualizado", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeUploadConfirmDTO.class))),
    })
    Response confirmUpload(@Valid UploadConfirmDTO dto);

    @DELETE
    @Tag(name = "Admin - Uploads")
    @Operation(summary = "Remover imagem", description = "Remove o objeto no R2 e limpa avatar_url ou logo_url quando a chave coincide com a atual.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Sucesso (idempotente se já não houver imagem)", content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = EnvelopeUploadDeleteDTO.class))),
    })
    Response deleteUpload(@Valid UploadDeleteDTO dto);
}
