package backoffice.v1.dtos.billing;

import java.time.LocalDate;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import backoffice.common.validators.EnumConstraint;
import backoffice.v1.dtos.common.PageDTO;
import jakarta.validation.constraints.Size;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListSubscriberBillingQueryDTO {

  @Parameter(description = "Filtro por status da mensalidade; ALL para todos.")
  @QueryParam("status")
  @EnumConstraint(enumClass = SubscriberBillingListStatusFilter.class, message = "Status de filtro inválido.")
  private String status;

  @Parameter(description = "Vencimento inicial (inclusivo)")
  @QueryParam("dueFrom")
  private LocalDate dueFrom;

  @Parameter(description = "Vencimento final (inclusivo)")
  @QueryParam("dueTo")
  private LocalDate dueTo;

  @Parameter(description = "Busca textual: nome, e-mail, documento, código ou WhatsApp")
  @QueryParam("search")
  @Size(max = 100, message = "O termo de busca excede o tamanho máximo permitido.")
  private String search;

  @Parameter(description = "Número da página (1-based)")
  @QueryParam("page")
  private Integer page;

  @Parameter(description = "Itens por página")
  @QueryParam("size")
  private Integer size;

  public SubscriberBillingListStatusFilter resolveStatusFilter() {
    if (status == null || status.isBlank()) {
      return SubscriberBillingListStatusFilter.ALL;
    }
    return SubscriberBillingListStatusFilter.valueOf(status.trim().toUpperCase());
  }

  public String resolveSearch() {
    if (search == null || search.isBlank()) {
      return null;
    }
    return search.trim();
  }

  public PageDTO resolvePage() {
    return PageDTO.of(page, size);
  }
}
