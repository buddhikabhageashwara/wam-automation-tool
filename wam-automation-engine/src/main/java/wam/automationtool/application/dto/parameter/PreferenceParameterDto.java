package wam.automationtool.application.dto.parameter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class PreferenceParameterDto {

  private long id;
  private String parameterValue;
  private String parameterName;
  private String parameterDisplayName;
  private long preferenceParameterTypeId;
  private long testCaseStepId;
}
