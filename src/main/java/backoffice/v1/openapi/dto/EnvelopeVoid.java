package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;

@Schema(description = "Resposta padrão da API sem payload em data (pode ser null)")
public class EnvelopeVoid extends ResponseModel<Object> {
}
