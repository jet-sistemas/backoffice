package backoffice.v1.services;

import java.util.List;
import java.util.Map;

import backoffice.common.database.Pageable;
import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BadRequestException;
import backoffice.common.exceptions.customs.BusinessException;
import backoffice.common.exceptions.customs.NotFoundException;
import backoffice.common.mappers.UserMapper;
import backoffice.common.utils.PasswordPolicyService;
import backoffice.common.utils.PasswordUtils;
import backoffice.v1.dtos.accountvalidation.ResendAccountValidationDTO;
import backoffice.v1.dtos.benefit.BenefitCreateDTO;
import backoffice.v1.dtos.benefit.BenefitDTO;
import backoffice.v1.dtos.benefit.BenefitUpdateDTO;
import backoffice.v1.dtos.checkin.AdminCheckinDTO;
import backoffice.v1.dtos.checkin.ListAdminCheckinsQueryDTO;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.member.MemberDTO;
import backoffice.v1.dtos.member.SubscriberMemberUpdateDTO;
import backoffice.v1.dtos.billing.ListSubscriberBillingQueryDTO;
import backoffice.v1.dtos.billing.SubscriberBillingListResultDTO;
import backoffice.v1.dtos.billing.SubscriberPaymentEventDTO;
import backoffice.v1.dtos.billing.SubscriberPaymentMarkPaidDTO;
import backoffice.v1.dtos.user.UserCreateDTO;
import backoffice.v1.dtos.user.UserWithSponsorCreateDTO;
import backoffice.v1.dtos.user.UserWithSponsorDTO;
import backoffice.v1.dtos.user.UserWithSponsorUpdateDTO;
import backoffice.v1.entities.Sponsor;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.MemberTypeEnum;
import backoffice.v1.entities.enums.SponsorEntityTypeEnum;
import backoffice.v1.entities.enums.SponsorPersonaEnum;
import backoffice.v1.entities.enums.SponsorTierEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AdminService {
  @Inject
  private UserService userService;

  @Inject
  private SponsorService sponsorService;

  @Inject
  private BenefitService benefitService;

  @Inject
  private MemberService memberService;

  @Inject
  private MemberBillingService memberBillingService;

  @Inject
  private AccountValidationService accountValidationService;

  @Inject
  private SponsorCheckinService sponsorCheckinService;

  @Transactional
  public UserWithSponsorDTO createUser(UserWithSponsorCreateDTO dto, Long adminActorId) {
    UserCreateDTO userData = dto.getUser();
    UserTypeEnum type = resolveUserType(userData.getType());

    validateTypeRequirements(type, dto);

    userService.validateUniqueFields(userData.getEmail(), userData.getDocument(), userData.getCode());
    userData.setType(type.name());

    User adminActor = adminActorId != null
        ? userService.findById(adminActorId).orElse(null)
        : null;

    String temporaryPassword;
    if (type == UserTypeEnum.ADM) {
      temporaryPassword = PasswordPolicyService.generateTemporaryPassword();
      userData.setPassword(PasswordUtils.hashPass(temporaryPassword));
      User user = userService.create(userData);
      user.setAccountActive(true);
      user.setMustChangePassword(false);
      userService.persistAndFlush(user);
      return UserMapper.fromEntityToUserWithSponsorDTO(user, null, null,
          accountValidationService.resolveStatus(user), false, false);
    }

    temporaryPassword = PasswordPolicyService.generateTemporaryPassword();
    userData.setPassword(PasswordUtils.hashPass(temporaryPassword));

    User user = userService.create(userData);
    user.setAccountActive(false);
    user.setMustChangePassword(false);
    userService.persistAndFlush(user);

    Sponsor sponsor = null;
    if (isSponsorType(type)) {
      sponsor = sponsorService.create(dto.getSponsor(), user);
    }
    if (UserTypeEnum.MEMBER.equals(type)) {
      memberService.create(dto.getMember(), user, adminActorId);
    }

    accountValidationService.createInviteAndSendEmail(user, adminActor, temporaryPassword);

    MemberDTO memberDto = null;
    if (UserTypeEnum.MEMBER.equals(type)) {
      memberDto = memberService.findDTOByUserId(user.getId());
    }

    return mapUserWithValidationFlags(user, sponsor, memberDto);
  }

  @Transactional
  public UserWithSponsorDTO updateUser(Long userId, UserWithSponsorUpdateDTO dto) {
    User user = userService.findById(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    userService.validateUniqueFieldsForUpdate(userId, dto.getEmail(), dto.getDocument());

    UserMapper.applyUpdate(dto, user);
    userService.persistAndFlush(user);

    Sponsor sponsor = null;
    if (isSponsorType(user.getType())) {
      sponsor = sponsorService.findByUserId(userId)
          .orElseThrow(() -> new NotFoundException(MessageErrorEnum.SPONSOR_NOT_FOUND.getMessage()));

      if (dto.getSponsor() != null) {
        sponsor = sponsorService.update(sponsor, dto.getSponsor());
      }
    }

    if (UserTypeEnum.MEMBER.equals(user.getType()) && dto.getMember() != null) {
      memberService.updateMemberDataByUserId(userId, dto.getMember());
    }

    MemberDTO memberDto = null;
    if (UserTypeEnum.MEMBER.equals(user.getType())) {
      memberDto = memberService.findDTOByUserId(userId);
    }

    return mapUserWithValidationFlags(user, sponsor, memberDto);
  }

  @Transactional
  public ResendAccountValidationDTO resendAccountValidation(Long userId, Long adminActorId) {
    User user = userService.findById(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    if (user.getEmailVerifiedAt() == null) {
      return accountValidationService.resendInvite(userId, adminActorId);
    }
    if (user.isMustChangePassword()) {
      return accountValidationService.resendTemporaryPassword(userId, adminActorId);
    }
    throw new BadRequestException(MessageErrorEnum.ACCOUNT_VALIDATION_RESEND_NOT_APPLICABLE.getMessage());
  }

  @Transactional
  public void deactivateUser(Long userId) {
    User user = userService.findById(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    user.setAccountActive(false);
    userService.persistAndFlush(user);

    if (isSponsorType(user.getType())) {
      sponsorService.deactivateByUserId(userId);
    } else if (UserTypeEnum.MEMBER.equals(user.getType())) {
      memberService.deactivateByUserId(userId);
    }
  }

  @Transactional
  public void activateUser(Long userId) {
    User user = userService.findById(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    user.setAccountActive(true);
    userService.persistAndFlush(user);

    if (isSponsorType(user.getType())) {
      sponsorService.activateByUserId(userId);
    } else if (UserTypeEnum.MEMBER.equals(user.getType())) {
      memberService.activateByUserId(userId);
    }
  }

  @Transactional
  public void deleteUser(Long userId) {
    User user = userService.findById(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    if (isSponsorType(user.getType())) {
      sponsorService.deleteByUserId(userId);
    } else if (UserTypeEnum.MEMBER.equals(user.getType())) {
      memberService.deleteCascadeByUserId(userId);
    }

    userService.delete(user);
  }

  public UserWithSponsorDTO findUserById(Long userId) {
    return userService.findById(userId)
        .map(user -> {
          Sponsor sponsor = isSponsorType(user.getType())
              ? sponsorService.findByUserId(userId).orElse(null)
              : null;
          MemberDTO member = UserTypeEnum.MEMBER.equals(user.getType())
              ? memberService.findDTOByUserId(userId)
              : null;
          return mapUserWithValidationFlags(user, sponsor, member);
        })
        .orElse(null);
  }

  public Pageable<UserWithSponsorDTO> listUsers(UserTypeEnum type, SponsorTierEnum tier,
      SponsorEntityTypeEnum entityType, SponsorPersonaEnum persona, MemberTypeEnum memberType,
      Boolean isActive, String search, PageDTO pageDTO) {
    if (memberType != null && type != UserTypeEnum.MEMBER) {
      throw new BadRequestException(MessageErrorEnum.MEMBER_TYPE_FILTER_REQUIRES_USER_TYPE_MEMBER.getMessage());
    }
    SponsorPersonaEnum effectivePersona =
        entityType == SponsorEntityTypeEnum.PERSON ? persona : null;
    Pageable<User> pageable =
        userService.listUsers(type, tier, entityType, effectivePersona, memberType, isActive, search, pageDTO);

    List<Long> sponsorUserIds = pageable.getData().stream()
        .filter(u -> isSponsorType(u.getType()))
        .map(User::getId)
        .toList();

    Map<Long, Sponsor> sponsorsByUserId = sponsorService.findByUserIds(sponsorUserIds);

    List<Long> memberUserIds = pageable.getData().stream()
        .filter(u -> UserTypeEnum.MEMBER.equals(u.getType()))
        .map(User::getId)
        .toList();
    Map<Long, MemberDTO> membersByUserId = memberService.findDTOsByUserIds(memberUserIds);

    List<UserWithSponsorDTO> dtos = pageable.getData().stream()
        .map(user -> mapUserWithValidationFlags(
            user,
            sponsorsByUserId.get(user.getId()),
            membersByUserId.get(user.getId())))
        .toList();

    return Pageable.<UserWithSponsorDTO>builder()
        .data(dtos)
        .totalElements(pageable.getTotalElements())
        .totalPages(pageable.getTotalPages())
        .pageSize(pageable.getPageSize())
        .currentPage(pageable.getCurrentPage())
        .build();
  }

  @Transactional
  public MemberDTO patchSubscriberMemberByUserId(Long userId, SubscriberMemberUpdateDTO dto, Long adminUserId) {
    return memberService.patchSubscriberMemberByUserId(userId, dto, adminUserId);
  }

  @Transactional
  public MemberDTO markSubscriberPaidByUserId(Long userId, SubscriberPaymentMarkPaidDTO dto, Long adminUserId) {
    memberBillingService.markSubscriberPaidByUserId(userId, dto, adminUserId);
    return memberService.findDTOByUserId(userId);
  }

  public Pageable<SubscriberPaymentEventDTO> listSubscriberPaymentEvents(Long userId, PageDTO pageDTO) {
    return memberBillingService.listPaymentEventsByUserId(userId, pageDTO);
  }

  public SubscriberBillingListResultDTO listSubscriberBilling(ListSubscriberBillingQueryDTO query) {
    return memberBillingService.listSubscriberBilling(query);
  }

  private UserTypeEnum resolveUserType(String type) {
    if (type == null || type.isBlank()) {
      throw new BadRequestException(MessageErrorEnum.USER_INVALID_TYPE_ENUM.getMessage());
    }
    return UserTypeEnum.valueOf(type.toUpperCase());
  }

  private void validateTypeRequirements(UserTypeEnum type, UserWithSponsorCreateDTO dto) {
    if (type == UserTypeEnum.SPONSOR_MEMBER) {
      throw new BusinessException(MessageErrorEnum.USER_TYPE_NOT_IMPLEMENTED.getMessage(), 400);
    }

    if (isSponsorType(type) && dto.getSponsor() == null) {
      throw new BadRequestException(MessageErrorEnum.SPONSOR_DATA_REQUIRED.getMessage());
    }
    if (UserTypeEnum.MEMBER.equals(type) && dto.getMember() == null) {
      throw new BadRequestException(MessageErrorEnum.MEMBER_DATA_REQUIRED.getMessage());
    }
  }

  private boolean isSponsorType(UserTypeEnum type) {
    return UserTypeEnum.SPONSOR.equals(type) || UserTypeEnum.SPONSOR_MEMBER.equals(type);
  }

  private UserWithSponsorDTO mapUserWithValidationFlags(User user, Sponsor sponsor, MemberDTO member) {
    return UserMapper.fromEntityToUserWithSponsorDTO(
        user,
        sponsor,
        member,
        accountValidationService.resolveStatus(user),
        accountValidationService.canResendInvite(user),
        accountValidationService.canResendTemporaryPassword(user));
  }

  public BenefitDTO createBenefit(BenefitCreateDTO dto) {
    return benefitService.create(dto);
  }

  public BenefitDTO updateBenefit(Long benefitId, BenefitUpdateDTO dto) {
    return benefitService.update(benefitId, dto);
  }

  public BenefitDTO findBenefitById(Long benefitId) {
    return benefitService.findById(benefitId);
  }

  public Pageable<BenefitDTO> listBenefits(Long sponsorId, Boolean isActive, PageDTO pageDTO) {
    return benefitService.list(sponsorId, isActive, pageDTO);
  }

  public void deactivateBenefit(Long benefitId) {
    benefitService.deactivate(benefitId);
  }

  public void activateBenefit(Long benefitId) {
    benefitService.activate(benefitId);
  }

  public void deleteBenefit(Long benefitId) {
    benefitService.delete(benefitId);
  }

  public Pageable<AdminCheckinDTO> listCheckins(ListAdminCheckinsQueryDTO query) {
    return sponsorCheckinService.listCheckinsForAdmin(
        query.getSponsorId(),
        query.getMemberUserId(),
        query.getStartDate(),
        query.getEndDate(),
        query.getValidated(),
        query.toPageDTO());
  }
}
