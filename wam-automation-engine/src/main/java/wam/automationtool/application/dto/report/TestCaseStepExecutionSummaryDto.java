package wam.automationtool.application.dto.report;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TestCaseStepExecutionSummaryDto {

  private int totalTestCaseSteps;
  private int passedTestCaseSteps;
  private int failedTestCaseSteps;
}
