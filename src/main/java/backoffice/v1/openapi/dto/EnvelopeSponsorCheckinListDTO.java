package backoffice.v1.openapi.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.sponsor.SponsorCheckinDTO;

@Schema(description = "Resposta padrão da API com lista paginada de check-ins")
public class EnvelopeSponsorCheckinListDTO extends ResponseModel<List<SponsorCheckinDTO>> {
}
