package backoffice.v1.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import backoffice.v1.entities.SubscriberMember;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SubscriberMemberRepository implements PanacheRepositoryBase<SubscriberMember, Long> {

  public Optional<SubscriberMember> findByMemberId(Long memberId) {
    return find("member.id = ?1", memberId).firstResultOptional();
  }

  public List<SubscriberMember> findByMemberIdIn(Collection<Long> memberIds) {
    if (memberIds == null || memberIds.isEmpty()) {
      return List.of();
    }
    return find("member.id in ?1", memberIds).list();
  }
}
