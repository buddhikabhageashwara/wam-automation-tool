package wam.automationtool.application.impl.testcasestep.execute;

import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;

public interface TestCaseStepExecutor {
    TestCaseStepExecuteResponseDto execute(TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto);
}
