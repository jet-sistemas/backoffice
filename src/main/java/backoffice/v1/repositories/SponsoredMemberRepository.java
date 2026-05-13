package backoffice.v1.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import backoffice.v1.entities.SponsoredMember;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SponsoredMemberRepository
    implements PanacheRepositoryBase<SponsoredMember, SponsoredMember.SponsoredMemberId> {

  public Optional<SponsoredMember> findFirstByMemberId(Long memberId) {
    return find("member.id = ?1", memberId).firstResultOptional();
  }

  public List<SponsoredMember> findByMemberIdIn(Collection<Long> memberIds) {
    if (memberIds == null || memberIds.isEmpty()) {
      return List.of();
    }
    return find("member.id in ?1", memberIds).list();
  }

  public boolean existsByMemberId(Long memberId) {
    return count("member.id = ?1", memberId) > 0;
  }
}
