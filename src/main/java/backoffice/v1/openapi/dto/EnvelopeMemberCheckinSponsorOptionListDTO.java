package backoffice.v1.openapi.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.member.MemberCheckinSponsorOptionDTO;

@Schema(description = "Resposta padrão da API com opções de patrocinadores do histórico do membro")
public class EnvelopeMemberCheckinSponsorOptionListDTO extends ResponseModel<List<MemberCheckinSponsorOptionDTO>> {
}
