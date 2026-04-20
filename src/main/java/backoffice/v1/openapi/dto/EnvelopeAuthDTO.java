package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.auth.AuthDTO;

@Schema(description = "Resposta padrão da API com dados de autenticação (token JWT)")
public class EnvelopeAuthDTO extends ResponseModel<AuthDTO> {
}
