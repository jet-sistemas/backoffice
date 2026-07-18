package backoffice.v1.entities;

import java.time.Instant;

import backoffice.v1.entities.enums.UserTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, length = 255)
  private String password;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true, length = 20)
  private String document;

  @Column(nullable = false, unique = true, length = 5)
  private String code;

  @Column(columnDefinition = "boolean default false")
  @Builder.Default
  private boolean isAccountActive = false;

  @Column(name = "email_verified_at")
  private Instant emailVerifiedAt;

  @Builder.Default
  @Column(name = "must_change_password", nullable = false, columnDefinition = "boolean default false")
  private boolean mustChangePassword = false;

  @Column(name = "password_changed_at")
  private Instant passwordChangedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserTypeEnum type;

  private String avatarUrl;
}
