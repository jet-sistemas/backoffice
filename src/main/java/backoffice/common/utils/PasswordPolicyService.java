package backoffice.common.utils;

import java.security.SecureRandom;
import java.util.Base64;

import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BadRequestException;

public final class PasswordPolicyService {

  private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
  private static final String DIGITS = "0123456789";
  private static final String SPECIAL = "!@#$%&*-_+=?";
  private static final String TEMP_ALPHABET = UPPER + LOWER + DIGITS + SPECIAL;
  private static final int TEMP_PASSWORD_LENGTH = 12;
  private static final int MIN_DEFINITIVE_LENGTH = 8;

  // Lazy: SecureRandom no static <clinit> quebra GraalVM native (seed no image heap)
  private static volatile SecureRandom secureRandom;

  private PasswordPolicyService() {
  }

  private static SecureRandom secureRandom() {
    SecureRandom random = secureRandom;
    if (random == null) {
      random = new SecureRandom();
      secureRandom = random;
    }
    return random;
  }

  public static String generateTemporaryPassword() {
    char[] password = new char[TEMP_PASSWORD_LENGTH];
    password[0] = pick(UPPER);
    password[1] = pick(LOWER);
    password[2] = pick(DIGITS);
    password[3] = pick(SPECIAL);
    for (int i = 4; i < TEMP_PASSWORD_LENGTH; i++) {
      password[i] = pick(TEMP_ALPHABET);
    }
    shuffle(password);
    return new String(password);
  }

  public static String generateValidationCode() {
    final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    char[] code = new char[5];
    for (int i = 0; i < 5; i++) {
      code[i] = alphabet.charAt(secureRandom().nextInt(alphabet.length()));
    }
    return new String(code);
  }

  public static String generateLinkToken() {
    byte[] bytes = new byte[32];
    secureRandom().nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public static void validateDefinitivePassword(String newPassword, String confirmPassword) {
    if (newPassword == null || confirmPassword == null) {
      throw new BadRequestException(MessageErrorEnum.PASSWORD_POLICY_NOT_MET.getMessage());
    }
    if (!newPassword.equals(confirmPassword)) {
      throw new BadRequestException(MessageErrorEnum.PASSWORD_CONFIRMATION_MISMATCH.getMessage());
    }
    if (newPassword.length() < MIN_DEFINITIVE_LENGTH) {
      throw new BadRequestException(MessageErrorEnum.PASSWORD_POLICY_NOT_MET.getMessage());
    }
    if (!newPassword.matches(".*[A-Z].*")) {
      throw new BadRequestException(MessageErrorEnum.PASSWORD_POLICY_NOT_MET.getMessage());
    }
    if (!newPassword.matches(".*[a-z].*")) {
      throw new BadRequestException(MessageErrorEnum.PASSWORD_POLICY_NOT_MET.getMessage());
    }
    if (!newPassword.matches(".*\\d.*")) {
      throw new BadRequestException(MessageErrorEnum.PASSWORD_POLICY_NOT_MET.getMessage());
    }
    if (!newPassword.matches(".*[!@#$%&*\\-_+=?].*")) {
      throw new BadRequestException(MessageErrorEnum.PASSWORD_POLICY_NOT_MET.getMessage());
    }
  }

  private static char pick(String alphabet) {
    return alphabet.charAt(secureRandom().nextInt(alphabet.length()));
  }

  private static void shuffle(char[] array) {
    SecureRandom random = secureRandom();
    for (int i = array.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      char tmp = array[i];
      array[i] = array[j];
      array[j] = tmp;
    }
  }
}
