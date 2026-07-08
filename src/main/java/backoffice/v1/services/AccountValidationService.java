package backoffice.v1.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BadRequestException;
import backoffice.common.exceptions.customs.BusinessException;
import backoffice.common.exceptions.customs.ForbiddenException;
import backoffice.common.exceptions.customs.NotFoundException;
import backoffice.common.services.mail.AccountValidationMailPayload;
import backoffice.common.services.mail.EmailService;
import backoffice.common.utils.DocumentUtils;
import backoffice.common.utils.PasswordPolicyService;
import backoffice.common.utils.PasswordUtils;
import backoffice.v1.dtos.accountvalidation.AccountValidationResultDTO;
import backoffice.v1.dtos.accountvalidation.ResendAccountValidationDTO;
import backoffice.v1.entities.AccountValidationCode;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.AccountValidationStatusEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import backoffice.v1.repositories.AccountValidationCodeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AccountValidationService {

  private static final int MAX_ATTEMPTS = 5;
  private static final String CODE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  @Inject
  private AccountValidationCodeRepository accountValidationCodeRepository;

  @Inject
  private UserService userService;

  @Inject
  private EmailService emailService;

  @Inject
  @ConfigProperty(name = "backoffice.account-validation.base-url")
  String validationBaseUrl;

  @Inject
  @ConfigProperty(name = "backoffice.account-validation.expiration-days", defaultValue = "7")
  int expirationDays;

  public record InviteCredentials(String plainCode, String plainToken, String temporaryPassword) {
  }

  @Transactional
  public InviteCredentials createInviteAndSendEmail(User user, User adminActor, String temporaryPassword) {
    InviteCredentials credentials = createInvite(user, adminActor, temporaryPassword);
    sendInviteEmail(user, credentials);
    return credentials;
  }

  @Transactional
  public InviteCredentials createInvite(User user, User adminActor, String temporaryPassword) {
    accountValidationCodeRepository.deactivateAllActiveByUserId(user.getId());

    String plainCode = PasswordPolicyService.generateValidationCode();
    String plainToken = PasswordPolicyService.generateLinkToken();
    Instant now = Instant.now();
    Instant expiresAt = now.plus(expirationDays, ChronoUnit.DAYS);

    AccountValidationCode invite = AccountValidationCode.builder()
        .user(user)
        .codeHash(PasswordUtils.hashPass(plainCode.toUpperCase()))
        .linkTokenHash(PasswordUtils.hashPass(plainToken))
        .isActive(true)
        .expiresAt(expiresAt)
        .sentAt(now)
        .createdByAdmin(adminActor)
        .build();

    accountValidationCodeRepository.persistAndFlush(invite);
    return new InviteCredentials(plainCode, plainToken, temporaryPassword);
  }

  public void sendInviteEmail(User user, InviteCredentials credentials) {
    Instant expiresAt = Instant.now().plus(expirationDays, ChronoUnit.DAYS);
    String validationUrl = buildValidationUrl(credentials.plainToken());
    AccountValidationMailPayload payload = new AccountValidationMailPayload(
        user.getEmail(),
        user.getName(),
        validationUrl,
        credentials.plainCode(),
        credentials.temporaryPassword(),
        expiresAt);

    try {
      emailService.sendAccountValidation(payload);
    } catch (RuntimeException e) {
      throw new BusinessException(MessageErrorEnum.EMAIL_SEND_FAILED.getMessage(), 500);
    }
  }

  @Transactional
  public AccountValidationResultDTO confirmAccount(String token, String code, String document) {
    if (token == null || token.isBlank()) {
      throw new BadRequestException(MessageErrorEnum.ACCOUNT_VALIDATION_INVITE_NOT_FOUND.getMessage());
    }

    String normalizedCode = normalizeCode(code);
    AccountValidationCode invite = findInviteByToken(token)
        .orElseThrow(() -> new BadRequestException(MessageErrorEnum.ACCOUNT_VALIDATION_INVITE_NOT_FOUND.getMessage()));

    User user = invite.getUser();

    if (user.getEmailVerifiedAt() != null) {
      throw new BadRequestException(MessageErrorEnum.ACCOUNT_ALREADY_VALIDATED.getMessage());
    }

    if (invite.getUsedAt() != null || !invite.isActive()) {
      throw new BadRequestException(MessageErrorEnum.ACCOUNT_VALIDATION_INVITE_ALREADY_USED.getMessage());
    }

    if (invite.getLockedAt() != null) {
      throw new ForbiddenException(MessageErrorEnum.ACCOUNT_VALIDATION_INVITE_LOCKED.getMessage());
    }

    if (invite.getExpiresAt().isBefore(Instant.now())) {
      throw new BadRequestException(MessageErrorEnum.ACCOUNT_VALIDATION_CODE_EXPIRED.getMessage());
    }

    if (!DocumentUtils.matches(user.getDocument(), document)) {
      incrementAttempt(invite);
      throw new BadRequestException(MessageErrorEnum.ACCOUNT_VALIDATION_DOCUMENT_MISMATCH.getMessage());
    }

    if (!PasswordUtils.checkPass(normalizedCode, invite.getCodeHash())) {
      incrementAttempt(invite);
      throw new BadRequestException(MessageErrorEnum.ACCOUNT_VALIDATION_CODE_INVALID.getMessage());
    }

    Instant now = Instant.now();
    invite.setUsedAt(now);
    invite.setActive(false);
    accountValidationCodeRepository.persistAndFlush(invite);

    user.setAccountActive(true);
    user.setEmailVerifiedAt(now);
    user.setMustChangePassword(true);
    userService.persistAndFlush(user);

    return AccountValidationResultDTO.builder()
        .email(user.getEmail())
        .accountActive(true)
        .mustChangePassword(true)
        .build();
  }

  @Transactional
  public ResendAccountValidationDTO resendExpiredInvite(Long userId, Long adminActorId) {
    User user = userService.findById(userId)
        .orElseThrow(() -> new NotFoundException(MessageErrorEnum.USER_NOT_FOUND.getMessage()));

    if (user.getType() == UserTypeEnum.ADM) {
      throw new BadRequestException(MessageErrorEnum.ACCOUNT_ALREADY_VALIDATED.getMessage());
    }

    if (user.getEmailVerifiedAt() != null) {
      throw new BadRequestException(MessageErrorEnum.ACCOUNT_ALREADY_VALIDATED.getMessage());
    }

    AccountValidationStatusEnum status = resolveStatus(user);
    if (status != AccountValidationStatusEnum.INVITE_EXPIRED) {
      throw new BadRequestException(MessageErrorEnum.ACCOUNT_VALIDATION_RESEND_NOT_EXPIRED.getMessage());
    }

    User adminActor = adminActorId != null
        ? userService.findById(adminActorId).orElse(null)
        : null;

    String temporaryPassword = PasswordPolicyService.generateTemporaryPassword();
    user.setPassword(PasswordUtils.hashPass(temporaryPassword));
    userService.persistAndFlush(user);

    InviteCredentials credentials = createInvite(user, adminActor, temporaryPassword);
    credentials = new InviteCredentials(credentials.plainCode(), credentials.plainToken(), temporaryPassword);
    sendInviteEmail(user, credentials);

    AccountValidationCode activeInvite = accountValidationCodeRepository.findActiveByUserId(user.getId())
        .orElseThrow();
    activeInvite.setResentAt(Instant.now());
    accountValidationCodeRepository.persistAndFlush(activeInvite);

    return ResendAccountValidationDTO.builder()
        .userId(userId)
        .sent(true)
        .accountValidationStatus(AccountValidationStatusEnum.PENDING)
        .build();
  }

  public AccountValidationStatusEnum resolveStatus(User user) {
    if (user.getType() == UserTypeEnum.ADM || user.getEmailVerifiedAt() != null) {
      return AccountValidationStatusEnum.VALIDATED;
    }

    Instant now = Instant.now();
    Optional<AccountValidationCode> activeInvite = accountValidationCodeRepository.findActiveByUserId(user.getId());

    if (activeInvite.isEmpty()) {
      return AccountValidationStatusEnum.INVITE_EXPIRED;
    }

    AccountValidationCode invite = activeInvite.get();
    if (invite.getExpiresAt().isBefore(now) || invite.getLockedAt() != null) {
      return AccountValidationStatusEnum.INVITE_EXPIRED;
    }

    return AccountValidationStatusEnum.PENDING;
  }

  public boolean canResendInvite(User user) {
    return resolveStatus(user) == AccountValidationStatusEnum.INVITE_EXPIRED;
  }

  private Optional<AccountValidationCode> findInviteByToken(String token) {
    List<AccountValidationCode> activeInvites = accountValidationCodeRepository
        .find("isActive = true and usedAt is null")
        .list();

    return activeInvites.stream()
        .filter(invite -> PasswordUtils.checkPass(token, invite.getLinkTokenHash()))
        .findFirst();
  }

  private void incrementAttempt(AccountValidationCode invite) {
    invite.setAttemptCount(invite.getAttemptCount() + 1);
    if (invite.getAttemptCount() >= MAX_ATTEMPTS) {
      invite.setLockedAt(Instant.now());
      invite.setActive(false);
    }
    accountValidationCodeRepository.persistAndFlush(invite);
  }

  private String buildValidationUrl(String token) {
    String base = validationBaseUrl == null ? "" : validationBaseUrl.trim();
    if (base.endsWith("/")) {
      base = base.substring(0, base.length() - 1);
    }
    return base + "/" + token;
  }

  private static String normalizeCode(String code) {
    if (code == null) {
      return "";
    }
    return code.trim().toUpperCase().replaceAll("[^" + CODE_ALPHABET + "]", "");
  }
}
