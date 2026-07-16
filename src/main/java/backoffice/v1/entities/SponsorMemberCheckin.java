package backoffice.v1.entities;

import backoffice.v1.entities.enums.CheckinLookupTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sponsors_members_checkin")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class SponsorMemberCheckin extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "sponsor_id", nullable = false)
  private Sponsor sponsor;

  @ManyToOne
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Builder.Default
  @Column(columnDefinition = "boolean default false", nullable = false)
  private boolean validated = false;

  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(name = "lookup_type", length = 20)
  private CheckinLookupTypeEnum lookupType;

  @Builder.Default
  @Column(name = "duplicate_confirmed", nullable = false, columnDefinition = "boolean default false")
  private boolean duplicateConfirmed = false;
}
