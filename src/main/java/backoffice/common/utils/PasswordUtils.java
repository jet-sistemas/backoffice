package backoffice.common.utils;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class PasswordUtils {

  public static String hashPass(String password) {
    return BCrypt.withDefaults().hashToString(12, password.toCharArray());
  };

  public static boolean checkPass(String password, String hashedPassword) {
    BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), hashedPassword);
    return result.verified;
  };
}
