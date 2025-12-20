package wam.automationtool.application.impl.testcasestep.execute;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.execute.ActualAndExpectedResultDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;
import wam.automationtool.application.exception.TestCaseStepExecutionFailException;
import wam.automationtool.application.impl.testcasestep.execute.web.OpenBrowserImpl;
import wam.automationtool.application.util.AgentRequestManager;
import wam.automationtool.application.util.TestCaseStepActualAndExpectedResultManager;
import wam.automationtool.application.util.WAMCacheManager;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_STEP_EXECUTION_FAIL_CODE;

@Service
@Slf4j
public abstract class TestCaseStepExecutorBase {

  @Autowired
  private WAMCacheManager wamCacheManager;

  protected WAMCacheManager getWamCacheManager() {
    return wamCacheManager;
  }

  protected boolean isNotRemoteExecution(
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto, final String agentURL) {
    // Checks if the request is not for remote execution
    return testCaseStepExecuteRequestDto.isAgentRequest()
        ? testCaseStepExecuteRequestDto.isAgentRequest()
        : Objects.isNull(agentURL);
  }

  protected TestCaseStepExecuteResponseDto buildResponse(
      String status,
      ActualAndExpectedResultDto actualAndExpectedResult,
      String startTime,
      String endTime) {
    // Builds and returns the response DTO
    return TestCaseStepExecuteResponseDto.builder()
        .status(status)
        .expectedResult(actualAndExpectedResult.getExpectedResult())
        .actualResult(actualAndExpectedResult.getActualResult())
        .startTime(startTime)
        .endTime(endTime)
        .build();
  }

  protected ActualAndExpectedResultDto getActualAndExpectedResult(
      LinkedHashMap<String, String> resultParameters,
      TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto,
      boolean isUnknown,
      String status,
      String unknownReason) {
    // Retrieves the actual and expected result based on the parameters
    return TestCaseStepActualAndExpectedResultManager.getActualAndExpectedResult(
        resultParameters,
        TestCaseStepType.valueOf(testCaseStepExecuteRequestDto.getTestCaseStepType()),
        isUnknown,
        status,
        unknownReason);
  }

  protected TestCaseStepExecuteResponseDto submitToAgent(
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto, final String agentURL) {
    // Submits the request to the agent for remote execution
    return AgentRequestManager.submitToAgent(testCaseStepExecuteRequestDto, agentURL);
  }

  protected Map<String, String> extractPreferenceParameters(
          final TestCaseStepExecuteRequestDto requestDto) {
    final Map<String, String> parameters = new LinkedHashMap<>();
    requestDto.getTestCaseStepDto().getPreferenceParameterDtoList()
            .forEach(preference -> {
              parameters.put(preference.getParameterName(), preference.getParameterValue());
            });
    return parameters;
  }

  protected WebDriver getActiveWebDriverByName(final String webDriverCacheName) {
    final WebDriver existingDriver = OpenBrowserImpl.getActiveDrivers().get(webDriverCacheName);
    if (Objects.isNull(existingDriver)) {
      log.warn("No WebDriver found for the given cache name: {}", webDriverCacheName);
        throw new TestCaseStepExecutionFailException(
                BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, "driver not found to close");
    } else {
      return existingDriver;
    }
  }
}
