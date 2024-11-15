package wam.automationtool.application.dto.parameter;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PreferenceParameterTypeAddRequestDto {

  @NotBlank(message = "error.preference.parameter.display.name.empty")
  private String parameterDisplayName;

  private String parameterName;
}
