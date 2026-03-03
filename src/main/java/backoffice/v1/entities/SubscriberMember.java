package backoffice.v1.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import backoffice.v1.entities.enums.MemberStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscriber_member")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class SubscriberMember extends BaseEntity {
  @OneToOne
  @JoinColumn(name = "member_id", nullable = false, unique = true)
  private Member member;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal monthlyFeeAmount;

  @Column(nullable = false)
  private Integer billingDay;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MemberStatusEnum status;

  @Column(nullable = false)
  private LocalDate nextDueDate;

  private Instant lastPaidAt;
}
