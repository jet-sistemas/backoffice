package backoffice.v1.repositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.SponsorTierEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, Long> {

  public Optional<User> findByEmail(String email) {
    return find("email = ?1", email).firstResultOptional();
  }

  public boolean existsByEmail(String email) {
    return count("email = ?1", email) > 0;
  }

  public boolean existsByDocument(String document) {
    return count("document = ?1", document) > 0;
  }

  public boolean existsByCode(String code) {
    return count("code = ?1", code) > 0;
  }

  public boolean existsByEmailAndIdNot(String email, Long id) {
    return count("email = ?1 and id != ?2", email, id) > 0;
  }

  public boolean existsByDocumentAndIdNot(String document, Long id) {
    return count("document = ?1 and id != ?2", document, id) > 0;
  }

  public Pageable<User> findAllPaginated(UserTypeEnum type, SponsorTierEnum tier, PageDTO pageDTO) {
    var sb = new StringBuilder();
    Map<String, Object> params = new HashMap<>();
    List<String> conditions = new ArrayList<>();

    if (type != null) {
      conditions.add("u.type = :type");
      params.put("type", type);
    }

    if (tier != null) {
      conditions.add("exists (select 1 from Sponsor s where s.user = u and s.tier = :tier)");
      params.put("tier", tier);
    }

    if (conditions.isEmpty()) {
      sb.append("order by u.createdAt desc");
    } else {
      sb.append(String.join(" and ", conditions));
      sb.append(" order by u.createdAt desc");
    }

    var query = find("from User u where " + (conditions.isEmpty() ? "1=1 " : "") + sb.toString(), params);
    var paginatedQuery = query.page(pageDTO.getPagination());

    return new Pageable<>(paginatedQuery, pageDTO.getOneBasePage());
  }
}
