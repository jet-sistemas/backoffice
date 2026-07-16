package backoffice.v1.dtos.accountvalidation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountValidationResultDTO {
  private String email;
  private boolean accountActive;
  private boolean mustChangePassword;
}
