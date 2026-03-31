package backoffice.v1.openapi.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.publics.PublicSponsorItemDTO;

@Schema(description = "Resposta paginada: data é a lista de patrocinadores públicos; totalElements, totalPages, pageSize e currentPage quando aplicável")
public class EnvelopePublicSponsorListDTO extends ResponseModel<List<PublicSponsorItemDTO>> {
}
