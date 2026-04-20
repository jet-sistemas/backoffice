package backoffice.v1.dtos.sponsor;

import backoffice.v1.entities.enums.SponsorTierEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorMinDTO {
  private Long id;
  private String publicName;
  private SponsorTierEnum tier;
  private boolean isActive;
}
