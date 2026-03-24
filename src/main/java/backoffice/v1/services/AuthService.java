package backoffice.v1.services;

import java.util.logging.Logger;

import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BadRequestException;
import backoffice.common.exceptions.customs.ForbiddenException;
import backoffice.common.exceptions.customs.NotFoundException;
import backoffice.common.mappers.AuthMapper;
import backoffice.common.utils.PasswordUtils;
import backoffice.common.utils.TokenUtils;
import backoffice.v1.dtos.auth.AuthCreateDTO;
import backoffice.v1.dtos.auth.AuthDTO;
import backoffice.v1.dtos.auth.AuthExtDTO;
import backoffice.v1.entities.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AuthService {
  @Inject
  private UserService userService;

  public AuthDTO signIn(AuthCreateDTO dto) {
    Logger.getLogger(AuthService.class.getName()).warning("Signing in user with email: " + dto.getEmail());
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

  public AuthExtDTO me(String email) {
    User user = userService.findByEmail(email)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    return AuthMapper.fromUserToDTO(user);
  }
}
