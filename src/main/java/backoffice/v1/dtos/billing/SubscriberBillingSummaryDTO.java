package backoffice.v1.dtos.billing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriberBillingSummaryDTO {
  private long overdueCount;
  private long dueSoonCount;
  private long activeCount;
  private long inactiveCount;
}
