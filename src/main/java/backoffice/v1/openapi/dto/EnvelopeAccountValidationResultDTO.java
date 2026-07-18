package backoffice.v1.openapi.dto;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.accountvalidation.AccountValidationResultDTO;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "EnvelopeAccountValidationResult")
public class EnvelopeAccountValidationResultDTO extends ResponseModel<AccountValidationResultDTO> {
}
