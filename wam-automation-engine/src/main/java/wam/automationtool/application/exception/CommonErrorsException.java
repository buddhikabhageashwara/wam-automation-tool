package wam.automationtool.application.exception;

import java.util.List;
import lombok.Getter;

@Getter
public class CommonErrorsException extends RuntimeException {

  private final String code;
  private final transient List<CustomError> errors;

  public CommonErrorsException(final String code, final List<CustomError> errors) {

    super();

    this.code = code;
    this.errors = errors;
  }
}
