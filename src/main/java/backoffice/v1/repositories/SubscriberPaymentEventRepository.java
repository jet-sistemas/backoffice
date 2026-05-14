package backoffice.v1.repositories;

import backoffice.common.database.Pageable;
import backoffice.v1.dtos.common.PageDTO;
import backoffice.v1.entities.SubscriberPaymentEvent;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SubscriberPaymentEventRepository implements PanacheRepositoryBase<SubscriberPaymentEvent, Long> {

  public Pageable<SubscriberPaymentEvent> findBySubscriberMemberId(Long subscriberMemberId, PageDTO pageDTO) {
    var query = find("subscriberMember.id = ?1 order by createdAt desc", subscriberMemberId)
        .page(pageDTO.getPagination());
    return new Pageable<>(query, pageDTO.getOneBasePage());
  }
}
