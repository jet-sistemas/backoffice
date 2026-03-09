package backoffice.v1.repositories;

import java.util.Optional;

import backoffice.v1.entities.Sponsor;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SponsorRepository implements PanacheRepositoryBase<Sponsor, Long> {

  public Optional<Sponsor> findByWhatsapp(String whatsapp) {
    return find("whatsapp = ?1", whatsapp).firstResultOptional();
  }
}
