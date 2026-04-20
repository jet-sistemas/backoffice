package backoffice.common.mappers;

import java.util.List;
import java.util.Map;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.user.UserCreateDTO;
import backoffice.v1.dtos.user.UserDTO;
import backoffice.v1.dtos.user.UserMinDTO;
import backoffice.v1.dtos.user.UserWithSponsorDTO;
import backoffice.v1.dtos.user.UserWithSponsorUpdateDTO;
import backoffice.v1.entities.Sponsor;
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

  public static UserWithSponsorDTO fromEntityToUserWithSponsorDTO(User user, Sponsor sponsor) {
    var builder = UserWithSponsorDTO.builder()
        .id(user.getId())
        .email(user.getEmail())
        .name(user.getName())
        .document(user.getDocument())
        .code(user.getCode())
        .isAccountActive(user.isAccountActive())
        .type(user.getType())
        .avatarUrl(user.getAvatarUrl())
        .createdAt(user.getCreatedAt());

    if (sponsor != null) {
      builder.sponsor(SponsorMapper.fromEntityToSponsorDTOWithoutUser(sponsor));
    }

    return builder.build();
  }

  public static void applyUpdate(UserWithSponsorUpdateDTO dto, User user) {
    if (dto.getEmail() != null) user.setEmail(dto.getEmail());
    if (dto.getName() != null) user.setName(dto.getName());
    if (dto.getDocument() != null) user.setDocument(dto.getDocument());
    if (dto.getAvatarUrl() != null) user.setAvatarUrl(dto.getAvatarUrl());
  }

  public static Pageable<UserWithSponsorDTO> fromEntityToPageableDTO(
      Pageable<User> data, Map<Long, Sponsor> sponsorsByUserId) {
    List<UserWithSponsorDTO> dtos = data.getData().stream()
        .map(user -> fromEntityToUserWithSponsorDTO(user, sponsorsByUserId.get(user.getId())))
        .toList();

    return Pageable.<UserWithSponsorDTO>builder()
        .data(dtos)
        .totalElements(data.getTotalElements())
        .totalPages(data.getTotalPages())
        .pageSize(data.getPageSize())
        .currentPage(data.getCurrentPage())
        .build();
  }
}
