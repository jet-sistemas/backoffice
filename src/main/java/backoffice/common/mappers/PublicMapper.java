package backoffice.common.mappers;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.publics.PublicSponsorDataDTO;
import backoffice.v1.dtos.publics.PublicSponsorItemDTO;
import backoffice.v1.dtos.publics.PublicSponsorUserDTO;
import backoffice.v1.entities.Sponsor;
import backoffice.v1.entities.User;

public class PublicMapper {

  public static PublicSponsorItemDTO fromUserAndSponsor(User user, Sponsor sponsor) {
    if (sponsor == null || !sponsor.isActive()) {
      return null;
    }

    PublicSponsorDataDTO sponsorData = PublicSponsorDataDTO.builder()
        .id(sponsor.getId())
        .publicName(sponsor.getPublicName())
        .tier(sponsor.getTier())
        .logoUrl(sponsor.getLogoUrl())
        .site(sponsor.getSite())
        .instagram(sponsor.getInstagram())
        .whatsapp(sponsor.getWhatsapp())
        .build();

    PublicSponsorUserDTO userData = PublicSponsorUserDTO.builder()
        .id(user.getId())
        .name(user.getName())
        .type(user.getType())
        .createdAt(user.getCreatedAt())
        .sponsor(sponsorData)
        .build();

    return PublicSponsorItemDTO.builder()
        .user(userData)
        .build();
  }

  public static Pageable<PublicSponsorItemDTO> fromUsersPageableToPublicItems(
      Pageable<User> users,
      Map<Long, Sponsor> sponsorsByUserId) {
    List<PublicSponsorItemDTO> items = users.getData().stream()
        .map(u -> fromUserAndSponsor(u, sponsorsByUserId.get(u.getId())))
        .filter(Objects::nonNull)
        .toList();

    return Pageable.<PublicSponsorItemDTO>builder()
        .data(items)
        .totalElements(users.getTotalElements())
        .totalPages(users.getTotalPages())
        .pageSize(users.getPageSize())
        .currentPage(users.getCurrentPage())
        .build();
  }
}
