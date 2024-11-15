package wam.automationtool.application.dto.testcase;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import wam.automationtool.application.config.ValidDateTime;

@Builder
@Getter
@Setter
public class TestCaseDto {

  private long id;
  private String testCaseName;
  private String description;
  private long testPlanId;
  private long numberOfTestCaseSteps;
  private String executionScheduledDate;
  private String executionExpiryDate;
  private String executionFrequency;
  private long executionOrder;
}
