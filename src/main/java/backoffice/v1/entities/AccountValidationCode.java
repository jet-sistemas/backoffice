package backoffice.v1.entities;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "account_validation_codes")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class AccountValidationCode extends BaseEntity {

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "code_hash", nullable = false, length = 255)
  private String codeHash;

  @Column(name = "link_token_hash", nullable = false, unique = true, length = 255)
  private String linkTokenHash;

  @Builder.Default
  @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
  private boolean isActive = true;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "used_at")
  private Instant usedAt;

  @Column(name = "sent_at", nullable = false)
  private Instant sentAt;

  @Column(name = "resent_at")
  private Instant resentAt;

  @Builder.Default
  @Column(name = "attempt_count", nullable = false)
  private int attemptCount = 0;

  @Column(name = "locked_at")
  private Instant lockedAt;

  @ManyToOne
  @JoinColumn(name = "created_by_admin_id")
  private User createdByAdmin;
}
