package backoffice.common.services.mail;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LogMailProvider implements MailProvider {

  private static final Logger LOG = Logger.getLogger(LogMailProvider.class);
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));

  private final List<AccountValidationMailPayload> sent = Collections.synchronizedList(new ArrayList<>());
  private final List<TemporaryPasswordMailPayload> temporaryPasswordSent =
      Collections.synchronizedList(new ArrayList<>());

  @Override
  public void sendAccountValidation(AccountValidationMailPayload payload) {
    sent.add(payload);
    LOG.infof(
        "MAIL[log] to=%s subject=Valide sua conta Jet code=%s tempPassword=%s url=%s expires=%s",
        payload.toEmail(),
        payload.validationCode(),
        payload.temporaryPassword(),
        payload.validationUrl(),
        FORMATTER.format(payload.expiresAt()));
  }

  @Override
  public void sendTemporaryPassword(TemporaryPasswordMailPayload payload) {
    temporaryPasswordSent.add(payload);
    LOG.infof(
        "MAIL[log] to=%s subject=Nova senha temporária Jet tempPassword=%s loginUrl=%s",
        payload.toEmail(),
        payload.temporaryPassword(),
        payload.loginUrl());
  }

  public List<AccountValidationMailPayload> getSentMessages() {
    synchronized (sent) {
      return List.copyOf(sent);
    }
  }

  public AccountValidationMailPayload getLastSent() {
    synchronized (sent) {
      if (sent.isEmpty()) {
        return null;
      }
      return sent.get(sent.size() - 1);
    }
  }

  public TemporaryPasswordMailPayload getLastTemporaryPasswordSent() {
    synchronized (temporaryPasswordSent) {
      if (temporaryPasswordSent.isEmpty()) {
        return null;
      }
      return temporaryPasswordSent.get(temporaryPasswordSent.size() - 1);
    }
  }

  public void clear() {
    sent.clear();
    temporaryPasswordSent.clear();
  }
}
