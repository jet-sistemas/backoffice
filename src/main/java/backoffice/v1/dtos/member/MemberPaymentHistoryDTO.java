package backoffice.v1.dtos.member;

import java.math.BigDecimal;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberPaymentHistoryDTO {
  private Long id;
  private Instant conferenceAt;
  private String adminName;
  private BigDecimal amount;
  private String note;
}
