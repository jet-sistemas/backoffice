package backoffice.v1.dtos.billing;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriberPaymentMarkPaidDTO {

  private Instant paidAt;

  @DecimalMin(value = "0.01", message = "Valor pago deve ser maior que zero.")
  private BigDecimal amountPaid;

  @Size(max = 500, message = "Observação excede o tamanho máximo permitido.")
  private String note;
}
