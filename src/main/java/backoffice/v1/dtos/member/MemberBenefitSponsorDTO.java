package backoffice.v1.dtos.member;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import backoffice.v1.entities.enums.SponsorTierEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberBenefitSponsorDTO {
  private Long id;
  private String publicName;
  private SponsorTierEnum tier;

  @JsonInclude(Include.NON_NULL)
  private String logoUrl;
}
