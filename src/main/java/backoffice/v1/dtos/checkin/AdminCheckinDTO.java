package backoffice.v1.dtos.checkin;

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
public class AdminCheckinDTO {

  private Long id;

  private Instant checkedInAt;

  private boolean validated;

  private String reason;

  private boolean duplicateConfirmed;

  private CheckinLookupTypeEnum lookupType;

  private AdminCheckinSponsorMinDTO sponsor;

  private AdminCheckinMemberMinDTO member;
}
