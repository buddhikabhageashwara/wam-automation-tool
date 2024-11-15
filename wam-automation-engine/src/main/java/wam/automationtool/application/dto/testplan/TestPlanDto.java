package wam.automationtool.application.dto.testplan;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TestPlanDto {

  private long id;
  private String testPlanName;
  private String description;
  private String executionScheduledDate;
  private String executionExpiryDate;
  private String executionFrequency;
  private long numberOfTestCases;
}
