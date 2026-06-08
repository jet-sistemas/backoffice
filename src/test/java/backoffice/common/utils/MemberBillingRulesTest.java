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

  // --- resolveEffectiveStatus (F1) ---

  @Test
  void effectiveStatus_inactivePreservedEvenWithOverdueDueDate() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    assertEquals(MemberStatusEnum.INACTIVE,
        MemberBillingRules.resolveEffectiveStatus(
            MemberStatusEnum.INACTIVE, today, 5, LocalDate.of(2026, 1, 1)));
  }

  @Test
  void effectiveStatus_staleActiveBecomesOverdue() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    assertEquals(MemberStatusEnum.OVERDUE,
        MemberBillingRules.resolveEffectiveStatus(
            MemberStatusEnum.ACTIVE, today, 5, LocalDate.of(2026, 5, 10)));
  }

  @Test
  void effectiveStatus_staleActiveBecomesDueSoon() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    assertEquals(MemberStatusEnum.DUE_SOON,
        MemberBillingRules.resolveEffectiveStatus(
            MemberStatusEnum.ACTIVE, today, 5, LocalDate.of(2026, 6, 18)));
  }

  @Test
  void effectiveStatus_activeRemainsActive() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    assertEquals(MemberStatusEnum.ACTIVE,
        MemberBillingRules.resolveEffectiveStatus(
            MemberStatusEnum.ACTIVE, today, 5, LocalDate.of(2026, 6, 25)));
  }

  // --- canMarkSubscriberPayment ---

  @Test
  void cannotMarkWhenOutsideCompetenceMonth() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    assertEquals(false,
        MemberBillingRules.canMarkSubscriberPayment(MemberStatusEnum.ACTIVE, today, 5, LocalDate.of(2026, 7, 10)));
  }

  @Test
  void cannotMarkActiveWhenPaidAhead() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    LocalDate due = LocalDate.of(2026, 6, 25);
    assertEquals(false,
        MemberBillingRules.canMarkSubscriberPayment(MemberStatusEnum.ACTIVE, today, 5, due));
  }

  @Test
  void canMarkDueSoonInCompetenceMonth() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    LocalDate due = LocalDate.of(2026, 6, 18);
    assertEquals(true,
        MemberBillingRules.canMarkSubscriberPayment(MemberStatusEnum.DUE_SOON, today, 5, due));
  }

  @Test
  void overdueAlwaysCanMark() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    assertEquals(true,
        MemberBillingRules.canMarkSubscriberPayment(MemberStatusEnum.OVERDUE, today, 5, LocalDate.of(2020, 1, 10)));
  }

  // --- shouldAdvanceOverduePayment (F3) ---

  @Test
  void shouldAdvance_sameCompetenceMonth() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    LocalDate nextDue = LocalDate.of(2026, 6, 5);
    assertEquals(true, MemberBillingRules.shouldAdvanceOverduePayment(today, nextDue, false));
  }

  @Test
  void shouldAdvance_pendingTrue_pastCompetence() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    LocalDate nextDue = LocalDate.of(2026, 3, 10);
    assertEquals(true, MemberBillingRules.shouldAdvanceOverduePayment(today, nextDue, true));
  }

  @Test
  void shouldNotAdvance_pendingFalse_pastCompetence() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    LocalDate nextDue = LocalDate.of(2026, 3, 10);
    assertEquals(false, MemberBillingRules.shouldAdvanceOverduePayment(today, nextDue, false));
  }

  @Test
  void shouldAdvance_nextDueDateInFuture() {
    LocalDate today = LocalDate.of(2026, 6, 15);
    LocalDate nextDue = LocalDate.of(2026, 7, 10);
    assertEquals(true, MemberBillingRules.shouldAdvanceOverduePayment(today, nextDue, false));
  }
}
