package backoffice.v1.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthCreateDTO {
  @NotBlank
  @Email
  private String email;

  @NotBlank
  @Size(min = 8, max = 16, message = "A senha deve ter entre 8 e 16 caracteres")
  private String password;
}
