package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.member.MemberPaymentHistoryDTO;

@Schema(description = "Resposta padrão da API com histórico de pagamentos do membro assinante")
public class EnvelopeMemberPaymentHistoryListDTO extends ResponseModel<MemberPaymentHistoryDTO> {
}
