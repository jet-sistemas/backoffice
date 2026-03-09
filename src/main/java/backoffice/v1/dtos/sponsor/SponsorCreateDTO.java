package backoffice.v1.dtos.sponsor;

import backoffice.common.annotations.UnmaskNumber;
import backoffice.common.validators.EnumConstraint;
import backoffice.v1.entities.enums.SponsorEntityTypeEnum;
import backoffice.v1.entities.enums.SponsorPersonaEnum;
import backoffice.v1.entities.enums.SponsorTierEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorCreateDTO {

  @Valid
  @NotNull(message = "Os dados do usuário são obrigatórios")
  private UserData user;

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

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class UserData {
    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "O nome é obrigatório")
    private String name;

    @NotBlank(message = "O documento é obrigatório")
    @Length(min = 11, max = 14, message = "O documento deve ter entre 11 e 14 caracteres")
    @UnmaskNumber
    private String document;

    @NotBlank(message = "O código é obrigatório")
    @Length(min = 5, max = 5, message = "O código deve ter exatamente 5 caracteres")
    private String code;

    private String avatarUrl;
  }
}
