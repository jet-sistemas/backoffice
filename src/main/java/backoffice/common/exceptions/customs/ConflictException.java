package project.common.exceptions.customs;

public class ConflictException extends RuntimeException {
  private final int statusCode;

  public ConflictException(String message) {
    super(message);
    this.statusCode = 409;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
