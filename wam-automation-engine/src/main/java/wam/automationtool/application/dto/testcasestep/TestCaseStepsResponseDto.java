package wam.automationtool.application.dto.testcasestep;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TestCaseStepsResponseDto {

  private List<TestCaseStepDto>  testCaseStepDtoList;
}
