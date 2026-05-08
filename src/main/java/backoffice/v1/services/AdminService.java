package backoffice.v1.services;

import java.util.List;
import java.util.Map;

import backoffice.common.database.Pageable;
import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BadRequestException;
import backoffice.common.exceptions.customs.BusinessException;
import backoffice.common.exceptions.customs.NotFoundException;
import backoffice.common.mappers.UserMapper;
import backoffice.common.utils.PasswordUtils;
import backoffice.v1.dtos.benefit.BenefitCreateDTO;
import backoffice.v1.dtos.benefit.BenefitDTO;
import backoffice.v1.dtos.benefit.BenefitUpdateDTO;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.member.MemberCreateDTO;
import backoffice.v1.dtos.member.MemberDTO;
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

  @Transactional
  public UserWithSponsorDTO createUser(UserWithSponsorCreateDTO dto) {
    UserCreateDTO userData = dto.getUser();
    UserTypeEnum type = resolveUserType(userData.getType());

    validateTypeRequirements(type, dto);

    userService.validateUniqueFields(userData.getEmail(), userData.getDocument(), userData.getCode());

    userData.setPassword(PasswordUtils.hashPass("temp@1234"));
    userData.setType(type.name());

    User user = userService.create(userData);

    Sponsor sponsor = null;
    if (isSponsorType(type)) {
      sponsor = sponsorService.create(dto.getSponsor(), user);
    }
    if (UserTypeEnum.MEMBER.equals(type)) {
      memberService.create(dto.getMember(), user);
    }

    return UserMapper.fromEntityToUserWithSponsorDTO(user, sponsor);
  }

  @Transactional
  public MemberDTO createMember(MemberCreateDTO dto) {
    UserCreateDTO userData = dto.getUser();
    userService.validateUniqueFields(userData.getEmail(), userData.getDocument(), userData.getCode());

    userData.setPassword(PasswordUtils.hashPass("temp@1234"));
    userData.setType(UserTypeEnum.MEMBER.name());

    User user = userService.create(userData);
    return memberService.create(dto.getMember(), user);
  }

  public MemberDTO findMemberById(Long memberId) {
    return memberService.findDTOById(memberId);
  }

  public Pageable<MemberDTO> listMembers(MemberTypeEnum type, Boolean isActive, String search,
      PageDTO pageDTO) {
    return memberService.list(type, isActive, search, pageDTO);
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

    return UserMapper.fromEntityToUserWithSponsorDTO(user, sponsor);
  }

  @Transactional
  public void deactivateUser(Long userId) {
    User user = userService.findById(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    user.setAccountActive(false);
    userService.persistAndFlush(user);

    if (isSponsorType(user.getType())) {
      sponsorService.deactivateByUserId(userId);
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
    }
  }

  @Transactional
  public void deleteUser(Long userId) {
    User user = userService.findById(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    if (isSponsorType(user.getType())) {
      sponsorService.deleteByUserId(userId);
    }

    userService.delete(user);
  }

  public UserWithSponsorDTO findUserById(Long userId) {
    return userService.findById(userId)
        .map(user -> {
          Sponsor sponsor = isSponsorType(user.getType())
              ? sponsorService.findByUserId(userId).orElse(null)
              : null;
          return UserMapper.fromEntityToUserWithSponsorDTO(user, sponsor);
        })
        .orElse(null);
  }

  public Pageable<UserWithSponsorDTO> listUsers(UserTypeEnum type, SponsorTierEnum tier,
      SponsorEntityTypeEnum entityType, SponsorPersonaEnum persona, Boolean isActive, String search,
      PageDTO pageDTO) {
    SponsorPersonaEnum effectivePersona =
        entityType == SponsorEntityTypeEnum.PERSON ? persona : null;
    Pageable<User> pageable =
        userService.listUsers(type, tier, entityType, effectivePersona, isActive, search, pageDTO);

    List<Long> userIds = pageable.getData().stream()
        .filter(u -> isSponsorType(u.getType()))
        .map(User::getId)
        .toList();

    Map<Long, Sponsor> sponsorsByUserId = sponsorService.findByUserIds(userIds);

    return UserMapper.fromEntityToPageableDTO(pageable, sponsorsByUserId);
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
      throw new BadRequestException("Dados do membro são obrigatórios para usuários do tipo MEMBER.");
    }
  }

  private boolean isSponsorType(UserTypeEnum type) {
    return UserTypeEnum.SPONSOR.equals(type) || UserTypeEnum.SPONSOR_MEMBER.equals(type);
  }

  // --- Benefit ---

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
}
