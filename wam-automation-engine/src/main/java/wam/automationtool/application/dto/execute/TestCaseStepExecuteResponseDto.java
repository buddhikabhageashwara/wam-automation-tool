package wam.automationtool.application.dto.execute;

import lombok.Builder;
import lombok.Data;
import wam.automationtool.application.dto.testcasestep.LogFileBase64Dto;

@Builder
@Data
public class TestCaseStepExecuteResponseDto {

  private String status;
  private String expectedResult;
  private String actualResult;
  private String startTime;
  private String endTime;
  private LogFileBase64Dto logFileBase64Dto;
}
