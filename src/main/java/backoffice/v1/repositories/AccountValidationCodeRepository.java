package backoffice.v1.repositories;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import backoffice.v1.entities.AccountValidationCode;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AccountValidationCodeRepository implements PanacheRepository<AccountValidationCode> {

  public Optional<AccountValidationCode> findActiveByUserId(Long userId) {
    return find("user.id = ?1 and isActive = true and usedAt is null and lockedAt is null", userId)
        .firstResultOptional();
  }

  public void deactivateAllActiveByUserId(Long userId) {
    update("isActive = false where user.id = ?1 and isActive = true and usedAt is null", userId);
  }

  public Optional<AccountValidationCode> findActiveNonExpiredByUserId(Long userId, Instant now) {
    return find(
        "user.id = ?1 and isActive = true and usedAt is null and lockedAt is null and expiresAt > ?2",
        userId, now)
        .firstResultOptional();
  }

  @jakarta.transaction.Transactional
  public void expireActiveInviteForUser(Long userId) {
    update("expiresAt = ?1 where user.id = ?2 and isActive = true and usedAt is null",
        Instant.now().minus(1, ChronoUnit.DAYS), userId);
  }
}
