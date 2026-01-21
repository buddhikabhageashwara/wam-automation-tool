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
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.*;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.*;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
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
import wam.automationtool.application.util.RenderedCssChecker;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepExecutionStatus;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

@Service
@Slf4j
public class ElementCSSValueAttributeReadVerifyImpl extends TestCaseStepExecutorBase
    implements TestCaseStepExecutor {

  @Value("${selenium.grid.base.url}")
  private String seleniumGridBaseURL;

  @Override
  public TestCaseStepType getTestCaseStepType() {
    return TestCaseStepType.W_ELEMENT_CSS_ATTRIBUTE_READ_VERIFY;
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

    validateRequiredPreferenceParameters(extractedPreferenceParameters);

    final String webDriverCacheName = getWebDriverCacheName(extractedPreferenceParameters);
    final String elementHTMLCode = getElementHtmlCode(extractedPreferenceParameters);
    final int elementIndex = getElementIndex(extractedPreferenceParameters);
    final String cssAttributeName = getCssAttributeName(extractedPreferenceParameters);
    final String cssExpectedValue = getCssExpectedValue(extractedPreferenceParameters);

    putResultParameters(
        resultParameters,
        webDriverCacheName,
        elementHTMLCode,
        elementIndex,
        cssAttributeName,
        cssExpectedValue);

    log.info(
        "START | webDriverCacheName={} | elementIndex={} | cssAttributeName={} | cssExpectedValue={} | elementHtmlCode={}",
        webDriverCacheName,
        elementIndex,
        cssAttributeName,
        cssExpectedValue,
        elementHTMLCode);

    final WebDriver webDriver = getActiveWebDriverByName(webDriverCacheName);
    final String extractedXPath = selfHealXpath(webDriver, elementHTMLCode, elementIndex);
    final WebElement webElement = resolveWebElementByXpath(webDriver, extractedXPath, elementIndex);

    final RenderedCssChecker.Result result =
        RenderedCssChecker.checkRenderedCss(
            webDriver, webElement, cssAttributeName, cssExpectedValue);

    ensureCssMatched(result, resultParameters);

    log.info(
        "END | webDriverCacheName={} | elementIndex={} | cssAttributeName={} | passed={}",
        webDriverCacheName,
        elementIndex,
        cssAttributeName,
        result.passed());
  }

  /**
   * Validates existence of all required preference parameters.
   *
   * @param extractedPreferenceParameters extracted preference parameters map
   */
  private void validateRequiredPreferenceParameters(
      final Map<String, String> extractedPreferenceParameters) {

    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME.getParameterName());
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_HTML_CODE.getParameterName());
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INDEX.getParameterName());
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_CSS_ATTRIBUTE_NAME.getParameterName());
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_CSS_EXPECTED_VALUE.getParameterName());
  }

  /**
   * Extracts WebDriver cache name from preference parameters.
   *
   * @param extractedPreferenceParameters extracted preference parameters map
   * @return web driver cache name
   */
  private String getWebDriverCacheName(final Map<String, String> extractedPreferenceParameters) {
    return extractedPreferenceParameters.get(
        TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME.getParameterName());
  }

  /**
   * Extracts element HTML snippet from preference parameters.
   *
   * @param extractedPreferenceParameters extracted preference parameters map
   * @return element HTML snippet
   */
  private String getElementHtmlCode(final Map<String, String> extractedPreferenceParameters) {
    return extractedPreferenceParameters.get(
        TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_HTML_CODE.getParameterName());
  }

  /**
   * Extracts element index (0-based) from preference parameters.
   *
   * @param extractedPreferenceParameters extracted preference parameters map
   * @return element index
   */
  private int getElementIndex(final Map<String, String> extractedPreferenceParameters) {
    return Integer.parseInt(
        extractedPreferenceParameters.get(
            TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INDEX
                .getParameterName()));
  }

  /**
   * Extracts CSS attribute name from preference parameters.
   *
   * @param extractedPreferenceParameters extracted preference parameters map
   * @return css attribute name
   */
  private String getCssAttributeName(final Map<String, String> extractedPreferenceParameters) {
    return extractedPreferenceParameters.get(
        TCS_PREFERENCE_PARAMETER_TYPE_CSS_ATTRIBUTE_NAME.getParameterName());
  }

  /**
   * Extracts expected CSS value from preference parameters.
   *
   * @param extractedPreferenceParameters extracted preference parameters map
   * @return expected css value
   */
  private String getCssExpectedValue(final Map<String, String> extractedPreferenceParameters) {
    return extractedPreferenceParameters.get(
        TCS_PREFERENCE_PARAMETER_TYPE_CSS_EXPECTED_VALUE.getParameterName());
  }

  /**
   * Writes extracted parameters into {@code resultParameters} for downstream visibility/reporting.
   *
   * @param resultParameters output map used by the caller for step results
   * @param webDriverCacheName web driver cache name
   * @param elementHTMLCode element html snippet
   * @param elementIndex element index
   * @param cssAttributeName css attribute name
   * @param cssExpectedValue expected css value
   */
  private void putResultParameters(
      final LinkedHashMap<String, String> resultParameters,
      final String webDriverCacheName,
      final String elementHTMLCode,
      final int elementIndex,
      final String cssAttributeName,
      final String cssExpectedValue) {

    resultParameters.put(TCS_RESULT_WEB_DRIVER_CACHE_NAME, webDriverCacheName);
    resultParameters.put(TCS_RESULT_ELEMENT_HTML_CODE, elementHTMLCode);
    resultParameters.put(TCS_RESULT_ELEMENT_INDEX, String.valueOf(elementIndex));
    resultParameters.put(TCS_RESULT_CSS_ATTRIBUTE_NAME, cssAttributeName);
    resultParameters.put(TCS_RESULT_CSS_EXPECTED_VALUE, cssExpectedValue);
  }

  /**
   * Performs self-healing of XPath by using the given HTML snippet and index.
   *
   * @param webDriver active web driver instance
   * @param elementHTMLCode HTML snippet of the target element
   * @param elementIndex 0-based index to pick if multiple matches exist
   * @return extracted working XPath
   */
  private String selfHealXpath(
      final WebDriver webDriver, final String elementHTMLCode, final int elementIndex) {

    log.info(
        "Self-heal XPath | elementIndex={} | elementHtmlCode={}", elementIndex, elementHTMLCode);

    return AutoLocatorDetector.selfHealXPathByHtml(webDriver, elementHTMLCode, elementIndex);
  }

  /**
   * Resolves a {@link WebElement} using the given XPath.
   *
   * @param webDriver active web driver instance
   * @param extractedXPath healed xpath to locate the element
   * @param elementIndex index used by the underlying element resolver
   * @return resolved {@link WebElement}
   */
  private WebElement resolveWebElementByXpath(
      final WebDriver webDriver, final String extractedXPath, final int elementIndex) {

    log.info(
        "Resolve WebElement | locatorType=xpath | elementIndex={} | extractedXPath={}",
        elementIndex,
        extractedXPath);

    return getWebElement(webDriver, "xpath", extractedXPath, elementIndex);
  }

  /**
   * Ensures rendered CSS validation is successful.
   *
   * <p>When the match fails, {@link RenderedCssChecker.Result#note()} contains the failure reason.
   *
   * @param result rendered css check result
   * @throws TestCaseStepExecutionFailException when rendered css does not match expected value
   */
  private void ensureCssMatched(
      final RenderedCssChecker.Result result,
      final LinkedHashMap<String, String> resultParameters) {
    if (result.passed()) {
      return;
    }

    log.error(
        "Rendered CSS mismatch | cssAttribute={} | expected={} | actualRendered={} | note={}",
        result.cssAttribute(),
        result.expected(),
        result.actualRendered(),
        result.note());

    resultParameters.put(TCS_RESULT_CSS_EXPECTED_VALUE_UNMATCHED_REASON, result.note());
    resultParameters.put(TCS_RESULT_CSS_ACTUAL_VALUE, result.actualRendered());

    throw new TestCaseStepExecutionFailException(
        BAD_REQUEST,
        TEST_CASE_STEP_EXECUTION_FAIL_CODE,
        "Rendered CSS validation failed. cssAttribute="
            + result.cssAttribute()
            + ", expected="
            + result.expected()
            + ", actualRendered="
            + result.actualRendered()
            + ", note="
            + result.note());
  }
}
