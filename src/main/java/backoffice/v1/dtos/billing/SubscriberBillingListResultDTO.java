package backoffice.v1.dtos.billing;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriberBillingListResultDTO {
  private SubscriberBillingSummaryDTO summary;
  private List<SubscriberBillingRowDTO> rows;
  private Long totalElements;
  private Integer totalPages;
  private Integer pageSize;
  private Integer currentPage;
}
