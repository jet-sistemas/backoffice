package backoffice.v1.dtos.member;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberBenefitDTO {
  private Long id;
  private String name;
  private String description;
  private String address;

  @JsonInclude(Include.NON_NULL)
  private MemberBenefitSponsorDTO sponsor;
}
