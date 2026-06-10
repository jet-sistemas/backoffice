package backoffice.v1.dtos.member;

import java.math.BigDecimal;
import java.time.LocalDate;

import backoffice.common.validators.EnumConstraint;
import backoffice.v1.entities.enums.MemberStatusEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriberMemberUpdateDTO {
  @DecimalMin(value = "0.01", message = "O valor da mensalidade deve ser maior que zero.")
  private BigDecimal monthlyFeeAmount;

  @Min(value = 1, message = "O dia de cobrança deve estar entre 1 e 28.")
  @Max(value = 28, message = "O dia de cobrança deve estar entre 1 e 28.")
  private Integer billingDay;
  private LocalDate nextDueDate;

  @EnumConstraint(enumClass = MemberStatusEnum.class, message = "Status de assinante inválido.")
  private String status;
}
