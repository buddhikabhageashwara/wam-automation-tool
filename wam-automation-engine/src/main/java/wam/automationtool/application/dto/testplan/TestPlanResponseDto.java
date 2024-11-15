package wam.automationtool.application.dto.testplan;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TestPlanResponseDto {

  private TestPlanDto  testPlanDto;
}
