package backoffice.v1.repositories;

import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import backoffice.v1.entities.User;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, Long> {
  public Optional<User> findByEmail(String email) {
    return find("email = ?1", email).firstResultOptional();
  }
}
