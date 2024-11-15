package wam.automationtool.application.dto.testplan;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import wam.automationtool.application.config.ValidDateTime;

@Builder
@Getter
@Setter
public class TestPlanUpdateRequestDto {

  @NotBlank(message = "error.test.plan.name.empty")
  private String testPlanName;

  private String description;

  @ValidDateTime(message = "error.execution.scheduled.date.invalid", format = "yyyy-MM-dd HH:mm:ss.SSSSSS")
  private String executionScheduledDate;

  @ValidDateTime(message = "error.execution.expiry.date.invalid", format = "yyyy-MM-dd HH:mm:ss.SSSSSS")
  private String executionExpiryDate;

  private String executionFrequency;
}
