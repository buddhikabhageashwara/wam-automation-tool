package wam.automationtool.application.exception;

import lombok.Getter;

@Getter
public class CustomResponseStatusException extends RuntimeException {

  private final String code;
  private final String msgKey;

  public CustomResponseStatusException(final String code, final String msgKey) {

    super();

    this.code = code;
    this.msgKey = msgKey;
  }
}
