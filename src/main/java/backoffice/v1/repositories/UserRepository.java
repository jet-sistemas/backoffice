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
}
