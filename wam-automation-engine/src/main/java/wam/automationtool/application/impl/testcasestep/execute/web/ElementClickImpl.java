package wam.automationtool.application.impl.testcasestep.execute.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_STEP_EXECUTION_FAIL_CODE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_LOCATOR_INDEX;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_LOCATOR_TYPE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_LOCATOR_VALUE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_WEB_DRIVER_CACHE_NAME;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_INDEX;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_TYPE;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_VALUE;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.execute.ActualAndExpectedResultDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;
import wam.automationtool.application.exception.TestCaseStepExecutionFailException;
import wam.automationtool.application.impl.testcasestep.execute.TestCaseStepExecutor;
import wam.automationtool.application.impl.testcasestep.execute.TestCaseStepExecutorBase;
import wam.automationtool.application.util.AgentRequestManager;
import wam.automationtool.application.util.DateTimeManager;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepExecutionStatus;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

@Service
@Slf4j
public class ElementClickImpl extends TestCaseStepExecutorBase
    implements TestCaseStepExecutor {

  @Value("${selenium.grid.base.url}")
  private String seleniumGridBaseURL;

  @Override
  public TestCaseStepType getTestCaseStepType() {
    return TestCaseStepType.W_ELEMENT_CLICK;
  }

  @Override
  public TestCaseStepExecuteResponseDto execute(
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
    String startTime = DateTimeManager.getCurrentUTCDateTime();
    String status = TestCaseStepExecutionStatus.FAILED.toString();
    String endTime = null;
    final ActualAndExpectedResultDto actualAndExpectedResult;
    boolean isUnknown = false;
    String unknownReason = "";
    final LinkedHashMap<String, String> resultParameters = new LinkedHashMap<>();
    try {
      startTime = DateTimeManager.getCurrentUTCDateTime();
      final String agentURL = AgentRequestManager.getAgentURL(testCaseStepExecuteRequestDto);
      if (isNotRemoteExecution(testCaseStepExecuteRequestDto, agentURL)) {
        start(resultParameters, testCaseStepExecuteRequestDto);
      } else {
        return submitToAgent(testCaseStepExecuteRequestDto, agentURL);
      }
      status = TestCaseStepExecutionStatus.PASSED.toString();
    } catch (final TestCaseStepExecutionFailException exception) {
      status = TestCaseStepExecutionStatus.FAILED.toString();
      isUnknown = false;
    } catch (final Exception exception) {
      status = TestCaseStepExecutionStatus.FAILED.toString();
      isUnknown = true;
      unknownReason = exception.getMessage();
    } finally {
      endTime = DateTimeManager.getCurrentUTCDateTime();
      actualAndExpectedResult =
          getActualAndExpectedResult(
              resultParameters, testCaseStepExecuteRequestDto, isUnknown, status, unknownReason);
    }
    return buildResponse(status, actualAndExpectedResult, startTime, endTime, null);
  }

  private void start(
      final LinkedHashMap<String, String> resultParameters,
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
    final Map<String, String> extractedPreferenceParameters =
        extractPreferenceParameters(testCaseStepExecuteRequestDto);
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters, TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME.getParameterName());
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters, TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_TYPE.getParameterName());
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters, TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_VALUE.getParameterName());
    final String webDriverCacheName =
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME.getParameterName());
    resultParameters.put(TCS_RESULT_WEB_DRIVER_CACHE_NAME, webDriverCacheName);
    resultParameters.put(
        TCS_RESULT_ELEMENT_LOCATOR_TYPE,
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_TYPE.getParameterName()));
    resultParameters.put(
        TCS_RESULT_ELEMENT_LOCATOR_VALUE,
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_VALUE.getParameterName()));
    resultParameters.put(
        TCS_RESULT_ELEMENT_LOCATOR_INDEX,
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_INDEX.getParameterName()));
    final WebDriver driver = getActiveWebDriverByName(webDriverCacheName);
    final String locatorType =
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_TYPE.getParameterName());
    final String locatorValue =
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_VALUE.getParameterName());
    final int locatorIndex =
        Integer.parseInt(
            extractedPreferenceParameters.get(
                TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_INDEX.getParameterName()));
      clickElement(driver, locatorType, locatorValue, locatorIndex);
  }

  private void clickElement(
      final WebDriver driver,
      final String locatorType,
      final String locatorValue,
      final int locatorIndex) {
    if (Objects.nonNull(driver)) {
      final WebElement webElement = getWebElement(driver, locatorType, locatorValue, locatorIndex);
      webElement.click();
    } else {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "driver not found to click element");
    }
  }
}
