package backoffice.common.mappers;

import backoffice.v1.dtos.user.UserCreateDTO;
import backoffice.v1.dtos.user.UserDTO;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.UserTypeEnum;

public class UserMapper {
  public static User fromDTO(UserCreateDTO dto) {
    return User.builder()
        .email(dto.getEmail())
        .password(dto.getPassword())
        .avatarUrl(dto.getAvatarUrl())
        .type(UserTypeEnum.valueOf(dto.getType().toUpperCase()))
        .document(dto.getDocument())
        .code(dto.getCode())
        .name(dto.getName())
        .build();
  }

  public static UserDTO fromEntityToDto(User user) {
    return UserDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .document(user.getDocument())
        .code(user.getCode())
        .isAccountActive(user.isAccountActive())
        .type(user.getType())
        .avatarUrl(user.getAvatarUrl())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
