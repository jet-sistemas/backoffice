package backoffice.v1.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import backoffice.common.database.Pageable;
import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BadRequestException;
import backoffice.common.exceptions.customs.ConflictException;
import backoffice.common.exceptions.customs.NotFoundException;
import backoffice.common.mappers.SubscriberBillingMapper;
import backoffice.common.utils.MemberBillingRules;
import backoffice.common.utils.MemberBillingUtil;
import backoffice.v1.dtos.member.SubscriberMemberDTO;
import backoffice.v1.dtos.billing.ListSubscriberBillingQueryDTO;
import backoffice.v1.dtos.billing.SubscriberBillingListResultDTO;
import backoffice.v1.dtos.billing.SubscriberBillingSummaryDTO;
import backoffice.v1.dtos.billing.SubscriberMemberConfigSnapshot;
import backoffice.v1.dtos.billing.SubscriberPaymentMarkPaidDTO;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.billing.SubscriberPaymentEventDTO;
import backoffice.v1.entities.Member;
import backoffice.v1.entities.SubscriberMember;
import backoffice.v1.entities.SubscriberPaymentEvent;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.MemberStatusEnum;
import backoffice.v1.entities.enums.MemberTypeEnum;
import backoffice.v1.entities.enums.SubscriberPaymentEventTypeEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import backoffice.v1.repositories.MemberRepository;
import backoffice.v1.repositories.SubscriberMemberRepository;
import backoffice.v1.repositories.SubscriberPaymentEventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MemberBillingService {
  private static final Logger LOG = Logger.getLogger(MemberBillingService.class);

  @Inject
  SubscriberMemberRepository subscriberMemberRepository;

  @Inject
  SubscriberPaymentEventRepository subscriberPaymentEventRepository;

  @Inject
  MemberRepository memberRepository;

  @Inject
  UserService userService;

  @ConfigProperty(name = "backoffice.billing.due-soon-days", defaultValue = "5")
  int dueSoonDays;

  @Transactional
  public int refreshBillingStatuses() {
    long startedAt = System.nanoTime();
    LOG.infof("billing.status.job started");
    LocalDate today = LocalDate.now(ZoneId.systemDefault());
    LocalDate windowEnd = today.plusDays(dueSoonDays);
    int changed = 0;
    changed += applyAutomationPass(today, windowEnd, MemberStatusEnum.OVERDUE);
    changed += applyAutomationPass(today, windowEnd, MemberStatusEnum.DUE_SOON);
    changed += applyAutomationPass(today, windowEnd, MemberStatusEnum.ACTIVE);
    LOG.infof("billing.status.job finished; transitions=%d", changed);
    LOG.infof("billing.status.job total execution time: %d ms", (System.nanoTime() - startedAt) / 1_000_000);
    return changed;
  }

  /**
   * Recalcula vencimento e status quando o dia de cobrança muda (membro ACTIVE ou DUE_SOON antes do patch).
   */
  public void applyBillingDayRecalc(SubscriberMember sub, int oldBillingDay, int newBillingDay) {
    LocalDate shifted = MemberBillingUtil.shiftNextDueDateForBillingDayChange(sub.getNextDueDate(), oldBillingDay,
        newBillingDay);
    sub.setNextDueDate(shifted);
    LocalDate today = LocalDate.now(ZoneId.systemDefault());
    sub.setStatus(MemberBillingRules.expectedAutomationStatus(today, dueSoonDays, shifted));
  }

  public void enrichSubscriberPaymentMarkFields(SubscriberMemberDTO dto, SubscriberMember entity) {
    LocalDate today = LocalDate.now(ZoneId.systemDefault());
    MemberStatusEnum effective = MemberBillingRules.resolveEffectiveStatus(
        entity.getStatus(), today, dueSoonDays, entity.getNextDueDate());
    dto.setStatus(effective);
    dto.setCanMarkPayment(
        MemberBillingRules.canMarkSubscriberPayment(effective, today, dueSoonDays, entity.getNextDueDate()));
    dto.setPaymentMarkBlockedReason(
        MemberBillingRules.paymentMarkBlockedReason(effective, today, dueSoonDays, entity.getNextDueDate()));
  }

  private int applyAutomationPass(LocalDate today, LocalDate windowEnd, MemberStatusEnum phase) {
    int changed = 0;
    for (SubscriberMember s : subscriberMemberRepository.listNeedingAutomationStatus(today, windowEnd, phase)) {
      if (s.getStatus() == MemberStatusEnum.INACTIVE) {
        continue;
      }
      MemberStatusEnum target = MemberBillingRules.expectedAutomationStatus(today, dueSoonDays, s.getNextDueDate());
      if (target != phase || s.getStatus() == target) {
        continue;
      }
      SubscriberMemberConfigSnapshot before = SubscriberMemberConfigSnapshot.from(s);
      s.setStatus(target);
      subscriberMemberRepository.persist(s);
      SubscriberMemberConfigSnapshot after = SubscriberMemberConfigSnapshot.from(s);
      persistSubscriberEvent(s, before, after, SubscriberPaymentEventTypeEnum.STATUS_AUTO_UPDATED, null, null, null);
      changed++;
    }
    return changed;
  }

  @Transactional
  public void markSubscriberPaidByUserId(Long userId, SubscriberPaymentMarkPaidDTO dto, Long adminUserId) {
    Member member = memberRepository.findByUserId(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_NOT_FOUND.getMessage()));
    validateMemberUser(member);
    if (!MemberTypeEnum.SUBSCRIBER.equals(member.getType())) {
      throw new BadRequestException(MessageErrorEnum.MEMBER_SUBSCRIBER_UPDATE_INVALID.getMessage());
    }
    SubscriberMember sub = subscriberMemberRepository.findByMemberId(member.getId())
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_SUBSCRIBER_NOT_FOUND.getMessage()));

    Instant paidAt = dto.getPaidAt() != null ? dto.getPaidAt() : Instant.now();
    BigDecimal amount = dto.getAmountPaid() != null ? dto.getAmountPaid() : sub.getMonthlyFeeAmount();
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new BadRequestException("O valor pago deve ser maior que zero.");
    }

    LocalDate today = LocalDate.now(ZoneId.systemDefault());

    MemberStatusEnum effective = MemberBillingRules.resolveEffectiveStatus(
        sub.getStatus(), today, dueSoonDays, sub.getNextDueDate());

    if (effective != sub.getStatus()) {
      sub.setStatus(effective);
    }

    SubscriberMemberConfigSnapshot before = SubscriberMemberConfigSnapshot.from(sub);

    if (effective == MemberStatusEnum.INACTIVE) {
      throw new BadRequestException(MessageErrorEnum.SUBSCRIBER_PAYMENT_INACTIVE.getMessage());
    }

    if (effective == MemberStatusEnum.ACTIVE || effective == MemberStatusEnum.DUE_SOON) {
      if (!MemberBillingRules.canMarkSubscriberPayment(effective, today, dueSoonDays, sub.getNextDueDate())) {
        String msg = MemberBillingRules.paymentMarkBlockedReason(effective, today, dueSoonDays, sub.getNextDueDate());
        throw new BadRequestException(
            msg != null ? msg : MessageErrorEnum.SUBSCRIBER_PAYMENT_ALREADY_REGISTERED.getMessage());
      }
      advanceDueDateAfterPayment(sub, paidAt, today);
    } else if (effective == MemberStatusEnum.OVERDUE) {
      if (MemberBillingRules.shouldAdvanceOverduePayment(today, sub.getNextDueDate(),
          sub.isOverdueDueAdvancePending())) {
        advanceDueDateAfterPayment(sub, paidAt, today);
      } else {
        sub.setLastPaidAt(paidAt);
        sub.setOverdueDueAdvancePending(true);
      }
    }

    subscriberMemberRepository.persistAndFlush(sub);

    User admin = resolveAdminActor(adminUserId);
    SubscriberMemberConfigSnapshot after = SubscriberMemberConfigSnapshot.from(sub);
    persistSubscriberEvent(sub, before, after, SubscriberPaymentEventTypeEnum.PAYMENT_MARKED_PAID, amount,
        dto.getNote(), admin);
  }

  private void advanceDueDateAfterPayment(SubscriberMember sub, Instant paidAt, LocalDate today) {
    LocalDate nd = MemberBillingUtil.advanceNextDueDate(sub.getNextDueDate(), sub.getBillingDay(), today);
    sub.setLastPaidAt(paidAt);
    sub.setNextDueDate(nd);
    sub.setOverdueDueAdvancePending(false);
    sub.setStatus(MemberBillingRules.expectedAutomationStatus(today, dueSoonDays, nd));
  }

  @Transactional
  public void recordConfigChangeAfterPatch(SubscriberMember sub, SubscriberMemberConfigSnapshot before,
      Long adminUserId) {
    if (before.equalsSnapshotOf(sub)) {
      return;
    }
    User admin = resolveAdminActor(adminUserId);
    SubscriberMemberConfigSnapshot after = SubscriberMemberConfigSnapshot.from(sub);
    persistSubscriberEvent(sub, before, after, SubscriberPaymentEventTypeEnum.BILLING_CONFIG_UPDATED, null, null,
        admin);
  }

  public Pageable<SubscriberPaymentEventDTO> listPaymentEventsByUserId(Long userId, PageDTO pageDTO) {
    Member member = memberRepository.findByUserId(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_NOT_FOUND.getMessage()));
    validateMemberUser(member);
    if (!MemberTypeEnum.SUBSCRIBER.equals(member.getType())) {
      throw new BadRequestException(MessageErrorEnum.MEMBER_SUBSCRIBER_UPDATE_INVALID.getMessage());
    }
    SubscriberMember sub = subscriberMemberRepository.findByMemberId(member.getId())
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_SUBSCRIBER_NOT_FOUND.getMessage()));
    var page = subscriberPaymentEventRepository.findBySubscriberMemberId(sub.getId(), pageDTO);
    return SubscriberBillingMapper.fromPaymentEventPageable(page);
  }

  public SubscriberBillingListResultDTO listSubscriberBilling(ListSubscriberBillingQueryDTO query) {
    var statusFilter = query.resolveStatusFilter().resolveMemberStatusOrNull();
    PageDTO pageDTO = query.resolvePage();
    var entityPage = subscriberMemberRepository.findSubscriberBillingPage(
        statusFilter,
        query.getDueFrom(),
        query.getDueTo(),
        query.resolveSearch(),
        pageDTO);
    var rowPage = SubscriberBillingMapper.fromEntityPageableToBillingRowPage(entityPage, dueSoonDays);

    var summary = SubscriberBillingSummaryDTO.builder()
        .overdueCount(subscriberMemberRepository.countWithStatusEnum(MemberStatusEnum.OVERDUE))
        .dueSoonCount(subscriberMemberRepository.countWithStatusEnum(MemberStatusEnum.DUE_SOON))
        .activeCount(subscriberMemberRepository.countWithStatusEnum(MemberStatusEnum.ACTIVE))
        .inactiveCount(subscriberMemberRepository.countWithStatusEnum(MemberStatusEnum.INACTIVE))
        .build();

    return SubscriberBillingListResultDTO.builder()
        .summary(summary)
        .rows(rowPage.getData())
        .totalElements(rowPage.getTotalElements())
        .totalPages(rowPage.getTotalPages())
        .pageSize(rowPage.getPageSize())
        .currentPage(rowPage.getCurrentPage())
        .build();
  }

  private void validateMemberUser(Member member) {
    if (!UserTypeEnum.MEMBER.equals(member.getUser().getType())) {
      throw new ConflictException(MessageErrorEnum.USER_INVALID_TYPE_ENUM.getMessage());
    }
  }

  private User resolveAdminActor(Long adminUserId) {
    if (adminUserId == null) {
      return null;
    }
    return userService.findById(adminUserId).orElse(null);
  }

  private void persistSubscriberEvent(
      SubscriberMember sub,
      SubscriberMemberConfigSnapshot before,
      SubscriberMemberConfigSnapshot after,
      SubscriberPaymentEventTypeEnum type,
      BigDecimal amount,
      String note,
      User adminUser) {
    SubscriberPaymentEvent ev = SubscriberPaymentEvent.builder()
        .subscriberMember(sub)
        .adminUser(adminUser)
        .eventType(type)
        .oldStatus(before.getStatus())
        .newStatus(after.getStatus())
        .oldNextDueDate(before.getNextDueDate())
        .newNextDueDate(after.getNextDueDate())
        .oldMonthlyFeeAmount(before.getMonthlyFeeAmount())
        .newMonthlyFeeAmount(after.getMonthlyFeeAmount())
        .oldBillingDay(before.getBillingDay())
        .newBillingDay(after.getBillingDay())
        .amount(amount)
        .note(note)
        .build();
    subscriberPaymentEventRepository.persist(ev);
  }
}
