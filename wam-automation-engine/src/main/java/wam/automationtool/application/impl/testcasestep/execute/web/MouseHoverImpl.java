/*
 * MIT License
 *
 * Copyright (c) 2024 buddhika bhageashwara alwis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package wam.automationtool.application.impl.testcasestep.execute.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_STEP_EXECUTION_FAIL_CODE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_HTML_CODE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_INDEX;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_WEB_DRIVER_CACHE_NAME;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_HTML_CODE;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INDEX;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType;
import wam.automationtool.application.dto.execute.ActualAndExpectedResultDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;
import wam.automationtool.application.exception.TestCaseStepExecutionFailException;
import wam.automationtool.application.impl.testcasestep.execute.TestCaseStepExecutor;
import wam.automationtool.application.impl.testcasestep.execute.TestCaseStepExecutorBase;
import wam.automationtool.application.util.AgentRequestManager;
import wam.automationtool.application.util.AutoLocatorDetector;
import wam.automationtool.application.util.DateTimeManager;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepExecutionStatus;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

@Service
@Slf4j
public class MouseHoverImpl extends TestCaseStepExecutorBase implements TestCaseStepExecutor {

  @Value("${selenium.grid.base.url}")
  private String seleniumGridBaseURL;

  @Override
  public TestCaseStepType getTestCaseStepType() {
    return TestCaseStepType.W_MOUSE_HOVER;
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
        extractedPreferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME.getParameterName());
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_HTML_CODE.getParameterName());
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INDEX.getParameterName());
    final String webDriverCacheName =
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME.getParameterName());
    resultParameters.put(TCS_RESULT_WEB_DRIVER_CACHE_NAME, webDriverCacheName);
    resultParameters.put(
        TCS_RESULT_ELEMENT_HTML_CODE,
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_HTML_CODE.getParameterName()));
    resultParameters.put(
        TCS_RESULT_ELEMENT_INDEX,
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INDEX.getParameterName()));

    final WebDriver webDriver = getActiveWebDriverByName(webDriverCacheName);
    final String elementHTMLCode =
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_HTML_CODE.getParameterName());
    final int elementIndex =
        Integer.parseInt(
            extractedPreferenceParameters.get(
                TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INDEX
                    .getParameterName()));

    final String extractedXPath =
        AutoLocatorDetector.selfHealXPathByHtml(webDriver, elementHTMLCode, elementIndex);

    mouseHover(webDriver, extractedXPath, elementIndex);
  }

  private void mouseHover(
      final WebDriver webDriver, final String extractedXPath, final int elementIndex) {
    if (Objects.nonNull(webDriver)) {
      final WebElement webElement = getWebElement(webDriver, "xpath", extractedXPath, elementIndex);
        final Actions actions = new Actions(webDriver);
        actions.moveToElement(webElement).perform();
    } else {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, "mouse hover failed");
    }
  }
}
