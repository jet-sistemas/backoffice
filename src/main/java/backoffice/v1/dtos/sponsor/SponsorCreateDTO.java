package backoffice.v1.dtos.sponsor;

import backoffice.common.annotations.UnmaskNumber;
import backoffice.common.validators.EnumConstraint;
import backoffice.v1.dtos.user.UserCreateDTO;
import backoffice.v1.entities.enums.SponsorEntityTypeEnum;
import backoffice.v1.entities.enums.SponsorPersonaEnum;
import backoffice.v1.entities.enums.SponsorTierEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorCreateDTO {

  @Valid
  @NotNull(message = "Os dados do usuário são obrigatórios")
  private UserCreateDTO user;

  @NotBlank(message = "O nome público é obrigatório")
  private String publicName;

  @NotBlank(message = "O tier é obrigatório")
  @EnumConstraint(enumClass = SponsorTierEnum.class, message = "Tier inválido. Valores aceitos: BRONZE, SILVER, GOLD")
  private String tier;

  @NotBlank(message = "O tipo de entidade é obrigatório")
  @EnumConstraint(enumClass = SponsorEntityTypeEnum.class, message = "Tipo de entidade inválido. Valores aceitos: PERSON, COMPANY, GOVERNMENT, NGO")
  private String entityType;

  @NotBlank(message = "A persona é obrigatória")
  @EnumConstraint(enumClass = SponsorPersonaEnum.class, message = "Persona inválida. Valores aceitos: POLITICIAN, INFLUENCER, ATHLETE, OTHER")
  private String persona;

  private String logoUrl;

  private String site;

  private String instagram;

  @UnmaskNumber
  private String whatsapp;
}
