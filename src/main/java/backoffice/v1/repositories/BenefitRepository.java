package backoffice.v1.repositories;

import backoffice.v1.entities.Benefit;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BenefitRepository implements PanacheRepositoryBase<Benefit, Long> {

  public int deactivateBySponsorId(Long sponsorId) {
    return update("isActive = false where sponsor.id = ?1", sponsorId);
  }

  public long deleteBySponsorId(Long sponsorId) {
    return delete("sponsor.id = ?1", sponsorId);
  }
}
