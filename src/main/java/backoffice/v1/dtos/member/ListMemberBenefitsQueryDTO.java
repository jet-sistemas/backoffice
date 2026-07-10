package backoffice.v1.dtos.member;

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
public class ListMemberBenefitsQueryDTO {

  @Parameter(description = "Filtrar por patrocinador ativo (opcional)")
  @QueryParam("sponsorId")
  private Long sponsorId;

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
