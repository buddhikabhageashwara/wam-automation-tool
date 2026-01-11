package wam.automationtool.application.dto.testcasestep;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.LinkedHashMap;

@Builder
@Getter
@Setter
public class TestCaseStepAddRequestDto {

  private String testCaseStepType;
  private long executionOrder;
  @NotBlank(message = "error.test.case.step.name.empty")
  private String testCaseStepName;
  private String description;
  private long testCaseId;
  private LinkedHashMap<String, String> preferenceParameters;
  private LinkedHashMap<String, String> assertParameters;
}
