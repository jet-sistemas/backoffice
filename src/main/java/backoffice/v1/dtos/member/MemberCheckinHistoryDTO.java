package backoffice.v1.dtos.member;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCheckinHistoryDTO {

  private Long id;

  private MemberCheckinHistorySponsorDTO sponsor;

  private Instant checkedInAt;

  private boolean validated;

  private boolean duplicateConfirmed;
}
