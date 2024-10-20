package wam.automationtool.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class Response<T> {
  private String message;
  private String code;
  private T data;
}
