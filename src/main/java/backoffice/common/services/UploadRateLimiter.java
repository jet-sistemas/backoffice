package backoffice.common.services;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UploadRateLimiter {

  private static final Logger LOG = Logger.getLogger(UploadRateLimiter.class);

  @Inject
  @ConfigProperty(name = "backoffice.storage.upload.rate-limit.capacity", defaultValue = "30")
  int capacity;

  @Inject
  @ConfigProperty(name = "backoffice.storage.upload.rate-limit.refill-minutes", defaultValue = "1")
  int refillMinutes;

  private final Map<String, Object> mutexByKey = new ConcurrentHashMap<>();
  private final Map<String, WindowState> windows = new ConcurrentHashMap<>();

  public void consumeOrThrow(String rateLimitKey) {
    if (rateLimitKey == null || rateLimitKey.isBlank()) {
      rateLimitKey = "anonymous";
    }
    Duration window = Duration.ofMinutes(Math.max(1, refillMinutes));
    int cap = Math.max(1, capacity);
    String key = rateLimitKey;
    Object mutex = mutexByKey.computeIfAbsent(key, k -> new Object());
    synchronized (mutex) {
      Instant now = Instant.now();
      WindowState state = windows.computeIfAbsent(key, k -> new WindowState(now, 0));
      if (Duration.between(state.windowStart, now).compareTo(window) >= 0) {
        state.windowStart = now;
        state.count = 0;
      }
      if (state.count >= cap) {
        LOG.warnv("Rate limit de upload excedido para chave={0}", key);
        throw new BusinessException(MessageErrorEnum.UPLOAD_RATE_LIMIT.getMessage(), 429);
      }
      state.count++;
    }
  }

  private static final class WindowState {
    Instant windowStart;
    int count;

    WindowState(Instant windowStart, int count) {
      this.windowStart = windowStart;
      this.count = count;
    }
  }
}
