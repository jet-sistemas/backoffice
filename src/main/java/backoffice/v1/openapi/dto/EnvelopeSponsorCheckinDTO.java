package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.sponsor.SponsorCheckinDTO;

@Schema(description = "Resposta padrão da API com um check-in")
public class EnvelopeSponsorCheckinDTO extends ResponseModel<SponsorCheckinDTO> {
}
