package backoffice.v1.dtos.user;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import backoffice.common.validators.EnumConstraint;
import backoffice.v1.entities.enums.SponsorEntityTypeEnum;
import backoffice.v1.entities.enums.SponsorPersonaEnum;
import backoffice.v1.entities.enums.SponsorTierEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import jakarta.ws.rs.QueryParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListUsersQueryDTO {

  @Parameter(description = "Tipo do usuário")
  @QueryParam("type")
  @EnumConstraint(enumClass = UserTypeEnum.class, message = "Tipo de usuário inválido.")
  private String type;

  @Parameter(description = "Tier do patrocinador", schema = @Schema(enumeration = { "BRONZE", "SILVER", "GOLD" }))
  @QueryParam("tier")
  @EnumConstraint(enumClass = SponsorTierEnum.class, message = "Tier de patrocinador inválido.")
  private String tier;

  @Parameter(description = "Tipo de entidade do patrocinador", schema = @Schema(enumeration = {
      "PERSON", "COMPANY", "GOVERNMENT", "NGO" }))
  @QueryParam("entityType")
  @EnumConstraint(enumClass = SponsorEntityTypeEnum.class, message = "Tipo de entidade inválido.")
  private String entityType;

  @Parameter(description = "Persona (quando entityType = PERSON)", schema = @Schema(enumeration = {
      "POLITICIAN", "INFLUENCER", "ATHLETE", "OTHER" }))
  @QueryParam("persona")
  @EnumConstraint(enumClass = SponsorPersonaEnum.class, message = "Persona inválida.")
  private String persona;

  @Parameter(description = "Filtrar por conta ativa/inativa")
  @QueryParam("isActive")
  private Boolean isActive;

  @Parameter(description = "Número da página (0-based)")
  @QueryParam("page")
  private Integer page;

  @Parameter(description = "Itens por página")
  @QueryParam("size")
  private Integer size;

  public UserTypeEnum resolveType() {
    if (type == null || type.isBlank()) {
      return null;
    }
    return UserTypeEnum.valueOf(type.trim().toUpperCase());
  }

  public SponsorTierEnum resolveTier() {
    if (tier == null || tier.isBlank()) {
      return null;
    }
    return SponsorTierEnum.valueOf(tier.trim().toUpperCase());
  }

  public SponsorEntityTypeEnum resolveEntityType() {
    if (entityType == null || entityType.isBlank()) {
      return null;
    }
    return SponsorEntityTypeEnum.valueOf(entityType.trim().toUpperCase());
  }

  public SponsorPersonaEnum resolvePersona() {
    if (persona == null || persona.isBlank()) {
      return null;
    }
    return SponsorPersonaEnum.valueOf(persona.trim().toUpperCase());
  }
}
