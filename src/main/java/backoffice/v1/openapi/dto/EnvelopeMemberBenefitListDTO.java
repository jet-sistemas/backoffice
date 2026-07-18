package backoffice.v1.openapi.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.member.MemberBenefitDTO;

@Schema(description = "Resposta padrão da API com lista paginada de benefícios do membro")
public class EnvelopeMemberBenefitListDTO extends ResponseModel<List<MemberBenefitDTO>> {
}
