package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.benefit.BenefitDTO;

@Schema(description = "Resposta padrão da API com um benefício")
public class EnvelopeBenefitDTO extends ResponseModel<BenefitDTO> {
}
