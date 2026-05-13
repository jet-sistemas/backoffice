package backoffice.v1.repositories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.entities.User;
import backoffice.v1.entities.enums.MemberTypeEnum;
import backoffice.v1.entities.enums.SponsorEntityTypeEnum;
import backoffice.v1.entities.enums.SponsorPersonaEnum;
import backoffice.v1.entities.enums.SponsorTierEnum;
import backoffice.v1.entities.enums.UserTypeEnum;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, Long> {

  private static String escapeLikePattern(String raw) {
    return raw
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
        .toLowerCase(Locale.ROOT);
  }

  private static String digitsOnly(String raw) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < raw.length(); i++) {
      char c = raw.charAt(i);
      if (c >= '0' && c <= '9') {
        sb.append(c);
      }
    }
    return sb.toString();
  }

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

  public Pageable<User> findAllPaginated(UserTypeEnum type, SponsorTierEnum tier,
      SponsorEntityTypeEnum entityType, SponsorPersonaEnum persona, MemberTypeEnum memberType,
      Boolean isActive, String search, PageDTO pageDTO) {
    var sb = new StringBuilder();
    Map<String, Object> params = new HashMap<>();
    List<String> conditions = new ArrayList<>();

    if (type != null) {
      conditions.add("u.type = :type");
      params.put("type", type);
    }

    boolean sponsorFilter = tier != null || entityType != null || persona != null;
    if (sponsorFilter) {
      var sub = new StringBuilder("exists (select 1 from Sponsor s where s.user = u");
      if (tier != null) {
        sub.append(" and s.tier = :tier");
        params.put("tier", tier);
      }
      if (entityType != null) {
        sub.append(" and s.entityType = :entityType");
        params.put("entityType", entityType);
      }
      if (persona != null) {
        sub.append(" and s.persona = :persona");
        params.put("persona", persona);
      }
      sub.append(")");
      conditions.add(sub.toString());
    }

    if (memberType != null) {
      conditions.add("exists (select 1 from Member mmf where mmf.user = u and mmf.type = :memberType)");
      params.put("memberType", memberType);
    }

    if (isActive != null) {
      conditions.add("u.isAccountActive = :isActive");
      params.put("isActive", isActive);
    }

    if (search != null && !search.isBlank()) {
      String term = search.trim();
      params.put("searchLike", "%" + escapeLikePattern(term) + "%");
      String likeClause = " like :searchLike escape '\\'";
      List<String> searchOrs = new ArrayList<>();
      searchOrs.add("lower(u.name)" + likeClause);
      searchOrs.add("lower(u.code)" + likeClause);
      searchOrs.add("lower(u.document)" + likeClause);
      searchOrs.add(
          "exists (select 1 from Sponsor s2 where s2.user = u and lower(s2.publicName)" + likeClause + ")");
      String digits = digitsOnly(term);
      if (!digits.isEmpty()) {
        params.put("docDigitsLike", "%" + digits + "%");
        searchOrs.add(
            "cast(function('regexp_replace', u.document, '[^0-9]', '', 'g') as string) like :docDigitsLike");
      }
      if (UserTypeEnum.MEMBER.equals(type)) {
        searchOrs.add(
            "exists (select 1 from Member mm2 where mm2.user = u and (lower(mm2.fullname)" + likeClause
                + " or lower(mm2.whatsapp)" + likeClause + "))");
      }
      conditions.add("(" + String.join(" or ", searchOrs) + ")");
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
