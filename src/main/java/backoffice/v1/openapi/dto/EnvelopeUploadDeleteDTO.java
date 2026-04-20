package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.upload.UploadDeleteResponseDTO;

@Schema(description = "Resposta da exclusão de imagem no armazenamento")
public class EnvelopeUploadDeleteDTO extends ResponseModel<UploadDeleteResponseDTO> {
}
