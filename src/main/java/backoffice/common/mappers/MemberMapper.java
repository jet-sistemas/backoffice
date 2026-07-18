package backoffice.common.mappers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import backoffice.common.database.Pageable;
import backoffice.common.utils.MemberBillingRules;
import backoffice.v1.dtos.member.MemberAccountStatusDTO;
import backoffice.v1.dtos.member.MemberCardDTO;
import backoffice.v1.dtos.member.MemberDTO;
import backoffice.v1.dtos.member.MemberDataCreateDTO;
import backoffice.v1.dtos.member.MemberPaymentHistoryDTO;
import backoffice.v1.dtos.member.SponsoredMemberDTO;
import backoffice.v1.dtos.member.SubscriberMemberDTO;
import backoffice.v1.entities.Member;
import backoffice.v1.entities.SponsoredMember;
import backoffice.v1.entities.SubscriberMember;
import backoffice.v1.entities.SubscriberPaymentEvent;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.MemberStatusEnum;
import backoffice.v1.entities.enums.MemberTypeEnum;

public class MemberMapper {
  public static Member fromCreateData(MemberDataCreateDTO dto, User user) {
    return Member.builder()
        .user(user)
        .fullname(dto.getFullname())
        .whatsapp(dto.getWhatsapp())
        .type(MemberTypeEnum.valueOf(dto.getType().toUpperCase()))
        .build();
  }

  public static MemberCardDTO fromEntityToCardDTO(Member member) {
    var user = member.getUser();
    String name = member.getFullname() != null && !member.getFullname().isBlank()
        ? member.getFullname()
        : user.getName();
    return MemberCardDTO.builder()
        .id(member.getId())
        .userId(user.getId())
        .name(name)
        .document(user.getDocument())
        .code(user.getCode() != null ? user.getCode().toUpperCase() : null)
        .avatarUrl(user.getAvatarUrl())
        .memberType(member.getType())
        .accountActive(user.isAccountActive())
        .build();
  }

  public static MemberDTO fromEntityToDTO(Member member) {
    return fromEntityToDTO(member, null, null);
  }

  public static MemberDTO fromEntityToDTO(Member member, SubscriberMember subscriber,
      SponsoredMember sponsored) {
    MemberDTO dto = MemberDTO.builder()
        .id(member.getId())
        .userId(member.getUser().getId())
        .email(member.getUser().getEmail())
        .code(member.getUser().getCode())
        .document(member.getUser().getDocument())
        .fullname(member.getFullname())
        .whatsapp(member.getWhatsapp())
        .type(member.getType())
        .isActive(member.isActive())
        .createdAt(member.getCreatedAt())
        .build();
    if (subscriber != null) {
      dto.setSubscriber(fromSubscriberEntity(subscriber));
    }
    if (sponsored != null) {
      dto.setSponsored(fromSponsoredEntity(sponsored));
    }
    return dto;
  }

  public static SubscriberMemberDTO fromSubscriberEntity(SubscriberMember entity) {
    if (entity == null) {
      return null;
    }
    return SubscriberMemberDTO.builder()
        .id(entity.getId())
        .monthlyFeeAmount(entity.getMonthlyFeeAmount())
        .billingDay(entity.getBillingDay())
        .status(entity.getStatus())
        .nextDueDate(entity.getNextDueDate())
        .lastPaidAt(entity.getLastPaidAt())
        .createdAt(entity.getCreatedAt())
        .build();
  }

  public static SponsoredMemberDTO fromSponsoredEntity(SponsoredMember entity) {
    if (entity == null) {
      return null;
    }
    return SponsoredMemberDTO.builder()
        .memberId(entity.getMember().getId())
        .grantedByUserId(entity.getGrantedByUser().getId())
        .grantedByUser(UserMapper.fromEntityToMinimal(entity.getGrantedByUser()))
        .startAt(entity.getStartAt())
        .endAt(entity.getEndAt())
        .reason(entity.getReason())
        .active(entity.isActive())
        .createdAt(entity.getCreatedAt())
        .updatedAt(entity.getUpdatedAt())
        .build();
  }

  public static Pageable<MemberDTO> fromEntityToPageableDTO(
      Pageable<Member> data,
      Map<Long, SubscriberMember> subscriberByMemberId,
      Map<Long, SponsoredMember> sponsoredByMemberId) {
    List<MemberDTO> dtos = data.getData().stream()
        .map(m -> fromEntityToDTO(
            m,
            subscriberByMemberId.get(m.getId()),
            sponsoredByMemberId.get(m.getId())))
        .toList();

    return Pageable.<MemberDTO>builder()
        .data(dtos)
        .totalElements(data.getTotalElements())
        .totalPages(data.getTotalPages())
        .pageSize(data.getPageSize())
        .currentPage(data.getCurrentPage())
        .build();
  }

  /** Conveniência para listas vazias de enriquecimento. */
  public static Pageable<MemberDTO> fromEntityToPageableDTO(Pageable<Member> data) {
    return fromEntityToPageableDTO(data, Map.of(), Map.of());
  }

  public static Map<Long, SubscriberMember> indexSubscribersByMemberId(
      List<SubscriberMember> list) {
    if (list == null || list.isEmpty()) {
      return Map.of();
    }
    return list.stream()
        .collect(Collectors.toMap(s -> s.getMember().getId(), Function.identity(),
            (a, b) -> a, HashMap::new));
  }

  public static Map<Long, SponsoredMember> indexSponsoredByMemberId(
      List<SponsoredMember> list) {
    if (list == null || list.isEmpty()) {
      return Map.of();
    }
    return list.stream()
        .collect(Collectors.toMap(s -> s.getMember().getId(), Function.identity(),
            (a, b) -> a, HashMap::new));
  }

  public static MemberAccountStatusDTO fromSubscriberToAccountStatus(
      SubscriberMember sub, int dueSoonDays, LocalDate today) {
    MemberStatusEnum effective = MemberBillingRules.resolveEffectiveStatus(
        sub.getStatus(), today, dueSoonDays, sub.getNextDueDate());
    return MemberAccountStatusDTO.builder()
        .status(effective)
        .nextDueDate(sub.getNextDueDate())
        .lastPaidAt(sub.getLastPaidAt())
        .monthlyFeeAmount(sub.getMonthlyFeeAmount())
        .billingDay(sub.getBillingDay())
        .build();
  }

  public static MemberPaymentHistoryDTO fromPaymentEventToMemberHistory(SubscriberPaymentEvent event) {
    return MemberPaymentHistoryDTO.builder()
        .id(event.getId())
        .conferenceAt(event.getCreatedAt())
        .adminName(event.getAdminUser() != null ? event.getAdminUser().getName() : null)
        .amount(event.getAmount())
        .note(event.getNote())
        .build();
  }

  public static Pageable<MemberPaymentHistoryDTO> fromPaymentHistoryPageable(
      Pageable<SubscriberPaymentEvent> pageable) {
    List<MemberPaymentHistoryDTO> dtos = pageable.getData().stream()
        .map(MemberMapper::fromPaymentEventToMemberHistory)
        .toList();
    return Pageable.<MemberPaymentHistoryDTO>builder()
        .data(dtos)
        .totalElements(pageable.getTotalElements())
        .totalPages(pageable.getTotalPages())
        .pageSize(pageable.getPageSize())
        .currentPage(pageable.getCurrentPage())
        .build();
  }
}
