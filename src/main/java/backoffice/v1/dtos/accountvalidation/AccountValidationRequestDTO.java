package backoffice.v1.dtos.accountvalidation;

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
public class AccountValidationRequestDTO {

  @NotBlank(message = "Token de validação é obrigatório.")
  private String token;

  @NotBlank(message = "Código de validação é obrigatório.")
  @Size(min = 5, max = 5, message = "O código de validação deve ter exatamente 5 caracteres.")
  private String code;

  @NotBlank(message = "Documento é obrigatório.")
  private String document;
}
