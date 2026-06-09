package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.member.MemberDTO;

@Schema(description = "Resposta padrão da API com um membro")
public class EnvelopeMemberDTO extends ResponseModel<MemberDTO> {
}
