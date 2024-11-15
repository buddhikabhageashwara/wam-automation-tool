package wam.automationtool.application.dto.testcasestep;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TestCaseStepResponseDto {

  private TestCaseStepDto  testCaseStepDto;
}
