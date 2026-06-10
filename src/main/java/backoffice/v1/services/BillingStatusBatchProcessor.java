package backoffice.v1.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import backoffice.common.utils.MemberBillingRules;
import backoffice.v1.dtos.billing.SubscriberMemberConfigSnapshot;
import backoffice.v1.entities.SubscriberMember;
import backoffice.v1.entities.SubscriberPaymentEvent;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.MemberStatusEnum;
import backoffice.v1.entities.enums.SubscriberPaymentEventTypeEnum;
import backoffice.v1.repositories.SubscriberMemberRepository;
import backoffice.v1.repositories.SubscriberPaymentEventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class BillingStatusBatchProcessor {

  @Inject
  SubscriberMemberRepository subscriberMemberRepository;

  @Inject
  SubscriberPaymentEventRepository subscriberPaymentEventRepository;

  @Transactional
  public int processBatch(
      List<SubscriberMember> batch,
      LocalDate today,
      MemberStatusEnum phase,
      int dueSoonDays) {
    int changed = 0;
    for (SubscriberMember s : batch) {
      SubscriberMember managed = subscriberMemberRepository.findById(s.getId());
      if (managed == null || managed.getStatus() == MemberStatusEnum.INACTIVE) {
        continue;
      }
      MemberStatusEnum target = MemberBillingRules.expectedAutomationStatus(
          today, dueSoonDays, managed.getNextDueDate());
      if (target != phase || managed.getStatus() == target) {
        continue;
      }
      SubscriberMemberConfigSnapshot before = SubscriberMemberConfigSnapshot.from(managed);
      managed.setStatus(target);
      SubscriberMemberConfigSnapshot after = SubscriberMemberConfigSnapshot.from(managed);
      persistSubscriberEvent(managed, before, after, SubscriberPaymentEventTypeEnum.STATUS_AUTO_UPDATED);
      changed++;
    }
    return changed;
  }

  private void persistSubscriberEvent(
      SubscriberMember sub,
      SubscriberMemberConfigSnapshot before,
      SubscriberMemberConfigSnapshot after,
      SubscriberPaymentEventTypeEnum type) {
    SubscriberPaymentEvent ev = SubscriberPaymentEvent.builder()
        .subscriberMember(sub)
        .adminUser((User) null)
        .eventType(type)
        .oldStatus(before.getStatus())
        .newStatus(after.getStatus())
        .oldNextDueDate(before.getNextDueDate())
        .newNextDueDate(after.getNextDueDate())
        .oldMonthlyFeeAmount(before.getMonthlyFeeAmount())
        .newMonthlyFeeAmount(after.getMonthlyFeeAmount())
        .oldBillingDay(before.getBillingDay())
        .newBillingDay(after.getBillingDay())
        .amount((BigDecimal) null)
        .note(null)
        .build();
    subscriberPaymentEventRepository.persist(ev);
  }
}
