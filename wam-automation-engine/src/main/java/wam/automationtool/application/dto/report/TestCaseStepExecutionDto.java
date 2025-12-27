package wam.automationtool.application.dto.report;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import wam.automationtool.application.dto.testcasestep.LogFileBase64Dto;

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
  private LogFileBase64Dto logFileBase64Dto;
}
