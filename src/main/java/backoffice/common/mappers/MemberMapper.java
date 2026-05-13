package backoffice.common.mappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.member.MemberDTO;
import backoffice.v1.dtos.member.MemberDataCreateDTO;
import backoffice.v1.dtos.member.SponsoredMemberDTO;
import backoffice.v1.dtos.member.SubscriberMemberDTO;
import backoffice.v1.entities.Member;
import backoffice.v1.entities.SponsoredMember;
import backoffice.v1.entities.SubscriberMember;
import backoffice.v1.entities.User;
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
}
