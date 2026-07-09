package backoffice.common.services.mail;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import backoffice.common.exceptions.MessageErrorEnum;
import backoffice.common.exceptions.customs.BusinessException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ResendMailProvider implements MailProvider {

  private static final Logger LOG = Logger.getLogger(ResendMailProvider.class);
  private static final URI RESEND_URI = URI.create("https://api.resend.com/emails");
  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));

  @Inject
  @ConfigProperty(name = "backoffice.mail.resend.api-key")
  Optional<String> apiKeyOpt;

  @Inject
  @ConfigProperty(name = "backoffice.mail.from")
  String mailFrom;

  private final HttpClient httpClient = HttpClient.newHttpClient();

  @Override
  public void sendAccountValidation(AccountValidationMailPayload payload) {
    String apiKey = apiKeyOpt.map(String::trim).filter(s -> !s.isEmpty()).orElse(null);
    if (apiKey == null) {
      throw new BusinessException(MessageErrorEnum.EMAIL_SEND_FAILED.getMessage(), 500);
    }

    String expiresFormatted = FORMATTER.format(payload.expiresAt());
    String html = AccountValidationEmailTemplate.render(payload, expiresFormatted);

    String body = """
        {"from":"%s","to":["%s"],"subject":"Valide sua conta Jet","html":"%s"}
        """
        .formatted(escapeJson(mailFrom), escapeJson(payload.toEmail()), escapeJson(html));

    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(RESEND_URI)
          .header("Authorization", "Bearer " + apiKey)
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(body))
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        LOG.errorf("Resend API error status=%d body=%s", response.statusCode(), response.body());
        throw new BusinessException(MessageErrorEnum.EMAIL_SEND_FAILED.getMessage(), 500);
      }
    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      LOG.error("Falha ao enviar e-mail via Resend", e);
      throw new BusinessException(MessageErrorEnum.EMAIL_SEND_FAILED.getMessage(), 500);
    }
  }

  private static String escapeJson(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r");
  }
}
