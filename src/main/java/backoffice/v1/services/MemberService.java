package backoffice.v1.services;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import backoffice.common.database.Pageable;
import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BadRequestException;
import backoffice.common.exceptions.customs.ConflictException;
import backoffice.common.exceptions.customs.NotFoundException;
import backoffice.common.mappers.MemberMapper;
import backoffice.common.utils.MemberBillingUtil;
import backoffice.v1.dtos.billing.SubscriberMemberConfigSnapshot;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.member.MemberDTO;
import backoffice.v1.dtos.member.MemberDataCreateDTO;
import backoffice.v1.dtos.member.MemberDataUpdateDTO;
import backoffice.v1.dtos.member.SponsoredDataCreateDTO;
import backoffice.v1.dtos.member.SubscriberDataCreateDTO;
import backoffice.v1.dtos.member.SubscriberMemberUpdateDTO;
import backoffice.v1.entities.Member;
import backoffice.v1.entities.Sponsor;
import backoffice.v1.entities.SponsoredMember;
import backoffice.v1.entities.SponsoredMember.SponsoredMemberId;
import backoffice.v1.entities.SubscriberMember;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.MemberStatusEnum;
import backoffice.v1.entities.enums.MemberTypeEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import backoffice.v1.repositories.MemberRepository;
import backoffice.v1.repositories.SponsoredMemberRepository;
import backoffice.v1.repositories.SubscriberMemberRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MemberService {
  @Inject
  private MemberRepository memberRepository;

  @Inject
  private SubscriberMemberRepository subscriberMemberRepository;

  @Inject
  private SponsoredMemberRepository sponsoredMemberRepository;

  @Inject
  private UserService userService;

  @Inject
  private MemberBillingService memberBillingService;

  @Inject
  private SponsorService sponsorService;

  @Transactional
  public MemberDTO create(MemberDataCreateDTO dto, User user) {
    validateMemberForUser(user);
    validateUniqueWhatsapp(dto.getWhatsapp());

    MemberTypeEnum memberType = MemberTypeEnum.valueOf(dto.getType().toUpperCase());
    validateMemberTypePayload(memberType, dto);

    Member member = MemberMapper.fromCreateData(dto, user);
    memberRepository.persistAndFlush(member);

    if (memberType == MemberTypeEnum.SUBSCRIBER) {
      createSubscriberRow(member, dto.getSubscriber());
    } else if (memberType == MemberTypeEnum.SPONSORED) {
      createSponsoredRow(member, dto.getSponsored());
    }

    return loadMemberDto(member.getId());
  }

  private void validateMemberTypePayload(MemberTypeEnum type, MemberDataCreateDTO dto) {
    if (type == MemberTypeEnum.SUBSCRIBER) {
      if (dto.getSubscriber() == null) {
        throw new BadRequestException(MessageErrorEnum.MEMBER_SUBSCRIBER_DATA_REQUIRED.getMessage());
      }
      if (dto.getSponsored() != null) {
        throw new BadRequestException(MessageErrorEnum.MEMBER_SUBSCRIBER_EXTRA_INVALID.getMessage());
      }
      return;
    }
    if (type == MemberTypeEnum.SPONSORED) {
      if (dto.getSponsored() == null) {
        throw new BadRequestException(MessageErrorEnum.MEMBER_SPONSORED_DATA_REQUIRED.getMessage());
      }
      if (dto.getSubscriber() != null) {
        throw new BadRequestException(MessageErrorEnum.MEMBER_SPONSORED_EXTRA_INVALID.getMessage());
      }
    }
  }

  private void createSubscriberRow(Member member, SubscriberDataCreateDTO subDto) {
    LocalDate today = LocalDate.now(ZoneId.systemDefault());
    LocalDate nextDue = subDto.getNextDueDate();
    if (nextDue == null) {
      nextDue = MemberBillingUtil.computeNextDueDate(today, subDto.getBillingDay());
    } else if (nextDue.isBefore(today)) {
      throw new BadRequestException(
          "A data de próximo vencimento não pode ser anterior à data atual.");
    }

    SubscriberMember row = SubscriberMember.builder()
        .member(member)
        .monthlyFeeAmount(subDto.getMonthlyFeeAmount())
        .billingDay(subDto.getBillingDay())
        .status(MemberStatusEnum.ACTIVE)
        .nextDueDate(nextDue)
        .build();
    subscriberMemberRepository.persistAndFlush(row);
  }

  private void createSponsoredRow(Member member, SponsoredDataCreateDTO spDto) {
    User grantUser = userService.findById(spDto.getGrantedByUserId())
        .orElseThrow(() -> new BadRequestException(MessageErrorEnum.MEMBER_SPONSORED_GRANT_INVALID.getMessage()));
    validateGrantUser(grantUser);

    if (sponsoredMemberRepository.existsByMemberId(member.getId())) {
      throw new ConflictException(MessageErrorEnum.MEMBER_ALREADY_SPONSORED.getMessage());
    }

    SponsoredMemberId id = new SponsoredMemberId(member.getId(), grantUser.getId());
    SponsoredMember row = SponsoredMember.builder()
        .id(id)
        .member(member)
        .grantedByUser(grantUser)
        .startAt(spDto.getStartAt())
        .endAt(spDto.getEndAt())
        .reason(spDto.getReason())
        .build();
    sponsoredMemberRepository.persistAndFlush(row);
  }

  private void validateGrantUser(User grantUser) {
    if (!UserTypeEnum.SPONSOR.equals(grantUser.getType())
        && !UserTypeEnum.SPONSOR_MEMBER.equals(grantUser.getType())) {
      throw new BadRequestException(MessageErrorEnum.MEMBER_SPONSORED_GRANT_INVALID.getMessage());
    }
    Sponsor sponsor = sponsorService.findByUserId(grantUser.getId())
        .orElseThrow(() -> new BadRequestException(MessageErrorEnum.MEMBER_SPONSORED_GRANT_INVALID.getMessage()));
    if (!sponsor.isActive()) {
      throw new BadRequestException(MessageErrorEnum.MEMBER_SPONSORED_GRANT_INVALID.getMessage());
    }
  }

  private MemberDTO loadMemberDto(Long memberId) {
    Member member = memberRepository.findByIdOptional(memberId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_NOT_FOUND.getMessage()));
    SubscriberMember sub = subscriberMemberRepository.findByMemberId(memberId).orElse(null);
    SponsoredMember sp = sponsoredMemberRepository.findFirstByMemberId(memberId).orElse(null);
    MemberDTO dto = MemberMapper.fromEntityToDTO(member, sub, sp);
    enrichSubscriberIfPresent(dto, sub);
    return dto;
  }

  private void enrichSubscriberIfPresent(MemberDTO dto, SubscriberMember sub) {
    if (sub != null && dto.getSubscriber() != null) {
      memberBillingService.enrichSubscriberPaymentMarkFields(dto.getSubscriber(), sub);
    }
  }

  public Optional<Member> findById(Long memberId) {
    return memberRepository.findByIdOptional(memberId);
  }

  public MemberDTO findDTOById(Long memberId) {
    Member member = memberRepository.findByIdOptional(memberId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_NOT_FOUND.getMessage()));
    validateMemberForUser(member.getUser());
    SubscriberMember sub = subscriberMemberRepository.findByMemberId(memberId).orElse(null);
    SponsoredMember sp = sponsoredMemberRepository.findFirstByMemberId(memberId).orElse(null);
    MemberDTO dto = MemberMapper.fromEntityToDTO(member, sub, sp);
    enrichSubscriberIfPresent(dto, sub);
    return dto;
  }

  public Pageable<MemberDTO> list(MemberTypeEnum type, Boolean isActive, String search, PageDTO pageDTO) {
    Pageable<Member> pageable = memberRepository.findAllPaginated(type, isActive, search, pageDTO);
    List<Long> ids = pageable.getData().stream().map(Member::getId).toList();
    if (ids.isEmpty()) {
      return MemberMapper.fromEntityToPageableDTO(pageable, Map.of(), Map.of());
    }
    Map<Long, SubscriberMember> subMap =
        MemberMapper.indexSubscribersByMemberId(subscriberMemberRepository.findByMemberIdIn(ids));
    Map<Long, SponsoredMember> spMap = dedupeSponsoredByMember(
        sponsoredMemberRepository.findByMemberIdIn(ids));
    Pageable<MemberDTO> page = MemberMapper.fromEntityToPageableDTO(pageable, subMap, spMap);
    for (MemberDTO dto : page.getData()) {
      enrichSubscriberIfPresent(dto, subMap.get(dto.getId()));
    }
    return page;
  }

  private static Map<Long, SponsoredMember> dedupeSponsoredByMember(List<SponsoredMember> list) {
    Map<Long, SponsoredMember> map = new HashMap<>();
    for (SponsoredMember s : list) {
      map.putIfAbsent(s.getMember().getId(), s);
    }
    return map;
  }

  @Transactional
  public MemberDTO patchSubscriberMember(Long memberId, SubscriberMemberUpdateDTO dto, Long adminActorId) {
    Member member = memberRepository.findByIdOptional(memberId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_NOT_FOUND.getMessage()));
    validateMemberForUser(member.getUser());
    if (!MemberTypeEnum.SUBSCRIBER.equals(member.getType())) {
      throw new BadRequestException(MessageErrorEnum.MEMBER_SUBSCRIBER_UPDATE_INVALID.getMessage());
    }
    SubscriberMember sub = subscriberMemberRepository.findByMemberId(memberId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_SUBSCRIBER_NOT_FOUND.getMessage()));

    SubscriberMemberConfigSnapshot before = SubscriberMemberConfigSnapshot.from(sub);

    MemberStatusEnum statusBeforePatch = sub.getStatus();
    int previousBillingDay = sub.getBillingDay();

    if (dto.getMonthlyFeeAmount() != null) {
      sub.setMonthlyFeeAmount(dto.getMonthlyFeeAmount());
    }

    boolean appliedBillingDayRecalc = false;
    if (dto.getBillingDay() != null) {
      int d = dto.getBillingDay();
      boolean billingDayChanging = d != previousBillingDay;
      sub.setBillingDay(d);
      if (billingDayChanging && (statusBeforePatch == MemberStatusEnum.ACTIVE
          || statusBeforePatch == MemberStatusEnum.DUE_SOON)) {
        memberBillingService.applyBillingDayRecalc(sub, previousBillingDay, d);
        appliedBillingDayRecalc = true;
      }
    }

    if (!appliedBillingDayRecalc && dto.getNextDueDate() != null) {
      sub.setNextDueDate(dto.getNextDueDate());
    }
    if (!appliedBillingDayRecalc && dto.getStatus() != null && !dto.getStatus().isBlank()) {
      sub.setStatus(MemberStatusEnum.valueOf(dto.getStatus().trim().toUpperCase()));
    }

    memberBillingService.normalizeSubscriberStatusAfterManualPatch(sub);

    subscriberMemberRepository.persistAndFlush(sub);
    memberBillingService.recordConfigChangeAfterPatch(sub, before, adminActorId);
    return loadMemberDto(memberId);
  }

  public Optional<Member> findByUserId(Long userId) {
    return memberRepository.findByUserId(userId);
  }

  public MemberDTO findDTOByUserId(Long userId) {
    Member member = memberRepository.findByUserId(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_NOT_FOUND.getMessage()));
    validateMemberForUser(member.getUser());
    return loadMemberDto(member.getId());
  }

  public Map<Long, MemberDTO> findDTOsByUserIds(List<Long> userIds) {
    if (userIds == null || userIds.isEmpty()) {
      return Map.of();
    }
    List<Member> members = memberRepository.findByUserIdIn(userIds);
    if (members.isEmpty()) {
      return Map.of();
    }
    List<Long> memberIds = members.stream().map(Member::getId).toList();
    Map<Long, SubscriberMember> subMap =
        MemberMapper.indexSubscribersByMemberId(subscriberMemberRepository.findByMemberIdIn(memberIds));
    Map<Long, SponsoredMember> spMap = dedupeSponsoredByMember(
        sponsoredMemberRepository.findByMemberIdIn(memberIds));
    Map<Long, MemberDTO> out = new HashMap<>();
    for (Member m : members) {
      MemberDTO mdto = MemberMapper.fromEntityToDTO(m, subMap.get(m.getId()), spMap.get(m.getId()));
      enrichSubscriberIfPresent(mdto, subMap.get(m.getId()));
      out.put(m.getUser().getId(), mdto);
    }
    return out;
  }

  public MemberDTO patchSubscriberMemberByUserId(Long userId, SubscriberMemberUpdateDTO dto, Long adminActorId) {
    Member member = memberRepository.findByUserId(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_NOT_FOUND.getMessage()));
    return patchSubscriberMember(member.getId(), dto, adminActorId);
  }

  @Transactional
  public MemberDTO updateMemberDataByUserId(Long userId, MemberDataUpdateDTO dto) {
    if (dto.getFullname() == null && dto.getWhatsapp() == null) {
      return findDTOByUserId(userId);
    }
    Member member = memberRepository.findByUserId(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.MEMBER_NOT_FOUND.getMessage()));
    validateMemberForUser(member.getUser());

    if (dto.getFullname() != null) {
      String trimmed = dto.getFullname().trim();
      if (trimmed.isEmpty()) {
        throw new BadRequestException(MessageErrorEnum.MEMBER_FULLNAME_REQUIRED.getMessage());
      }
      member.setFullname(trimmed);
    }
    if (dto.getWhatsapp() != null) {
      if (memberRepository.existsByWhatsappAndIdNot(member.getId(), dto.getWhatsapp())) {
        throw new ConflictException(MessageErrorEnum.MEMBER_ALREADY_EXISTS.getMessage());
      }
      member.setWhatsapp(dto.getWhatsapp());
    }

    memberRepository.persistAndFlush(member);
    return loadMemberDto(member.getId());
  }

  public void deactivateByUserId(Long userId) {
    memberRepository.findByUserId(userId).ifPresent(member -> {
      member.setActive(false);
      memberRepository.persistAndFlush(member);
      subscriberMemberRepository.findByMemberId(member.getId()).ifPresent(sub -> {
        sub.setStatus(MemberStatusEnum.INACTIVE);
        subscriberMemberRepository.persistAndFlush(sub);
      });
    });
  }

  public void activateByUserId(Long userId) {
    memberRepository.findByUserId(userId).ifPresent(member -> {
      member.setActive(true);
      memberRepository.persistAndFlush(member);
      subscriberMemberRepository.findByMemberId(member.getId()).ifPresent(sub -> {
        sub.setStatus(MemberStatusEnum.ACTIVE);
        subscriberMemberRepository.persistAndFlush(sub);
      });
    });
  }

  public void deleteCascadeByUserId(Long userId) {
    Optional<Member> opt = memberRepository.findByUserId(userId);
    if (opt.isEmpty()) {
      return;
    }
    Member member = opt.get();
    Long memberId = member.getId();
    subscriberMemberRepository.findByMemberId(memberId)
        .ifPresent(sub -> subscriberMemberRepository.delete(sub));
    sponsoredMemberRepository.delete("member.id = ?1", memberId);
    memberRepository.delete(member);
  }

  public void validateUniqueWhatsapp(String whatsapp) {
    if (memberRepository.existsByWhatsapp(whatsapp)) {
      throw new ConflictException(MessageErrorEnum.MEMBER_ALREADY_EXISTS.getMessage());
    }
  }

  public void validateMemberForUser(User user) {
    if (!UserTypeEnum.MEMBER.equals(user.getType())) {
      throw new ConflictException(MessageErrorEnum.USER_INVALID_TYPE_ENUM.getMessage());
    }
  }
}
