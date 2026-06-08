package backoffice.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

  // --- canMarkSubscriberPayment (F5) ---

  @Test
  void canMark_overdueAndDueSoon() {
    assertTrue(MemberBillingRules.canMarkSubscriberPayment(MemberStatusEnum.OVERDUE));
    assertTrue(MemberBillingRules.canMarkSubscriberPayment(MemberStatusEnum.DUE_SOON));
  }

  @Test
  void cannotMark_activeAndInactive() {
    assertEquals(false, MemberBillingRules.canMarkSubscriberPayment(MemberStatusEnum.ACTIVE));
    assertEquals(false, MemberBillingRules.canMarkSubscriberPayment(MemberStatusEnum.INACTIVE));
  }

  @Test
  void canMarkDueSoonCrossMonthAtMonthBoundary() {
    LocalDate today = LocalDate.of(2026, 6, 29);
    MemberStatusEnum effective = MemberBillingRules.resolveEffectiveStatus(
        MemberStatusEnum.ACTIVE, today, 5, LocalDate.of(2026, 7, 2));
    assertEquals(MemberStatusEnum.DUE_SOON, effective);
    assertTrue(MemberBillingRules.canMarkSubscriberPayment(effective));
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
