package wam.automationtool.application.dto.report;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TestCaseExecutionSummaryDto {

  private int totalTestCases;
  private int passedTestCases;
  private int failedTestCases;
}
