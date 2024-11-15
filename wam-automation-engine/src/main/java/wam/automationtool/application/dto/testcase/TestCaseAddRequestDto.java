package wam.automationtool.application.dto.testcase;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import wam.automationtool.application.config.ValidDateTime;

@Builder
@Getter
@Setter
public class TestCaseAddRequestDto {

  @NotBlank(message = "error.test.case.name.empty")
  private String testCaseName;

  private String description;

  private long testPlanId;

  @ValidDateTime(message = "error.execution.scheduled.date.invalid", format = "yyyy-MM-dd HH:mm:ss.SSSSSS")
  private String executionScheduledDate;

  @ValidDateTime(message = "error.execution.expiry.date.invalid", format = "yyyy-MM-dd HH:mm:ss.SSSSSS")
  private String executionExpiryDate;

  private String executionFrequency;

  private long executionOrder;
}
