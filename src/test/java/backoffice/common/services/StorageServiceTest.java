package backoffice.common.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StorageServiceTest {

  @Test
  @DisplayName("keyMatchesEntityPrefix aceita chave sob o prefixo da entidade")
  void keyMatchesEntityPrefix_acceptsValidKey() {
    StorageService svc = new StorageService();
    String prefix = "dev/users/avatar/42/";
    assertTrue(svc.keyMatchesEntityPrefix(prefix + "2026/03/1_abc12345.png", prefix));
  }

  @Test
  @DisplayName("keyMatchesEntityPrefix rejeita outro id de entidade")
  void keyMatchesEntityPrefix_rejectsOtherEntityId() {
    StorageService svc = new StorageService();
    assertFalse(svc.keyMatchesEntityPrefix("dev/users/avatar/99/x.png", "dev/users/avatar/9/"));
  }
}
