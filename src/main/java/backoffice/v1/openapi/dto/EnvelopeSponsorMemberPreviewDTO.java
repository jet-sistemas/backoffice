package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.sponsor.SponsorMemberPreviewDTO;

@Schema(description = "Resposta padrão da API com preview do membro para check-in")
public class EnvelopeSponsorMemberPreviewDTO extends ResponseModel<SponsorMemberPreviewDTO> {
}
