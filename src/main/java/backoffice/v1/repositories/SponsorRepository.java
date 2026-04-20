package backoffice.v1.repositories;

import java.util.Optional;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.entities.Sponsor;
import backoffice.v1.entities.enums.SponsorTierEnum;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SponsorRepository implements PanacheRepositoryBase<Sponsor, Long> {

  public Optional<Sponsor> findByWhatsapp(String whatsapp) {
    return find("whatsapp = ?1", whatsapp).firstResultOptional();
  }

  public Optional<Sponsor> findByUserId(Long userId) {
    return find("user.id = ?1", userId).firstResultOptional();
  }

  public boolean existsByWhatsappAndIdNot(String whatsapp, Long id) {
    return count("whatsapp = ?1 and id != ?2", whatsapp, id) > 0;
  }

  public Pageable<Sponsor> findActiveSponsors(SponsorTierEnum tier, PageDTO pageDTO) {
    var query = tier != null
        ? find("tier = ?1 and isActive = true", tier)
        : find("isActive = true");

    var paginatedQuery = query.page(pageDTO.getPagination());

    return new Pageable<>(paginatedQuery, pageDTO.getOneBasePage());
  }
}
