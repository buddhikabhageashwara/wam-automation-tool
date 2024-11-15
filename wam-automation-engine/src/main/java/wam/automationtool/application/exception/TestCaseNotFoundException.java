package wam.automationtool.application.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TestCaseNotFoundException extends RuntimeException {

  private final HttpStatus status;
  private final String code;
  private final String msgKey;

  public TestCaseNotFoundException(
      final HttpStatus status, final String code, final String msgKey) {

    super();

    this.status = status;
    this.code = code;
    this.msgKey = msgKey;
  }
}
