package backoffice.common.utils;

import java.time.LocalDate;

/**
 * Calcula a próxima data de vencimento para um {@code billingDay} (1–28) com base em uma data de referência
 * (em geral a data de cadastro no fuso do servidor).
 */
public final class MemberBillingUtil {

  private MemberBillingUtil() {
  }

  /**
   * Primeira data com dia {@code billingDay} no mesmo mês de {@code reference} ou no mês seguinte,
   * de modo que o resultado não seja anterior a {@code reference}.
   */
  public static LocalDate computeNextDueDate(LocalDate reference, int billingDay) {
    LocalDate candidate = reference.withDayOfMonth(billingDay);
    if (candidate.isBefore(reference)) {
      candidate = candidate.plusMonths(1);
    }
    return candidate;
  }

  /**
   * Reposiciona o vencimento quando o dia de cobrança muda (regra ACTIVE/DUE_SOON).
   * Dias limitados a 1–28 para evitar overflow em meses curtos.
   */
  public static LocalDate shiftNextDueDateForBillingDayChange(LocalDate currentNextDue, int oldBillingDay,
      int newBillingDay) {
    if (newBillingDay == oldBillingDay) {
      return currentNextDue;
    }
    if (newBillingDay > oldBillingDay) {
      return currentNextDue.withDayOfMonth(newBillingDay);
    }
    return currentNextDue.plusMonths(1).withDayOfMonth(newBillingDay);
  }
}
