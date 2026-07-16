package backoffice.v1.openapi.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.checkin.AdminCheckinDTO;

@Schema(description = "Resposta padrão da API com lista paginada global de check-ins (ADM)")
public class EnvelopeAdminCheckinListDTO extends ResponseModel<List<AdminCheckinDTO>> {
}
