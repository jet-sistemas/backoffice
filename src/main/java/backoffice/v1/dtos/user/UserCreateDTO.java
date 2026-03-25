package backoffice.v1.dtos.user;

import backoffice.common.annotations.UnmaskNumber;
import backoffice.common.validators.EnumConstraint;
import backoffice.v1.entities.enums.UserTypeEnum;
import jakarta.validation.constraints.Email;
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
public class UserCreateDTO {
  @NotBlank(message = "O e-mail é obrigatório")
  @Email(message = "E-mail inválido")
  private String email;

  @Length(min = 8, max = 16, message = "A senha deve ter entre 8 e 16 caracteres")
  private String password;

  private String avatarUrl;

  @EnumConstraint(enumClass = UserTypeEnum.class, message = "Tipo de usuário inválido")
  private String type;

  @NotBlank(message = "O nome é obrigatório")
  private String name;

  @NotBlank(message = "O documento é obrigatório")
  @Length(min = 11, max = 14, message = "O documento deve ter entre 11 e 14 caracteres")
  @UnmaskNumber
  private String document;

  @NotBlank(message = "O código é obrigatório")
  @Length(min = 5, max = 5, message = "O código deve ter exatamente 5 caracteres")
  private String code;
}
