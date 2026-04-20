package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.upload.UploadInitResponseDTO;

@Schema(description = "Resposta do início de upload (URL assinada e chave do objeto)")
public class EnvelopeUploadInitDTO extends ResponseModel<UploadInitResponseDTO> {
}
