package backoffice.v1.dtos.member;

import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import backoffice.common.validators.EnumConstraint;
import backoffice.v1.entities.enums.MemberTypeEnum;
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
public class ListMembersQueryDTO {
  @Parameter(description = "Tipo de membro")
  @QueryParam("type")
  @EnumConstraint(enumClass = MemberTypeEnum.class, message = "Tipo de membro inválido.")
  private String type;

  @Parameter(description = "Filtrar por membro ativo/inativo")
  @QueryParam("isActive")
  private Boolean isActive;

  @Parameter(description = "Busca textual: nome completo, e-mail, documento, código ou WhatsApp")
  @QueryParam("search")
  @Size(max = 100, message = "O termo de busca excede o tamanho máximo permitido.")
  private String search;

  @Parameter(description = "Número da página (0-based)")
  @QueryParam("page")
  private Integer page;

  @Parameter(description = "Itens por página")
  @QueryParam("size")
  private Integer size;

  public MemberTypeEnum resolveType() {
    if (type == null || type.isBlank()) {
      return null;
    }
    return MemberTypeEnum.valueOf(type.trim().toUpperCase());
  }

  public String resolveSearch() {
    if (search == null || search.isBlank()) {
      return null;
    }
    return search.trim();
  }
}
