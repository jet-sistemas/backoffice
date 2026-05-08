package backoffice.v1.repositories;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.entities.Member;
import backoffice.v1.entities.enums.MemberTypeEnum;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MemberRepository implements PanacheRepositoryBase<Member, Long> {
  private static String escapeLikePattern(String raw) {
    return raw
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
        .toLowerCase(Locale.ROOT);
  }

  public boolean existsByWhatsapp(String whatsapp) {
    return count("whatsapp = ?1", whatsapp) > 0;
  }

  public Optional<Member> findByUserId(Long userId) {
    return find("user.id = ?1", userId).firstResultOptional();
  }

  public Pageable<Member> findAllPaginated(MemberTypeEnum type, Boolean isActive, String search, PageDTO pageDTO) {
    var sb = new StringBuilder();
    Map<String, Object> params = new HashMap<>();

    sb.append("from Member m where 1=1");

    if (type != null) {
      sb.append(" and m.type = :type");
      params.put("type", type);
    }
    if (isActive != null) {
      sb.append(" and m.isActive = :isActive");
      params.put("isActive", isActive);
    }
    if (search != null && !search.isBlank()) {
      params.put("searchLike", "%" + escapeLikePattern(search.trim()) + "%");
      String likeClause = " like :searchLike escape '\\'";
      sb.append(" and (");
      sb.append("lower(m.fullname)").append(likeClause);
      sb.append(" or lower(m.whatsapp)").append(likeClause);
      sb.append(" or lower(m.user.email)").append(likeClause);
      sb.append(" or lower(m.user.code)").append(likeClause);
      sb.append(" or lower(m.user.document)").append(likeClause);
      sb.append(")");
    }

    sb.append(" order by m.createdAt desc");

    var query = find(sb.toString(), params);
    var paginatedQuery = query.page(pageDTO.getPagination());
    return new Pageable<>(paginatedQuery, pageDTO.getOneBasePage());
  }
}
