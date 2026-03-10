package backoffice.common.mappers;

import backoffice.v1.dtos.user.UserCreateDTO;
import backoffice.v1.dtos.user.UserDTO;
import backoffice.v1.dtos.user.UserMinDTO;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.UserTypeEnum;

public class UserMapper {

  public static User fromDTO(UserCreateDTO dto) {
    var builder = User.builder()
        .email(dto.getEmail())
        .password(dto.getPassword())
        .avatarUrl(dto.getAvatarUrl())
        .document(dto.getDocument())
        .code(dto.getCode())
        .name(dto.getName());

    if (dto.getType() != null) {
      builder.type(UserTypeEnum.valueOf(dto.getType().toUpperCase()));
    }

    return builder.build();
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

  public static UserMinDTO fromEntityToMinimal(User user) {
    return UserMinDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .type(user.getType())
        .createdAt(user.getCreatedAt())
        .build();
  }
}
