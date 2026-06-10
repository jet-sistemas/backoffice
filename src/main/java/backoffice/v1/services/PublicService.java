package backoffice.v1.services;

import java.util.List;
import java.util.Map;

import backoffice.common.database.Pageable;
import backoffice.common.mappers.PublicMapper;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.publics.PublicSponsorItemDTO;
import backoffice.v1.entities.Sponsor;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.SponsorTierEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PublicService {
  @Inject
  private UserService userService;

  @Inject
  private SponsorService sponsorService;

  public Pageable<PublicSponsorItemDTO> listActiveSponsors(SponsorTierEnum tier, PageDTO pageDTO) {
    Pageable<User> pageableUsers =
        userService.listUsers(UserTypeEnum.SPONSOR, tier, null, null, null, Boolean.TRUE, null, pageDTO);

    List<Long> userIds = pageableUsers.getData().stream()
        .map(User::getId)
        .toList();

    Map<Long, Sponsor> sponsorsByUserId = sponsorService.findByUserIds(userIds);

    return PublicMapper.fromUsersPageableToPublicItems(pageableUsers, sponsorsByUserId);
  }
}
