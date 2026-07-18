package backoffice.common.security;

import java.util.Set;

import org.eclipse.microprofile.jwt.JsonWebToken;

import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.ForbiddenException;
import backoffice.v1.entities.User;
import backoffice.v1.services.UserService;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHORIZATION + 10)
public class MustChangePasswordFilter implements ContainerRequestFilter {

  private static final Set<String> ALLOWED_SUFFIXES = Set.of(
      "/v1/auth/me",
      "/v1/auth/change-password");

  @Inject
  SecurityIdentity identity;

  @Inject
  UserService userService;

  @Context
  UriInfo uriInfo;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (identity == null || identity.isAnonymous()) {
      return;
    }

    String path = uriInfo.getPath();
    if (path == null) {
      return;
    }

    String normalized = path.startsWith("/") ? path : "/" + path;
    if (ALLOWED_SUFFIXES.stream().anyMatch(normalized::endsWith)) {
      return;
    }

    if (identity.getPrincipal() instanceof JsonWebToken jwt) {
      Object idClaim = jwt.getClaim("id");
      if (idClaim == null) {
        return;
      }
      long userId;
      if (idClaim instanceof Number n) {
        userId = n.longValue();
      } else {
        try {
          userId = Long.parseLong(idClaim.toString());
        } catch (NumberFormatException e) {
          return;
        }
      }

      User user = userService.findById(userId).orElse(null);
      if (user != null && user.isMustChangePassword()) {
        throw new ForbiddenException(MessageErrorEnum.PASSWORD_CHANGE_REQUIRED.getMessage());
      }
    }
  }
}
