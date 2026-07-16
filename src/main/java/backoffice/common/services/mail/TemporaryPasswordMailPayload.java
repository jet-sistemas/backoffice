package backoffice.common.services.mail;

public record TemporaryPasswordMailPayload(
    String toEmail,
    String userName,
    String temporaryPassword,
    String loginUrl) {
}
