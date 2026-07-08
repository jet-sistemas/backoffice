package backoffice.v1.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequestDTO {

  @NotBlank(message = "Senha atual é obrigatória.")
  private String currentPassword;

  @NotBlank(message = "Nova senha é obrigatória.")
  private String newPassword;

  @NotBlank(message = "Confirmação de senha é obrigatória.")
  private String confirmPassword;
}
