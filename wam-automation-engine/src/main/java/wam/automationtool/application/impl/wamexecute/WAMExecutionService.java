package wam.automationtool.application.impl.wamexecute;

import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;

public interface WAMExecutionService {

  void executeByTestPlan(long testPlanId);
  void executeByTestCase(long testcaseId);
  TestCaseStepExecuteResponseDto executeByTestCaseStep(
          long testCaseStepId, TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto);
}
