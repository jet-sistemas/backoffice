package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;

@Schema(
    description = "Envelope de erro retornado pelo GlobalExceptionMapper. `status` = ERROR e `statusCode` espelha o HTTP. "
        + "Em 400 por Bean Validation, prefira `messages`; nos demais casos, `message`. Em 500, `stackTrace` pode aparecer em desenvolvimento.")
public class EnvelopeErrorDTO extends ResponseModel<Object> {
}
