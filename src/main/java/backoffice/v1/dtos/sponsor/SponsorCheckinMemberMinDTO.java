package backoffice.v1.dtos.sponsor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorCheckinMemberMinDTO {
  private Long id;
  private String name;
  private String code;
  private String documentMasked;
}
