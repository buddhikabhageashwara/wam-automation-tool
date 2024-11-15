package wam.automationtool.application.dto.alias;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class AliasParameterTypeAddRequestDto {

  @NotBlank(message = "error.assert.parameter.display.name.empty")
  private String parameterDisplayName;

  private String parameterName;
}
