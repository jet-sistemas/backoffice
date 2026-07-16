package backoffice.v1.openapi.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.member.MemberCheckinHistoryDTO;

@Schema(description = "Resposta padrão da API com lista paginada de check-ins do membro")
public class EnvelopeMemberCheckinHistoryListDTO extends ResponseModel<List<MemberCheckinHistoryDTO>> {
}
