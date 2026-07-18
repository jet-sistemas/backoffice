package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.member.MemberAccountStatusDTO;

@Schema(description = "Resposta padrão da API com situação da conta do membro assinante")
public class EnvelopeMemberAccountStatusDTO extends ResponseModel<MemberAccountStatusDTO> {
}
