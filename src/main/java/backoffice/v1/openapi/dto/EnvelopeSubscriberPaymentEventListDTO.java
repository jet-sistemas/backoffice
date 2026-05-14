package backoffice.v1.openapi.dto;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.billing.SubscriberPaymentEventDTO;

@Schema(description = "Histórico de eventos da mensalidade (paginado)")
public class EnvelopeSubscriberPaymentEventListDTO extends ResponseModel<List<SubscriberPaymentEventDTO>> {
}
