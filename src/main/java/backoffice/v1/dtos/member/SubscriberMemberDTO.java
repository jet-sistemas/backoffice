package backoffice.v1.dtos.member;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import backoffice.v1.entities.enums.MemberStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriberMemberDTO {
  private Long id;
  private BigDecimal monthlyFeeAmount;
  private Integer billingDay;
  private MemberStatusEnum status;
  private LocalDate nextDueDate;
  private Instant lastPaidAt;
  private Instant createdAt;
  private Boolean canMarkPayment;
  private String paymentMarkBlockedReason;
}
