package backoffice.v1.dtos.member;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mensalidade para membro {@code SUBSCRIBER}. {@code billingDay}: 1–28 (compatível com todos os meses).
 * {@code nextDueDate}: opcional; se ausente, calcula-se a primeira data de vencimento no ou após o dia da criação.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriberDataCreateDTO {
  @NotNull(message = "O valor da mensalidade é obrigatório.")
  @DecimalMin(value = "0.01", message = "O valor da mensalidade deve ser maior que zero.")
  private BigDecimal monthlyFeeAmount;

  @NotNull(message = "O dia de cobrança é obrigatório.")
  @Min(value = 1, message = "O dia de cobrança deve estar entre 1 e 28.")
  @Max(value = 28, message = "O dia de cobrança deve estar entre 1 e 28.")
  private Integer billingDay;

  private LocalDate nextDueDate;
}
