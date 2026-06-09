package backoffice.v1.dtos.billing;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import backoffice.v1.dtos.user.UserMinDTO;
import backoffice.v1.entities.enums.MemberStatusEnum;
import backoffice.v1.entities.enums.SubscriberPaymentEventTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriberPaymentEventDTO {
  private Long id;
  private SubscriberPaymentEventTypeEnum eventType;
  private MemberStatusEnum oldStatus;
  private MemberStatusEnum newStatus;
  private LocalDate oldNextDueDate;
  private LocalDate newNextDueDate;
  private BigDecimal oldMonthlyFeeAmount;
  private BigDecimal newMonthlyFeeAmount;
  private Integer oldBillingDay;
  private Integer newBillingDay;
  private BigDecimal amount;
  private String note;
  private Instant createdAt;
  private UserMinDTO adminUser;
}
