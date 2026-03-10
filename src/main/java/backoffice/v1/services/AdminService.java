package backoffice.v1.services;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.dtos.sponsor.SponsorCreateDTO;
import backoffice.v1.dtos.sponsor.SponsorDTO;
import backoffice.v1.dtos.sponsor.SponsorUpdateDTO;
import backoffice.v1.entities.enums.SponsorTierEnum;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AdminService {
  @Inject
  private SponsorService sponsorService;

  public SponsorDTO createSponsor(SponsorCreateDTO dto) {
    return sponsorService.createSponsor(dto);
  }

  public SponsorDTO updateSponsor(Long userId, SponsorUpdateDTO dto) {
    return sponsorService.updateSponsor(userId, dto);
  }

  public Pageable<SponsorDTO> listSponsors(SponsorTierEnum tier, PageDTO pageDTO) {
    return sponsorService.listSponsors(tier, pageDTO);
  }
}
