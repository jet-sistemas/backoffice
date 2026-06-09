package backoffice.common.utils;

import java.time.LocalDate;
import java.time.YearMonth;

import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.v1.entities.enums.MemberStatusEnum;
import backoffice.v1.entities.enums.PaymentMarkBlockReasonEnum;

public final class MemberBillingRules {

  private MemberBillingRules() {
  }

  /**
   * Status automático esperado com base na data de vencimento e janela "a vencer".
   * Não considera {@link MemberStatusEnum#INACTIVE} — chamador deve ignorar esse caso.
   */
  public static MemberStatusEnum expectedAutomationStatus(LocalDate today, int dueSoonDays,
      LocalDate nextDueDate) {
    LocalDate windowEnd = today.plusDays(dueSoonDays);
    if (nextDueDate.isBefore(today)) {
      return MemberStatusEnum.OVERDUE;
    }
    if (!nextDueDate.isAfter(windowEnd)) {
      return MemberStatusEnum.DUE_SOON;
    }
    return MemberStatusEnum.ACTIVE;
  }

  /**
   * Resolve o status efetivo em tempo real: preserva {@code INACTIVE} (controle manual)
   * e deriva os demais a partir de {@code nextDueDate} — independente do cache persistido.
   */
  public static MemberStatusEnum resolveEffectiveStatus(
      MemberStatusEnum persisted, LocalDate today, int dueSoonDays, LocalDate nextDueDate) {
    if (persisted == MemberStatusEnum.INACTIVE) {
      return MemberStatusEnum.INACTIVE;
    }
    return expectedAutomationStatus(today, dueSoonDays, nextDueDate);
  }

  public static boolean canMarkSubscriberPayment(MemberStatusEnum effectiveStatus) {
    return effectiveStatus == MemberStatusEnum.OVERDUE
        || effectiveStatus == MemberStatusEnum.DUE_SOON;
  }

  /** Mensagem PT-BR para UI quando {@link #canMarkSubscriberPayment} é falso; {@code null} se pode marcar. */
  public static String paymentMarkBlockedReason(MemberStatusEnum effectiveStatus) {
    if (effectiveStatus == MemberStatusEnum.INACTIVE) {
      return MessageErrorEnum.SUBSCRIBER_PAYMENT_INACTIVE.getMessage();
    }
    if (effectiveStatus == MemberStatusEnum.ACTIVE) {
      return MessageErrorEnum.SUBSCRIBER_PAYMENT_ALREADY_REGISTERED.getMessage();
    }
    return null;
  }

  /** Código estável para UI quando {@link #canMarkSubscriberPayment} é falso; {@code null} se pode marcar. */
  public static PaymentMarkBlockReasonEnum paymentMarkBlockedCode(MemberStatusEnum effectiveStatus) {
    if (effectiveStatus == MemberStatusEnum.INACTIVE) {
      return PaymentMarkBlockReasonEnum.INACTIVE;
    }
    if (effectiveStatus == MemberStatusEnum.ACTIVE) {
      return PaymentMarkBlockReasonEnum.ALREADY_REGISTERED;
    }
    return null;
  }

  /**
   * OVERDUE com competência passada: decide se o pagamento deve avançar {@code nextDueDate}.
   * Avança na 1ª marcação se competência igual ou futura; na 2ª se flag {@code pending} ativo.
   */
  public static boolean shouldAdvanceOverduePayment(
      LocalDate today, LocalDate nextDueDate, boolean overdueDueAdvancePending) {
    if (YearMonth.from(today).equals(YearMonth.from(nextDueDate))) {
      return true;
    }
    if (!YearMonth.from(today).isAfter(YearMonth.from(nextDueDate))) {
      return true;
    }
    return overdueDueAdvancePending;
  }
}
