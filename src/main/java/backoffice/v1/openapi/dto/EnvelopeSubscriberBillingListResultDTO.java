package backoffice.v1.openapi.dto;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import backoffice.common.requests.ResponseModel;
import backoffice.v1.dtos.billing.SubscriberBillingListResultDTO;

@Schema(description = "Listagem de mensalidades com resumo e paginação em data.rows")
public class EnvelopeSubscriberBillingListResultDTO extends ResponseModel<SubscriberBillingListResultDTO> {
}
