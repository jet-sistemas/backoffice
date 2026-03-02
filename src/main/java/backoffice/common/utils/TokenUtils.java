package backoffice.common.utils;

import java.time.Duration;

import io.smallrye.jwt.build.Jwt;
import backoffice.v1.entities.User;

public class TokenUtils {
  static public String generateToken(User user) {
    String token = Jwt.issuer("sign-in")
        .upn(user.getEmail())
        .groups(user.getType().toString())
        .claim("id", user.getId())
        .expiresIn(Duration.ofDays(2))
        .sign();

    return token;
  }
}
