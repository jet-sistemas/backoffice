package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.auth.AuthExtDTO;

@Schema(description = "Resposta padrão da API com dados estendidos do usuário autenticado")
public class EnvelopeAuthExtDTO extends ResponseModel<AuthExtDTO> {
}
