package wam.automationtool.domain.entity.testcasestep;

import lombok.Getter;

@Getter
public enum TestCaseStepExecutionStatus {
  PASSED("PASSED"),
  FAILED("FAILED");

  private final String id;

  TestCaseStepExecutionStatus(final String id) {
    this.id = id;
  }

}
