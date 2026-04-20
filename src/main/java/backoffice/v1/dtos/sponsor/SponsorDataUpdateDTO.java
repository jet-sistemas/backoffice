package backoffice.v1.dtos.sponsor;

import backoffice.common.annotations.UnmaskNumber;
import backoffice.common.validators.EnumConstraint;
import backoffice.v1.entities.enums.SponsorEntityTypeEnum;
import backoffice.v1.entities.enums.SponsorPersonaEnum;
import backoffice.v1.entities.enums.SponsorTierEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorDataUpdateDTO {

  private String publicName;

  @EnumConstraint(enumClass = SponsorTierEnum.class, message = "Tier inválido. Valores aceitos: BRONZE, SILVER, GOLD")
  private String tier;

  @EnumConstraint(enumClass = SponsorEntityTypeEnum.class, message = "Tipo de entidade inválido. Valores aceitos: PERSON, COMPANY, GOVERNMENT, NGO")
  private String entityType;

  @EnumConstraint(enumClass = SponsorPersonaEnum.class, message = "Persona inválida. Valores aceitos: POLITICIAN, INFLUENCER, ATHLETE, OTHER")
  private String persona;

  private String logoUrl;

  private String site;

  private String instagram;

  @UnmaskNumber
  private String whatsapp;

  private Boolean isActive;
}
