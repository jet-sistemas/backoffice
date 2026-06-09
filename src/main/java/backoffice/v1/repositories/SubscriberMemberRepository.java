package backoffice.v1.repositories;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.entities.SubscriberMember;
import backoffice.v1.entities.enums.MemberStatusEnum;
import backoffice.v1.entities.enums.MemberTypeEnum;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SubscriberMemberRepository implements PanacheRepositoryBase<SubscriberMember, Long> {

  private static String escapeLikePattern(String raw) {
    return raw
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")
        .toLowerCase(Locale.ROOT);
  }

  public Optional<SubscriberMember> findByMemberId(Long memberId) {
    return find("member.id = ?1", memberId).firstResultOptional();
  }

  public Optional<SubscriberMember> findByMemberUserId(Long userId) {
    return find("member.user.id = ?1", userId).firstResultOptional();
  }

  public List<SubscriberMember> findByMemberIdIn(Collection<Long> memberIds) {
    if (memberIds == null || memberIds.isEmpty()) {
      return List.of();
    }
    return find("member.id in ?1", memberIds).list();
  }

  public List<SubscriberMember> listNeedingAutomationStatus(LocalDate today, LocalDate dueSoonWindowEnd,
      MemberStatusEnum expectedStatus) {
    return listNeedingAutomationStatusBatch(today, dueSoonWindowEnd, expectedStatus, 0L, Integer.MAX_VALUE);
  }

  public List<SubscriberMember> listNeedingAutomationStatusBatch(
      LocalDate today,
      LocalDate dueSoonWindowEnd,
      MemberStatusEnum expectedStatus,
      long afterId,
      int limit) {
    return switch (expectedStatus) {
      case OVERDUE -> find(
          "nextDueDate < ?1 and status <> ?2 and status <> ?3 and id > ?4 order by id asc",
          today, MemberStatusEnum.OVERDUE, MemberStatusEnum.INACTIVE, afterId)
          .page(0, limit).list();
      case DUE_SOON -> find(
          "nextDueDate >= ?1 and nextDueDate <= ?2 and status <> ?3 and status <> ?4 and id > ?5 order by id asc",
          today, dueSoonWindowEnd, MemberStatusEnum.DUE_SOON, MemberStatusEnum.INACTIVE, afterId)
          .page(0, limit).list();
      case ACTIVE -> find(
          "nextDueDate > ?1 and status <> ?2 and status <> ?3 and id > ?4 order by id asc",
          dueSoonWindowEnd, MemberStatusEnum.ACTIVE, MemberStatusEnum.INACTIVE, afterId)
          .page(0, limit).list();
      default -> List.of();
    };
  }

  public long countWithStatusEnum(MemberStatusEnum status) {
    return count("status = ?1", status);
  }

  public Pageable<SubscriberMember> findSubscriberBillingPage(
      MemberStatusEnum statusFilter,
      LocalDate dueFrom,
      LocalDate dueTo,
      String search,
      PageDTO pageDTO) {
    var sb = new StringBuilder();
    Map<String, Object> params = new HashMap<>();
    sb.append("select sm from SubscriberMember sm join sm.member m join m.user u ");
    sb.append("where m.type = :subscriberType");
    params.put("subscriberType", MemberTypeEnum.SUBSCRIBER);

    if (statusFilter != null) {
      sb.append(" and sm.status = :subStatus");
      params.put("subStatus", statusFilter);
    }
    if (dueFrom != null) {
      sb.append(" and sm.nextDueDate >= :dueFrom");
      params.put("dueFrom", dueFrom);
    }
    if (dueTo != null) {
      sb.append(" and sm.nextDueDate <= :dueTo");
      params.put("dueTo", dueTo);
    }
    if (search != null && !search.isBlank()) {
      params.put("searchLike", "%" + escapeLikePattern(search.trim()) + "%");
      String likeClause = " like :searchLike escape '\\'";
      sb.append(" and (");
      sb.append("lower(m.fullname)").append(likeClause);
      sb.append(" or lower(m.whatsapp)").append(likeClause);
      sb.append(" or lower(u.email)").append(likeClause);
      sb.append(" or lower(u.code)").append(likeClause);
      sb.append(" or lower(u.document)").append(likeClause);
      sb.append(")");
    }

    sb.append(" order by sm.nextDueDate asc");

    var query = find(sb.toString(), params).page(pageDTO.getPagination());
    return new Pageable<>(query, pageDTO.getOneBasePage());
  }
}
