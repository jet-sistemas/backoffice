package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.upload.UploadConfirmResponseDTO;

@Schema(description = "Resposta da confirmação de upload (vínculo persistido)")
public class EnvelopeUploadConfirmDTO extends ResponseModel<UploadConfirmResponseDTO> {
}
