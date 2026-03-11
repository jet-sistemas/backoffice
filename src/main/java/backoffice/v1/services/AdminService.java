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
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.user.UserCreateDTO;
import backoffice.v1.dtos.user.UserWithSponsorCreateDTO;
import backoffice.v1.dtos.user.UserWithSponsorDTO;
import backoffice.v1.dtos.user.UserWithSponsorUpdateDTO;
import backoffice.v1.entities.Sponsor;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.SponsorTierEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import backoffice.v1.repositories.UserRepository;
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
  private UserRepository userRepository;

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

    return UserMapper.fromEntityToUserWithSponsorDTO(user, sponsor);
  }

  @Transactional
  public UserWithSponsorDTO updateUser(Long userId, UserWithSponsorUpdateDTO dto) {
    User user = userService.findById(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    userService.validateUniqueFieldsForUpdate(userId, dto.getEmail(), dto.getDocument());

    UserMapper.applyUpdate(dto, user);
    userRepository.persistAndFlush(user);

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
    userRepository.persistAndFlush(user);

    if (isSponsorType(user.getType())) {
      sponsorService.deactivateByUserId(userId);
    }
  }

  @Transactional
  public void deleteUser(Long userId) {
    User user = userService.findById(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    if (isSponsorType(user.getType())) {
      sponsorService.deleteByUserId(userId);
    }

    userRepository.delete(user);
  }

  public Pageable<UserWithSponsorDTO> listUsers(UserTypeEnum type, SponsorTierEnum tier, Boolean isActive, PageDTO pageDTO) {
    Pageable<User> pageable = userRepository.findAllPaginated(type, tier, isActive, pageDTO);

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
    if (type == UserTypeEnum.MEMBER || type == UserTypeEnum.SPONSOR_MEMBER) {
      throw new BusinessException(MessageErrorEnum.USER_TYPE_NOT_IMPLEMENTED.getMessage(), 400);
    }

    if (isSponsorType(type) && dto.getSponsor() == null) {
      throw new BadRequestException(MessageErrorEnum.SPONSOR_DATA_REQUIRED.getMessage());
    }
  }

  private boolean isSponsorType(UserTypeEnum type) {
    return UserTypeEnum.SPONSOR.equals(type) || UserTypeEnum.SPONSOR_MEMBER.equals(type);
  }
}
