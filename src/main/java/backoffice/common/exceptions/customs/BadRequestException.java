package project.common.exceptions.customs;

public class BadRequestException extends RuntimeException {
  private final int statusCode;

  public BadRequestException(String message) {
    super(message);
    this.statusCode = 400;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
