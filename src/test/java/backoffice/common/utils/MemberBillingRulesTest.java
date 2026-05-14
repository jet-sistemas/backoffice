package backoffice.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import backoffice.v1.entities.enums.MemberStatusEnum;

class MemberBillingRulesTest {

  @Test
  void overdueWhenNextDueBeforeToday() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    assertEquals(MemberStatusEnum.OVERDUE,
        MemberBillingRules.expectedAutomationStatus(today, 5, LocalDate.of(2026, 6, 14)));
  }

  @Test
  void dueSoonWhenWithinWindowInclusive() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    assertEquals(MemberStatusEnum.DUE_SOON,
        MemberBillingRules.expectedAutomationStatus(today, 5, LocalDate.of(2026, 6, 15)));
    assertEquals(MemberStatusEnum.DUE_SOON,
        MemberBillingRules.expectedAutomationStatus(today, 5, LocalDate.of(2026, 6, 20)));
  }

  @Test
  void activeWhenBeyondWindow() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    assertEquals(MemberStatusEnum.ACTIVE,
        MemberBillingRules.expectedAutomationStatus(today, 5, LocalDate.of(2026, 6, 21)));
  }
}
