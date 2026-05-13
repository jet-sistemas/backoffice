package backoffice.v1.dtos.member;

import java.time.Instant;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsoredMemberDTO {
  private Long memberId;
  private Long grantedByUserId;
  private LocalDate startAt;
  private LocalDate endAt;
  private String reason;
  private boolean active;
  private Instant createdAt;
  private Instant updatedAt;
}
