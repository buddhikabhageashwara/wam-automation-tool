package wam.automationtool.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Setter;
import wam.automationtool.application.exception.CustomError;
import java.util.List;

@Setter
@Builder
public class ErrorResponse {

  @JsonProperty("code")
  private String code;

  @JsonProperty("errors")
  private List<CustomError> errors;
}
