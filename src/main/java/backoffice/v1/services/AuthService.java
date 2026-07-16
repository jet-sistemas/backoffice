package backoffice.v1.services;

import java.time.Instant;

import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BadRequestException;
import backoffice.common.exceptions.customs.ForbiddenException;
import backoffice.common.exceptions.customs.NotFoundException;
import backoffice.common.mappers.AuthMapper;
import backoffice.common.utils.PasswordPolicyService;
import backoffice.common.utils.PasswordUtils;
import backoffice.common.utils.TokenUtils;
import backoffice.v1.dtos.auth.AuthCreateDTO;
import backoffice.v1.dtos.auth.AuthDTO;
import backoffice.v1.dtos.auth.AuthExtDTO;
import backoffice.v1.dtos.auth.ChangePasswordRequestDTO;
import backoffice.v1.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AuthService {
  @Inject
  private UserService userService;

  public AuthDTO signIn(AuthCreateDTO dto) {
    User user = userService.findByEmail(dto.getEmail())
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    if (!user.isAccountActive()) {
      throw new ForbiddenException(MessageErrorEnum.ACCOUNT_INVALID_TO_ACTION.getMessage());
    }

    if (!PasswordUtils.checkPass(dto.getPassword(), user.getPassword())) {
      throw new BadRequestException(MessageErrorEnum.USER_PASS_NOT_MATCH.getMessage());
    }

    String token = TokenUtils.generateToken(user);

    return AuthDTO.builder().accessToken(token).build();
  }

  @Transactional
  public AuthExtDTO changePassword(String email, ChangePasswordRequestDTO dto) {
    User user = userService.findByEmail(email)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    if (!PasswordUtils.checkPass(dto.getCurrentPassword(), user.getPassword())) {
      throw new BadRequestException(MessageErrorEnum.PASSWORD_CURRENT_INVALID.getMessage());
    }

    PasswordPolicyService.validateDefinitivePassword(dto.getNewPassword(), dto.getConfirmPassword());

    if (PasswordUtils.checkPass(dto.getNewPassword(), user.getPassword())) {
      throw new BadRequestException(MessageErrorEnum.PASSWORD_SAME_AS_CURRENT.getMessage());
    }

    user.setPassword(PasswordUtils.hashPass(dto.getNewPassword()));
    user.setMustChangePassword(false);
    user.setPasswordChangedAt(Instant.now());
    userService.persistAndFlush(user);

    return AuthMapper.fromUserToDTO(user);
  }

  public AuthExtDTO me(String email) {
    User user = userService.findByEmail(email)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    return AuthMapper.fromUserToDTO(user);
  }
}
