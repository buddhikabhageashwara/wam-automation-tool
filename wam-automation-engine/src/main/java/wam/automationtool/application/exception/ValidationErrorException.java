package wam.automationtool.application.exception;

public class ValidationErrorException extends RuntimeException {
  public ValidationErrorException(final String message) {
    super(message);
  }
}
