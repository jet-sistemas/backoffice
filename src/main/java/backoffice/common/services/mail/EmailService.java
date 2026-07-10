package backoffice.common.services.mail;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailService {

  @Inject
  Instance<MailProvider> mailProviders;

  @Inject
  @ConfigProperty(name = "backoffice.mail.provider", defaultValue = "log")
  String providerName;

  public void sendAccountValidation(AccountValidationMailPayload payload) {
    resolveProvider().sendAccountValidation(payload);
  }

  public void sendTemporaryPassword(TemporaryPasswordMailPayload payload) {
    resolveProvider().sendTemporaryPassword(payload);
  }

  private MailProvider resolveProvider() {
    String normalized = providerName == null ? "log" : providerName.trim().toLowerCase();
    for (MailProvider provider : mailProviders) {
      if ("resend".equals(normalized) && provider instanceof ResendMailProvider) {
        return provider;
      }
      if ("log".equals(normalized) && provider instanceof LogMailProvider) {
        return provider;
      }
    }
    return mailProviders.stream()
        .filter(LogMailProvider.class::isInstance)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Nenhum MailProvider disponível"));
  }
}
