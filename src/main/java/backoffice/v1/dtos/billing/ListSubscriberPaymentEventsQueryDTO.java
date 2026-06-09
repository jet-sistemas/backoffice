package backoffice.v1.dtos.billing;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import backoffice.v1.dtos.common.PageDTO;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListSubscriberPaymentEventsQueryDTO {

  @Parameter(description = "Número da página (1-based)")
  @QueryParam("page")
  private Integer page;

  @Parameter(description = "Itens por página")
  @QueryParam("size")
  private Integer size;

  public PageDTO resolvePage() {
    return PageDTO.of(page, size);
  }
}
