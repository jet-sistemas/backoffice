package backoffice.common.utils;

import java.time.LocalDate;
import java.time.YearMonth;

import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.v1.entities.enums.MemberStatusEnum;

public final class MemberBillingRules {

  private MemberBillingRules() {
  }

  /**
   * Status automático esperado com base na data de vencimento e janela “a vencer”.
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

  public static boolean canMarkSubscriberPayment(MemberStatusEnum status, LocalDate today, int dueSoonDays,
      LocalDate nextDueDate) {
    if (status == MemberStatusEnum.INACTIVE) {
      return false;
    }
    if (status == MemberStatusEnum.OVERDUE) {
      return true;
    }
    if (!YearMonth.from(today).equals(YearMonth.from(nextDueDate))) {
      return false;
    }
    if (status == MemberStatusEnum.ACTIVE && nextDueDate.isAfter(today.plusDays(dueSoonDays))) {
      return false;
    }
    return true;
  }

  /** Mensagem PT-BR para UI quando {@link #canMarkSubscriberPayment} é falso; {@code null} se pode marcar ou OVERDUE. */
  public static String paymentMarkBlockedReason(MemberStatusEnum status, LocalDate today, int dueSoonDays,
      LocalDate nextDueDate) {
    if (status == MemberStatusEnum.INACTIVE) {
      return MessageErrorEnum.SUBSCRIBER_PAYMENT_INACTIVE.getMessage();
    }
    if (status == MemberStatusEnum.OVERDUE) {
      return null;
    }
    if (!YearMonth.from(today).equals(YearMonth.from(nextDueDate))) {
      return MessageErrorEnum.SUBSCRIBER_PAYMENT_OUTSIDE_COMPETENCE_MONTH.getMessage();
    }
    if (status == MemberStatusEnum.ACTIVE && nextDueDate.isAfter(today.plusDays(dueSoonDays))) {
      return MessageErrorEnum.SUBSCRIBER_PAYMENT_ALREADY_REGISTERED.getMessage();
    }
    return null;
  }

  /**
   * OVERDUE com competência já ultrapassada: segunda marcação após registro só com {@code lastPaidAt}
   * deve avançar {@code nextDueDate}.
   */
  public static boolean overdueEligibleForDeferredDueAdvance(LocalDate nextDueDate, LocalDate lastPaidLocalDate) {
    if (lastPaidLocalDate == null) {
      return false;
    }
    return YearMonth.from(lastPaidLocalDate).isAfter(YearMonth.from(nextDueDate));
  }
}
