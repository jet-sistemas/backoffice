package backoffice.v1.dtos.member;

import java.math.BigDecimal;
import java.time.LocalDate;

import backoffice.common.validators.EnumConstraint;
import backoffice.v1.entities.enums.MemberStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriberMemberUpdateDTO {
  private BigDecimal monthlyFeeAmount;
  private Integer billingDay;
  private LocalDate nextDueDate;

  @EnumConstraint(enumClass = MemberStatusEnum.class, message = "Status de assinante inválido.")
  private String status;
}
