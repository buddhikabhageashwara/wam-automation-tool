package wam.automationtool.application.dto.report;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TestCaseStepExecutionDto {

  private String startTime;
  private String endTime;
  private long id;
  private long executionOrder;
  private String testCaseStepType;
  private String testCaseStepName;
  private String expectedResult;
  private String actualResult;
  private String status;
}
