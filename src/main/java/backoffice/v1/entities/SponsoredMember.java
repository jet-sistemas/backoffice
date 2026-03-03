package backoffice.v1.entities;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sponsored_member")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SponsoredMember {
  @EmbeddedId
  private SponsoredMemberId id;

  @ManyToOne
  @MapsId("memberId")
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne
  @MapsId("grantedByUserId")
  @JoinColumn(name = "granted_by_user_id", nullable = false)
  private User grantedByUser;

  @Column(nullable = false)
  private LocalDate startAt;

  private LocalDate endAt;

  private String reason;

  @Builder.Default
  @Column(columnDefinition = "boolean default true")
  private boolean isActive = true;

  @CreationTimestamp
  @Column(columnDefinition = "timestamp")
  private Instant createdAt;

  @UpdateTimestamp
  @Column(columnDefinition = "timestamp")
  private Instant updatedAt;

  @Embeddable
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @EqualsAndHashCode
  public static class SponsoredMemberId implements Serializable {
    private Long memberId;
    private Long grantedByUserId;
  }
}
