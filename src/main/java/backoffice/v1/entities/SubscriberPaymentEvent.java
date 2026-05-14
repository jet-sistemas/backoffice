package backoffice.v1.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import backoffice.v1.entities.enums.MemberStatusEnum;
import backoffice.v1.entities.enums.SubscriberPaymentEventTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscriber_payment_event", indexes = {
    @Index(name = "idx_subscriber_payment_event_sub_created", columnList = "subscriber_member_id,created_at")
})
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class SubscriberPaymentEvent extends BaseEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "subscriber_member_id", nullable = false)
  private SubscriberMember subscriberMember;

  @ManyToOne
  @JoinColumn(name = "admin_user_id")
  private User adminUser;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SubscriberPaymentEventTypeEnum eventType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MemberStatusEnum oldStatus;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MemberStatusEnum newStatus;

  @Column(nullable = false)
  private LocalDate oldNextDueDate;

  @Column(nullable = false)
  private LocalDate newNextDueDate;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal oldMonthlyFeeAmount;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal newMonthlyFeeAmount;

  @Column(nullable = false)
  private Integer oldBillingDay;

  @Column(nullable = false)
  private Integer newBillingDay;

  @Column(precision = 10, scale = 2)
  private BigDecimal amount;

  @Column(length = 500)
  private String note;
}
