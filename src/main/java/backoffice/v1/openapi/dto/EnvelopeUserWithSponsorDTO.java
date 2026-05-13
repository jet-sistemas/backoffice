package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.user.UserWithSponsorDTO;

@Schema(description = "Resposta padrão da API com um usuário (patrocinador e/ou membro quando aplicável)")
public class EnvelopeUserWithSponsorDTO extends ResponseModel<UserWithSponsorDTO> {
}
