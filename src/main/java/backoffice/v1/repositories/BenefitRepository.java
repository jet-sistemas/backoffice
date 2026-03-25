package backoffice.v1.repositories;

import java.util.HashMap;
import java.util.Map;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.common.PageDTO;
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

  public Pageable<Benefit> findAllPaginated(Long sponsorId, Boolean isActive, PageDTO pageDTO) {
    var conditions = new StringBuilder("1=1");
    Map<String, Object> params = new HashMap<>();

    if (sponsorId != null) {
      conditions.append(" and sponsor.id = :sponsorId");
      params.put("sponsorId", sponsorId);
    }

    if (isActive != null) {
      conditions.append(" and isActive = :isActive");
      params.put("isActive", isActive);
    }

    var query = find(conditions.toString(), params).page(pageDTO.getPagination());
    return new Pageable<>(query, pageDTO.getOneBasePage());
  }
}
