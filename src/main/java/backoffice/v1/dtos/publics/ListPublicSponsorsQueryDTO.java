package backoffice.v1.dtos.publics;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import backoffice.common.validators.EnumConstraint;
import backoffice.v1.entities.enums.SponsorTierEnum;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListPublicSponsorsQueryDTO {

  @Parameter(description = "Tier do patrocinador", schema = @Schema(enumeration = { "BRONZE", "SILVER", "GOLD" }))
  @QueryParam("tier")
  @EnumConstraint(enumClass = SponsorTierEnum.class, message = "Tier de patrocinador inválido.")
  private String tier;

  @Parameter(description = "Número da página (1-based)")
  @QueryParam("page")
  private Integer page;

  @Parameter(description = "Itens por página")
  @QueryParam("size")
  private Integer size;

  public SponsorTierEnum resolveTier() {
    if (tier == null || tier.isBlank()) {
      return null;
    }
    return SponsorTierEnum.valueOf(tier.trim().toUpperCase());
  }
}
