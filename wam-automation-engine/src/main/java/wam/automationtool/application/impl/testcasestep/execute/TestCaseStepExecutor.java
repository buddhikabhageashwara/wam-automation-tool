package wam.automationtool.application.impl.testcasestep.execute;

import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

public interface TestCaseStepExecutor {
    TestCaseStepExecuteResponseDto execute(TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto);
    TestCaseStepType getTestCaseStepType();
}
