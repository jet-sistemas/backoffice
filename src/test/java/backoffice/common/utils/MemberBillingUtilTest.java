package backoffice.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class MemberBillingUtilTest {

  @Test
  void advanceAnchorsOnBillingDay() {
    LocalDate nextDue = LocalDate.of(2026, 6, 22);
    LocalDate today = LocalDate.of(2026, 6, 23);
    assertEquals(LocalDate.of(2026, 7, 10),
        MemberBillingUtil.advanceNextDueDate(nextDue, 10, today));
  }

  @Test
  void advanceCatchUpSkipsMonthsBehindToday() {
    LocalDate nextDue = LocalDate.of(2020, 1, 10);
    LocalDate today = LocalDate.of(2026, 6, 15);
    LocalDate result = MemberBillingUtil.advanceNextDueDate(nextDue, 10, today);
    assertEquals(10, result.getDayOfMonth());
    assertEquals(true, !result.isBefore(today));
    assertEquals(LocalDate.of(2026, 7, 10), result);
  }

  @Test
  void advanceWhenAlreadyOnBillingDay() {
    LocalDate nextDue = LocalDate.of(2026, 6, 10);
    LocalDate today = LocalDate.of(2026, 6, 10);
    assertEquals(LocalDate.of(2026, 7, 10),
        MemberBillingUtil.advanceNextDueDate(nextDue, 10, today));
  }

  @Test
  void advanceResultOnBillingDayEqualToday() {
    LocalDate nextDue = LocalDate.of(2026, 5, 10);
    LocalDate today = LocalDate.of(2026, 6, 10);
    assertEquals(LocalDate.of(2026, 6, 10),
        MemberBillingUtil.advanceNextDueDate(nextDue, 10, today));
  }

  @Test
  void computeNextDueDateSameMonth() {
    assertEquals(LocalDate.of(2026, 6, 15),
        MemberBillingUtil.computeNextDueDate(LocalDate.of(2026, 6, 10), 15));
  }

  @Test
  void computeNextDueDateNextMonth() {
    assertEquals(LocalDate.of(2026, 7, 5),
        MemberBillingUtil.computeNextDueDate(LocalDate.of(2026, 6, 10), 5));
  }

  @Test
  void shiftBillingDayHigher() {
    assertEquals(LocalDate.of(2026, 6, 15),
        MemberBillingUtil.shiftNextDueDateForBillingDayChange(LocalDate.of(2026, 6, 10), 10, 15));
  }

  @Test
  void shiftBillingDayLower() {
    assertEquals(LocalDate.of(2026, 7, 10),
        MemberBillingUtil.shiftNextDueDateForBillingDayChange(LocalDate.of(2026, 6, 15), 15, 10));
  }
}
