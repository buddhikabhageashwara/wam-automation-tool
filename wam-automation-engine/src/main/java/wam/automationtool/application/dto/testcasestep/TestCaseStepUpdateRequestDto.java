package wam.automationtool.application.dto.testcasestep;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class TestCaseStepUpdateRequestDto {

  private long executionOrder;
  @NotBlank(message = "error.test.case.step.name.empty")
  private String testCaseStepName;
  private String description;
  private long testCaseStepId;
}
