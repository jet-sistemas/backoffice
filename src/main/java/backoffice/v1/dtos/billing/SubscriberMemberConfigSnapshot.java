package backoffice.v1.dtos.billing;

import java.math.BigDecimal;
import java.time.LocalDate;

import backoffice.v1.entities.SubscriberMember;
import backoffice.v1.entities.enums.MemberStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberMemberConfigSnapshot {
  private BigDecimal monthlyFeeAmount;
  private Integer billingDay;
  private LocalDate nextDueDate;
  private MemberStatusEnum status;

  public static SubscriberMemberConfigSnapshot from(SubscriberMember entity) {
    return new SubscriberMemberConfigSnapshot(
        entity.getMonthlyFeeAmount(),
        entity.getBillingDay(),
        entity.getNextDueDate(),
        entity.getStatus());
  }

  public boolean equalsSnapshotOf(SubscriberMember entity) {
    return monthlyFeeAmount.compareTo(entity.getMonthlyFeeAmount()) == 0
        && billingDay.equals(entity.getBillingDay())
        && nextDueDate.equals(entity.getNextDueDate())
        && status == entity.getStatus();
  }
}
