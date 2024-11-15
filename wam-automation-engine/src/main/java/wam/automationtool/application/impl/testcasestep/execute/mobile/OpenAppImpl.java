package wam.automationtool.application.impl.testcasestep.execute.mobile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;
import wam.automationtool.application.impl.testcasestep.execute.TestCaseStepExecutor;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAppImpl implements TestCaseStepExecutor {

  @Override
  public TestCaseStepExecuteResponseDto execute(
      TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
    return null;
  }
}
