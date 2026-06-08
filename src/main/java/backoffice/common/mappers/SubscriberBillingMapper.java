package backoffice.common.mappers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import backoffice.common.database.Pageable;
import backoffice.common.utils.MemberBillingRules;
import backoffice.v1.dtos.billing.SubscriberBillingRowDTO;
import backoffice.v1.dtos.billing.SubscriberPaymentEventDTO;
import backoffice.v1.entities.SubscriberMember;
import backoffice.v1.entities.SubscriberPaymentEvent;
import backoffice.v1.entities.enums.MemberStatusEnum;

public final class SubscriberBillingMapper {

  private SubscriberBillingMapper() {
  }

  public static SubscriberBillingRowDTO fromSubscriberMemberToBillingRow(SubscriberMember sm, int dueSoonDays) {
    var m = sm.getMember();
    var u = m.getUser();
    LocalDate today = LocalDate.now(ZoneId.systemDefault());
    MemberStatusEnum effective = MemberBillingRules.resolveEffectiveStatus(
        sm.getStatus(), today, dueSoonDays, sm.getNextDueDate());
    var row = SubscriberBillingRowDTO.builder()
        .userId(u.getId())
        .memberId(m.getId())
        .fullname(m.getFullname())
        .email(u.getEmail())
        .document(u.getDocument())
        .whatsapp(m.getWhatsapp())
        .monthlyFeeAmount(sm.getMonthlyFeeAmount())
        .billingDay(sm.getBillingDay())
        .status(effective)
        .nextDueDate(sm.getNextDueDate())
        .lastPaidAt(sm.getLastPaidAt())
        .canMarkPayment(
            MemberBillingRules.canMarkSubscriberPayment(effective, today, dueSoonDays, sm.getNextDueDate()))
        .paymentMarkBlockedReason(
            MemberBillingRules.paymentMarkBlockedReason(effective, today, dueSoonDays, sm.getNextDueDate()))
        .build();
    return row;
  }

  public static Pageable<SubscriberBillingRowDTO> fromEntityPageableToBillingRowPage(
      Pageable<SubscriberMember> pageable, int dueSoonDays) {
    List<SubscriberBillingRowDTO> dtos = pageable.getData().stream()
        .map(sm -> fromSubscriberMemberToBillingRow(sm, dueSoonDays))
        .toList();
    return Pageable.<SubscriberBillingRowDTO>builder()
        .data(dtos)
        .totalElements(pageable.getTotalElements())
        .totalPages(pageable.getTotalPages())
        .pageSize(pageable.getPageSize())
        .currentPage(pageable.getCurrentPage())
        .build();
  }

  public static SubscriberPaymentEventDTO fromPaymentEvent(SubscriberPaymentEvent e) {
    return SubscriberPaymentEventDTO.builder()
        .id(e.getId())
        .eventType(e.getEventType())
        .oldStatus(e.getOldStatus())
        .newStatus(e.getNewStatus())
        .oldNextDueDate(e.getOldNextDueDate())
        .newNextDueDate(e.getNewNextDueDate())
        .oldMonthlyFeeAmount(e.getOldMonthlyFeeAmount())
        .newMonthlyFeeAmount(e.getNewMonthlyFeeAmount())
        .oldBillingDay(e.getOldBillingDay())
        .newBillingDay(e.getNewBillingDay())
        .amount(e.getAmount())
        .note(e.getNote())
        .createdAt(e.getCreatedAt())
        .adminUser(e.getAdminUser() == null ? null : UserMapper.fromEntityToMinimal(e.getAdminUser()))
        .build();
  }

  public static Pageable<SubscriberPaymentEventDTO> fromPaymentEventPageable(Pageable<SubscriberPaymentEvent> in) {
    List<SubscriberPaymentEventDTO> dtos = in.getData().stream()
        .map(SubscriberBillingMapper::fromPaymentEvent)
        .toList();
    return Pageable.<SubscriberPaymentEventDTO>builder()
        .data(dtos)
        .totalElements(in.getTotalElements())
        .totalPages(in.getTotalPages())
        .pageSize(in.getPageSize())
        .currentPage(in.getCurrentPage())
        .build();
  }
}
