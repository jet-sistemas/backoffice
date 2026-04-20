package backoffice.v1.openapi.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.user.UserWithSponsorDTO;

@Schema(description = "Resposta paginada: data é a lista de usuários; totalElements, totalPages, pageSize e currentPage quando aplicável")
public class EnvelopeUserWithSponsorListDTO extends ResponseModel<List<UserWithSponsorDTO>> {
}
