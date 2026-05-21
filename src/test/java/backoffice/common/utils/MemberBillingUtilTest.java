package backoffice.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class MemberBillingUtilTest {

  @Test
  void shiftBillingDayLaterKeepsMonth() {
    LocalDate due = LocalDate.of(2026, 6, 10);
    assertEquals(LocalDate.of(2026, 6, 15),
        MemberBillingUtil.shiftNextDueDateForBillingDayChange(due, 10, 15));
  }

  @Test
  void shiftBillingDayEarlierMovesToNextMonth() {
    LocalDate due = LocalDate.of(2026, 6, 15);
    assertEquals(LocalDate.of(2026, 7, 10),
        MemberBillingUtil.shiftNextDueDateForBillingDayChange(due, 15, 10));
  }

  @Test
  void shiftSameDayUnchanged() {
    LocalDate due = LocalDate.of(2026, 6, 10);
    assertEquals(due, MemberBillingUtil.shiftNextDueDateForBillingDayChange(due, 10, 10));
  }
}
