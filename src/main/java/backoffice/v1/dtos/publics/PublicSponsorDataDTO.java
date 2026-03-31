package backoffice.v1.dtos.publics;

import backoffice.v1.entities.enums.SponsorTierEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublicSponsorDataDTO {
  private Long id;
  private String publicName;
  private SponsorTierEnum tier;
  private String logoUrl;
  private String site;
  private String instagram;
  private String whatsapp;
}
