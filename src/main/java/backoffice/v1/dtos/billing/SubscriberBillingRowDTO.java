package backoffice.v1.dtos.billing;

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
public class SubscriberBillingRowDTO {
  private Long userId;
  private Long memberId;
  private String fullname;
  private String email;
  private String document;
  private String whatsapp;
  private BigDecimal monthlyFeeAmount;
  private Integer billingDay;
  private MemberStatusEnum status;
  private LocalDate nextDueDate;
  private Instant lastPaidAt;
  private Boolean canMarkPayment;
  private String paymentMarkBlockedReason;
  private String paymentMarkBlockedCode;
}
