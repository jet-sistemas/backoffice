package backoffice.v1.dtos.user;

import org.hibernate.validator.constraints.Length;

import backoffice.common.validators.EnumConstraint;
import backoffice.v1.entities.enums.UserTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDTO {
  @NotBlank
  @Email
  private String email;

  @NotBlank
  @Length(min = 8, max = 16)
  private String password;

  private String avatarUrl;

  @NotBlank
  @EnumConstraint(enumClass = UserTypeEnum.class, message = "User type invalid")
  private String type;

  @NotBlank
  private String name;

  @NotBlank
  @Length(min = 11, max = 14)
  private String document;

  @NotBlank
  @Length(min = 5, max = 5)
  private String code;
}
