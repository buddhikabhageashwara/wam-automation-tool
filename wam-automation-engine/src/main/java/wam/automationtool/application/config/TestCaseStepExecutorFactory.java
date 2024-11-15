package wam.automationtool.application.config;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.INVALID_TEST_CASE_STEP_TYPE_CODE;
import static wam.automationtool.domain.entity.testcasestep.TestCaseStepType.A_LOG_FILE_EXTRACT;
import static wam.automationtool.domain.entity.testcasestep.TestCaseStepType.A_SEND_HTTP_REQUEST;
import static wam.automationtool.domain.entity.testcasestep.TestCaseStepType.M_OPEN_APP;
import static wam.automationtool.domain.entity.testcasestep.TestCaseStepType.W_OPEN_BROWSER;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wam.automationtool.application.exception.InvalidTestCaseStepTypeException;
import wam.automationtool.application.impl.testcasestep.execute.TestCaseStepExecutor;
import wam.automationtool.application.impl.testcasestep.execute.api.LogFileExtractImpl;
import wam.automationtool.application.impl.testcasestep.execute.api.SendHTTPRequestImpl;
import wam.automationtool.application.impl.testcasestep.execute.mobile.OpenAppImpl;
import wam.automationtool.application.impl.testcasestep.execute.web.OpenBrowserImpl;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

@Slf4j
@Service
public class TestCaseStepExecutorFactory {

  private final Map<TestCaseStepType, TestCaseStepExecutor> testCaseStepTypeExecutorMap =
      new HashMap<>();
  {
    testCaseStepTypeExecutorMap.put(A_SEND_HTTP_REQUEST, new SendHTTPRequestImpl());
    testCaseStepTypeExecutorMap.put(A_LOG_FILE_EXTRACT, new LogFileExtractImpl());
    testCaseStepTypeExecutorMap.put(W_OPEN_BROWSER, new OpenBrowserImpl());
    testCaseStepTypeExecutorMap.put(M_OPEN_APP, new OpenAppImpl());
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
