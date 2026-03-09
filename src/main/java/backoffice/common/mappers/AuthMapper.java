package backoffice.common.mappers;

import backoffice.v1.dtos.auth.AuthExtDTO;
import backoffice.v1.entities.User;

public class AuthMapper {
  public static AuthExtDTO fromUserToDTO(User user) {

    var authExtBuild = AuthExtDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .avatarUrl(user.getAvatarUrl())
        .type(user.getType())
        .createdAt(user.getCreatedAt())
        .isAccountActive(user.isAccountActive())
        .build();

    return authExtBuild;
  }
}
