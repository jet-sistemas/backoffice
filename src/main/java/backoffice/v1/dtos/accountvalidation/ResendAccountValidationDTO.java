package backoffice.v1.dtos.accountvalidation;

import backoffice.v1.entities.enums.AccountValidationResendTypeEnum;
import backoffice.v1.entities.enums.AccountValidationStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendAccountValidationDTO {
  private Long userId;
  private boolean sent;
  private AccountValidationStatusEnum accountValidationStatus;
  private AccountValidationResendTypeEnum resendType;
}
