package backoffice.common.utils;

import java.time.LocalDate;

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
}
