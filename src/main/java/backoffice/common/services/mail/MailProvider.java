package backoffice.common.services.mail;

public interface MailProvider {

  void sendAccountValidation(AccountValidationMailPayload payload);

  void sendTemporaryPassword(TemporaryPasswordMailPayload payload);
}
