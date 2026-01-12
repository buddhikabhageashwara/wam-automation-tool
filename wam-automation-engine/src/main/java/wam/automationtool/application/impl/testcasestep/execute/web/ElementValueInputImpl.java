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
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_INPUT_CACHE_VALUE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_INPUT_VALUE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_INPUT_VALUE_CACHE_KEY;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_LOCATOR_INDEX;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_LOCATOR_TYPE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_LOCATOR_VALUE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_WEB_DRIVER_CACHE_NAME;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.cache.CacheDataDto;
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
public class ElementValueInputImpl extends TestCaseStepExecutorBase
    implements TestCaseStepExecutor {

  @Value("${selenium.grid.base.url}")
  private String seleniumGridBaseURL;

  @Override
  public TestCaseStepType getTestCaseStepType() {
    return TestCaseStepType.W_ELEMENT_VALUE_INPUT;
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
        putCommonResultParameters(resultParameters, extractedPreferenceParameters);
        final String webDriverCacheName =
                extractedPreferenceParameters.get(
                        TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME.getParameterName());
        final WebDriver driver = getActiveWebDriverByName(webDriverCacheName);
        final String locatorType = extractedPreferenceParameters.get(
                TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_TYPE.getParameterName());
        final String locatorValue = extractedPreferenceParameters.get(
                TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_VALUE.getParameterName());
        final int locatorIndex = parseLocatorIndex(extractedPreferenceParameters);
        final String resolvedInputValue = resolveInputValue(
                testCaseStepExecuteRequestDto, extractedPreferenceParameters, resultParameters);
        inputValueToElement(driver, locatorType, locatorValue, resolvedInputValue, locatorIndex);
    }

    private void validateRequiredPreferenceParameters(final Map<String, String> preference) {
        getAndValidateTCSPreferenceParameterTypeExistence(
                preference, TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME.getParameterName());
        getAndValidateTCSPreferenceParameterTypeExistence(
                preference, TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_TYPE.getParameterName());
        getAndValidateTCSPreferenceParameterTypeExistence(
                preference, TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_VALUE.getParameterName());
        getAndValidateTCSPreferenceParameterTypeExistence(
                preference, TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_INDEX.getParameterName());

        // Conditional validation:
        // If cache key is empty -> input value must exist (as per your comment).
        final String cacheKey = preference.get(
                TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INPUT_VALUE_CACHE_KEY.getParameterName());
        if (isBlank(cacheKey)) {
            getAndValidateTCSPreferenceParameterTypeExistence(
                    preference, TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INPUT_VALUE.getParameterName());
        }
    }

    private void putCommonResultParameters(
            final LinkedHashMap<String, String> resultParameters,
            final Map<String, String> pref) {

        resultParameters.put(
                TCS_RESULT_WEB_DRIVER_CACHE_NAME,
                pref.get(TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME.getParameterName()));

        resultParameters.put(
                TCS_RESULT_ELEMENT_LOCATOR_TYPE,
                pref.get(TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_TYPE.getParameterName()));

        resultParameters.put(
                TCS_RESULT_ELEMENT_LOCATOR_VALUE,
                pref.get(TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_VALUE.getParameterName()));

        resultParameters.put(
                TCS_RESULT_ELEMENT_LOCATOR_INDEX,
                pref.get(TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_INDEX.getParameterName()));

        resultParameters.put(
                TCS_RESULT_ELEMENT_INPUT_VALUE,
                pref.get(TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INPUT_VALUE.getParameterName()));

        resultParameters.put(
                TCS_RESULT_ELEMENT_INPUT_VALUE_CACHE_KEY,
                pref.get(TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INPUT_VALUE_CACHE_KEY.getParameterName()));
    }

    /**
     * Priority:
     * 1) If cache key is provided -> read from string cache map (first priority)
     * 2) Otherwise -> use direct input value
     *
     * Also sets:
     * - TCS_RESULT_ELEMENT_INPUT_CACHE_VALUE (new)
     */
    private String resolveInputValue(
            final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto,
            final Map<String, String> extractedPreferenceParameters,
            final LinkedHashMap<String, String> resultParameters) {
        final String cacheKey = extractedPreferenceParameters.get(
                TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INPUT_VALUE_CACHE_KEY.getParameterName());
        if (!isBlank(cacheKey)) {
            final Map<String, String> stringCacheMap = getStringCacheMap(testCaseStepExecuteRequestDto);
            final String cachedValue = stringCacheMap.get(cacheKey);
            // Save resolved cache value to results (new parameter).
            resultParameters.put(TCS_RESULT_ELEMENT_INPUT_CACHE_VALUE, cachedValue);
            // If you want to enforce "cache key must exist", keep this validation.
            // If null is allowed, remove this block.
            if (Objects.isNull(cachedValue)) {
                throw new TestCaseStepExecutionFailException(
                        BAD_REQUEST,
                        TEST_CASE_STEP_EXECUTION_FAIL_CODE,
                        "No cached value found for key: " + cacheKey
                                + " (executionId=" + testCaseStepExecuteRequestDto.getExecutionId() + ")");
            }
            return cachedValue;
        }
        // No cache key -> fallback to direct input value
        final String directInput = extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INPUT_VALUE.getParameterName());
        // New result parameter: no cache used, so keep it empty (or omit if you prefer)
        resultParameters.put(TCS_RESULT_ELEMENT_INPUT_CACHE_VALUE, null);
        return directInput;
    }

    private Map<String, String> getStringCacheMap(final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
        final CacheDataDto cacheDataDto =
                getAndValidateCacheDataDto(testCaseStepExecuteRequestDto.getExecutionId());
        return getAndValidateStringCacheMap(cacheDataDto, testCaseStepExecuteRequestDto.getExecutionId());
    }

    private int parseLocatorIndex(final Map<String, String> pref) {
        return Integer.parseInt(
                pref.get(
                        TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_INDEX.getParameterName()));
    }

    private boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    private void inputValueToElement(
      final WebDriver driver,
      final String locatorType,
      final String locatorValue,
      final String inputValue,
      final int locatorIndex) {
    if (Objects.nonNull(driver)) {
      final WebElement webElement = getWebElement(driver, locatorType, locatorValue, locatorIndex);
      webElement.sendKeys(inputValue);
    } else {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "driver not found to input value to element");
    }
  }
}
