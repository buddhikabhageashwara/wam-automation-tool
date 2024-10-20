package wam.automationtool.application.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Common exception class for API exceptions. */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
@Getter
public class InternalServerErrorException extends RuntimeException {

  public InternalServerErrorException(final String message) {
    super(message);
  }
}
