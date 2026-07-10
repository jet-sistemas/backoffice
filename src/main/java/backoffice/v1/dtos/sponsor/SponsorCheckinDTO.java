package backoffice.v1.dtos.sponsor;

import java.time.Instant;

import backoffice.v1.entities.enums.CheckinLookupTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorCheckinDTO {
  private Long id;
  private boolean validated;
  private String reason;
  private boolean duplicateConfirmed;
  private CheckinLookupTypeEnum lookupType;
  private Instant createdAt;
  private SponsorCheckinMemberMinDTO member;
}
