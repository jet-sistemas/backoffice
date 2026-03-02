package project.common.exceptions.customs;

public class NotFoundException extends RuntimeException {
  private final int statusCode;

  public NotFoundException(String message) {
    super(message);
    this.statusCode = 404;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
