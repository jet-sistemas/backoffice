package backoffice.v1.openapi.dto;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.accountvalidation.ResendAccountValidationDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "EnvelopeResendAccountValidation")
public class EnvelopeResendAccountValidationDTO extends ResponseModel<ResendAccountValidationDTO> {
}
