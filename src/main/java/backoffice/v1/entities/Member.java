package backoffice.v1.entities;

import backoffice.v1.entities.enums.MemberTypeEnum;
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
@Table(name = "members")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class Member extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String fullname;

  @Column(nullable = false, unique = true, length = 50)
  private String whatsapp;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MemberTypeEnum type;

  @Builder.Default
  @Column(columnDefinition = "boolean default true")
  private boolean isActive = true;
}
