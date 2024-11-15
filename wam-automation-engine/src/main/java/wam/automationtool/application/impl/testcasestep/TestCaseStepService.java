package wam.automationtool.application.impl.testcasestep;

import wam.automationtool.application.dto.testcasestep.TestCaseStepAddRequestDto;
import wam.automationtool.application.dto.testcasestep.TestCaseStepResponseDto;
import wam.automationtool.application.dto.testcasestep.TestCaseStepsResponseDto;

public interface TestCaseStepService {

  void addTestCaseStep(TestCaseStepAddRequestDto testCaseStepAddRequestDto);

  TestCaseStepsResponseDto getTestCaseSteps(long testCaseId);

  TestCaseStepResponseDto getTestCaseStep(long testCaseStepId);

  void deleteTestCaseStep(long testCaseId);
}
