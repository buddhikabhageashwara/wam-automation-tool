package wam.automationtool.application.config;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.INVALID_TEST_CASE_STEP_TYPE_CODE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wam.automationtool.application.exception.InvalidTestCaseStepTypeException;
import wam.automationtool.application.impl.testcasestep.execute.TestCaseStepExecutor;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

@Slf4j
@Service
public class TestCaseStepExecutorFactory {

  private final Map<TestCaseStepType, TestCaseStepExecutor> testCaseStepTypeExecutorMap;

  @Autowired
  public TestCaseStepExecutorFactory(List<TestCaseStepExecutor> executors) {
    this.testCaseStepTypeExecutorMap = new HashMap<>();
    executors.forEach(executor -> this.testCaseStepTypeExecutorMap.put(executor.getTestCaseStepType(), executor));
  }

  public TestCaseStepExecutor getTestCaseStepExecutor(final TestCaseStepType testCaseStepType) {
    final TestCaseStepExecutor executor = testCaseStepTypeExecutorMap.get(testCaseStepType);
    if (Objects.isNull(executor)) {
      throw new InvalidTestCaseStepTypeException(
              BAD_REQUEST, INVALID_TEST_CASE_STEP_TYPE_CODE, "error.invalid.test.case.step.type");
    }
    return executor;
  }
}

