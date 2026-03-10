package backoffice.v1.entities;

import backoffice.v1.entities.enums.SponsorEntityTypeEnum;
import backoffice.v1.entities.enums.SponsorPersonaEnum;
import backoffice.v1.entities.enums.SponsorTierEnum;
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
@Table(name = "sponsors")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class Sponsor extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private String publicName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SponsorTierEnum tier;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SponsorEntityTypeEnum entityType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = true)
  private SponsorPersonaEnum persona;

  private String logoUrl;

  private String site;

  private String instagram;

  @Column(unique = true)
  private String whatsapp;

  @Builder.Default
  @Column(columnDefinition = "boolean default true", nullable = false)
  private boolean isActive = true;
}
