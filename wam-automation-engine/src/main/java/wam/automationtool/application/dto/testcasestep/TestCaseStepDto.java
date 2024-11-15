package wam.automationtool.application.dto.testcasestep;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import wam.automationtool.application.dto.parameter.AssertParameterDto;
import wam.automationtool.application.dto.parameter.PreferenceParameterDto;
import java.util.List;

@Builder
@Getter
@Setter
public class TestCaseStepDto {

  private long id;
  private String testCaseStepType;
  private long executionOrder;
  private String testCaseStepName;
  private long testCaseId;
  private List<PreferenceParameterDto> preferenceParameterDtoList;
  private List<AssertParameterDto> assertParameterDtoList;
}
