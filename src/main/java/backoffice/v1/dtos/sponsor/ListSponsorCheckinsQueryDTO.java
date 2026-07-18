package backoffice.v1.dtos.sponsor;

import java.time.LocalDate;

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
public class ListSponsorCheckinsQueryDTO {

  @Parameter(description = "Data inicial (inclusive), formato YYYY-MM-DD")
  @QueryParam("startDate")
  private LocalDate startDate;

  @Parameter(description = "Data final (inclusive), formato YYYY-MM-DD")
  @QueryParam("endDate")
  private LocalDate endDate;

  @Parameter(description = "Página (1-based)")
  @QueryParam("page")
  private Integer page;

  @Parameter(description = "Tamanho da página")
  @QueryParam("size")
  private Integer size;

  public PageDTO toPageDTO() {
    return PageDTO.of(page, size);
  }
}
