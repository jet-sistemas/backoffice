package backoffice.v1.dtos.member;

import backoffice.common.annotations.UnmaskNumber;
import backoffice.common.validators.EnumConstraint;
import backoffice.v1.entities.enums.MemberTypeEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDataCreateDTO {
  @NotBlank(message = "O nome completo do membro é obrigatório")
  private String fullname;

  @NotBlank(message = "O WhatsApp do membro é obrigatório")
  @Length(max = 50, message = "O WhatsApp deve ter no máximo 50 caracteres")
  @UnmaskNumber
  private String whatsapp;

  @NotBlank(message = "O tipo do membro é obrigatório")
  @EnumConstraint(enumClass = MemberTypeEnum.class, message = "Tipo de membro inválido.")
  private String type;

  @Valid
  private SubscriberDataCreateDTO subscriber;

  @Valid
  private SponsoredDataCreateDTO sponsored;
}
