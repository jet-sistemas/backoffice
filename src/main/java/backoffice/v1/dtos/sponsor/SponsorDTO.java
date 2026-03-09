package backoffice.v1.dtos.sponsor;

import java.time.Instant;

import backoffice.v1.dtos.user.UserMinDTO;
import backoffice.v1.entities.enums.SponsorEntityTypeEnum;
import backoffice.v1.entities.enums.SponsorPersonaEnum;
import backoffice.v1.entities.enums.SponsorTierEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorDTO {
  private Long id;
  private UserMinDTO user;
  private String publicName;
  private SponsorTierEnum tier;
  private SponsorEntityTypeEnum entityType;
  private SponsorPersonaEnum persona;
  private String logoUrl;
  private String site;
  private String instagram;
  private String whatsapp;
  private boolean isActive;
  private Instant createdAt;
}
