package wam.automationtool.application.dto.parameter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PreferenceParameterTypeDto {

  private long id;
  private String parameterDisplayName;
  private String parameterName;
}
