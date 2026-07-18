package backoffice.v1.dtos.sponsor;

import java.time.Instant;

import backoffice.v1.entities.enums.MemberTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorMemberPreviewDTO {
  private Long id;
  private String name;
  private String avatarUrl;
  private String code;
  private String documentMasked;
  private MemberTypeEnum memberType;
  private boolean eligible;
  private String ineligibleReason;
  private boolean alreadyCheckedInToday;
  private Instant lastCheckinAt;
}
