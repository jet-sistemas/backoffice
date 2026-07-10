package backoffice.v1.repositories;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.entities.SponsorMemberCheckin;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SponsorMemberCheckinRepository implements PanacheRepositoryBase<SponsorMemberCheckin, Long> {

  public Optional<SponsorMemberCheckin> findLatestTodayBySponsorAndMember(
      Long sponsorId, Long memberId, Instant dayStart, Instant dayEnd) {
    return find(
        "sponsor.id = ?1 and member.id = ?2 and createdAt >= ?3 and createdAt < ?4 order by createdAt desc",
        sponsorId, memberId, dayStart, dayEnd)
        .firstResultOptional();
  }

  public Pageable<SponsorMemberCheckin> listBySponsor(
      Long sponsorId, Instant startInclusive, Instant endExclusive, PageDTO pageDTO) {
    var conditions = new StringBuilder("sponsor.id = :sponsorId");
    Map<String, Object> params = new HashMap<>();
    params.put("sponsorId", sponsorId);
    appendDateRange(conditions, params, startInclusive, endExclusive);
    conditions.append(" order by createdAt desc");
    var query = find(conditions.toString(), params).page(pageDTO.getPagination());
    return new Pageable<>(query, pageDTO.getOneBasePage());
  }

  public Pageable<SponsorMemberCheckin> listByMember(
      Long memberId, Instant startInclusive, Instant endExclusive, PageDTO pageDTO) {
    var conditions = new StringBuilder("member.id = :memberId");
    Map<String, Object> params = new HashMap<>();
    params.put("memberId", memberId);
    appendDateRange(conditions, params, startInclusive, endExclusive);
    conditions.append(" order by createdAt desc");
    var query = find(conditions.toString(), params).page(pageDTO.getPagination());
    return new Pageable<>(query, pageDTO.getOneBasePage());
  }

  public Pageable<SponsorMemberCheckin> listAll(
      Instant startInclusive, Instant endExclusive, PageDTO pageDTO) {
    var conditions = new StringBuilder("1=1");
    Map<String, Object> params = new HashMap<>();
    appendDateRange(conditions, params, startInclusive, endExclusive);
    conditions.append(" order by createdAt desc");
    var query = find(conditions.toString(), params).page(pageDTO.getPagination());
    return new Pageable<>(query, pageDTO.getOneBasePage());
  }

  private static void appendDateRange(
      StringBuilder conditions, Map<String, Object> params, Instant startInclusive, Instant endExclusive) {
    if (startInclusive != null) {
      conditions.append(" and createdAt >= :startInclusive");
      params.put("startInclusive", startInclusive);
    }
    if (endExclusive != null) {
      conditions.append(" and createdAt < :endExclusive");
      params.put("endExclusive", endExclusive);
    }
  }
}
