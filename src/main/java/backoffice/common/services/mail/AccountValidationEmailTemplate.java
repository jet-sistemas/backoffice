package backoffice.common.services.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Year;

public final class AccountValidationEmailTemplate {

  private static final String TEMPLATE_PATH = "mail-templates/account-validation-email.html";
  private static final String LOGO_URL = "https://pub-1e6a445c212b4d9b9ca003356c69a6f5.r2.dev/3/14_1c23b56459.png";
  private static final String FENIX_URL = "https://pub-1e6a445c212b4d9b9ca003356c69a6f5.r2.dev/3/logo_jet_fenix_00cbbde45f.svg";

  private static final String TEMPLATE = loadTemplate();

  private AccountValidationEmailTemplate() {
  }

  public static String render(AccountValidationMailPayload payload, String expiresFormatted) {
    return TEMPLATE
        .replace("${userName}", escapeHtml(payload.userName()))
        .replace("${validationUrl}", escapeHtml(payload.validationUrl()))
        .replace("${validationCode}", escapeHtml(payload.validationCode()))
        .replace("${temporaryPassword}", escapeHtml(payload.temporaryPassword()))
        .replace("${expiresAt}", escapeHtml(expiresFormatted))
        .replace("${year}", String.valueOf(Year.now().getValue()))
        .replace("${logoUrl}", LOGO_URL)
        .replace("${fenixUrl}", FENIX_URL);
  }

  private static String loadTemplate() {
    try (InputStream stream = AccountValidationEmailTemplate.class
        .getClassLoader()
        .getResourceAsStream(TEMPLATE_PATH)) {
      if (stream == null) {
        throw new IllegalStateException("Template de e-mail não encontrado: " + TEMPLATE_PATH);
      }
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException("Falha ao carregar template de e-mail: " + TEMPLATE_PATH, e);
    }
  }

  private static String escapeHtml(String value) {
    return value
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
