package wam.automationtool.application.dto.execute;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TestCaseStepExecuteResponseDto {

  private String status;
  private String expectedResult;
  private String actualResult;
  private String startTime;
  private String endTime;
}
