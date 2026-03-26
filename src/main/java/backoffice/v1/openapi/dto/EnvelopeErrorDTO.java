package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;

@Schema(
    description = "Envelope de erro padronizado (`ResponseModel`). `status` = ERROR e `statusCode` espelha o HTTP. "
        + "Em 400 por Bean Validation na borda REST, `RestValidationExceptionMapper` preenche `messages`; "
        + "demais erros passam pelo `GlobalExceptionMapper` (prefira `message` ou `messages` conforme o caso). "
        + "Em 500, `stackTrace` pode aparecer em desenvolvimento.")
public class EnvelopeErrorDTO extends ResponseModel<Object> {
}
