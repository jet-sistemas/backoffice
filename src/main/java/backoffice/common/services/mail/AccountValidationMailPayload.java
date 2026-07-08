package backoffice.common.services.mail;

import java.time.Instant;

public record AccountValidationMailPayload(
    String toEmail,
    String userName,
    String validationUrl,
    String validationCode,
    String temporaryPassword,
    Instant expiresAt) {
}
