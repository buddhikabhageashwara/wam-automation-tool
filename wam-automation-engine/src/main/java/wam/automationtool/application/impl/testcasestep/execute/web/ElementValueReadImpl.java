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
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INDEX;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType;
import wam.automationtool.application.dto.cache.CacheDataDto;
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
public class ElementValueReadImpl extends TestCaseStepExecutorBase implements TestCaseStepExecutor {

  @Value("${selenium.grid.base.url}")
  private String seleniumGridBaseURL;

  @Override
  public TestCaseStepType getTestCaseStepType() {
    return TestCaseStepType.W_ELEMENT_VALUE_READ;
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
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_KEY.getParameterName());
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
    resultParameters.put(
        TCS_RESULT_STRING_CACHE_MAP_KEY,
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_KEY.getParameterName()));
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
    resultParameters.put(
        TCS_RESULT_PICKED_ELEMENT,
        AutoLocatorDetector.getOuterHtmlByXPath(webDriver, extractedXPath));
    final String keyToSaveReadValue =
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_KEY.getParameterName());

    final String executionId = testCaseStepExecuteRequestDto.getExecutionId();
    readElement(
        resultParameters, webDriver, extractedXPath, elementIndex, keyToSaveReadValue, executionId);
  }

  private void readElement(
      final LinkedHashMap<String, String> resultParameters,
      final WebDriver webDriver,
      final String extractedXPath,
      final int elementIndex,
      final String keyToSaveReadValue,
      final String executionId) {
    try {
      final WebElement webElement = getWebElement(webDriver, "xpath", extractedXPath, elementIndex);
      final String extractedValue = readElementValueAsString(webElement, executionId);
      resultParameters.put(TCS_RESULT_STRING_CACHE_MAP_VALUE, extractedValue);
      final CacheDataDto cacheDataDto = getCacheDataDto(executionId);
      final Map<String, String> updatedStringCacheMap =
          buildUpdatedStringCacheMap(cacheDataDto, keyToSaveReadValue, extractedValue);
      persistCache(executionId, cacheDataDto, updatedStringCacheMap);
    } catch (final Exception exception) {
      if (exception instanceof TestCaseStepExecutionFailException) {
        throw (TestCaseStepExecutionFailException) exception;
      }
      final String msg =
          "failed to read element value, extractedXPath: "
              + safe(extractedXPath)
              + ", elementIndex: "
              + elementIndex
              + ", keyToSave: "
              + safe(keyToSaveReadValue);
      log.error("executionId: {}, message: {}", executionId, msg, exception);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, msg);
    }
  }

  /**
   * Auto-detects element type and extracts value as String. Empty string is allowed (valid value)
   * if it was successfully extracted. Only throws when it cannot extract (missing required
   * attributes, no selected option, selenium errors, etc.).
   */
  public String readElementValueAsString(final WebElement webElement, final String executionId) {
    try {
      if (isContentEditable(webElement)) {
        return readValueFromContentEditableElementAsString(webElement, executionId);
      }
      final String tagName = normalizeToLowerOrEmpty(webElement.getTagName());
      switch (tagName) {
        case "input":
          return readValueFromInputElementAsString(webElement, executionId);
        case "textarea":
          return readValueFromTextAreaElementAsString(webElement, executionId);
        case "select":
          return readValueFromSelectElementAsString(webElement, executionId);
        case "img":
          return readValueFromImageElementAsString(webElement, executionId);
        case "a":
          return readValueFromAnchorElementAsString(webElement, executionId);
        default:
          return readValueFromTextBasedElementAsString(webElement, executionId);
      }
    } catch (final Exception exception) {
      if (exception instanceof TestCaseStepExecutionFailException) {
        throw (TestCaseStepExecutionFailException) exception;
      }
      final String msg = "failed to read element value, unable to extract value from web element";
      log.error("executionId: {}, message: {}", executionId, msg, exception);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, msg);
    }
  }

  private String readValueFromInputElementAsString(
      final WebElement webElement, final String executionId) {
    try {
      final String inputType = normalizeToLowerOrEmpty(webElement.getAttribute("type"));
      if ("checkbox".equals(inputType) || "radio".equals(inputType)) {
        return convertBooleanToString(webElement.isSelected());
      }
      return getFirstNonBlankString(
          webElement.getAttribute("value"),
          webElement.getText(),
          webElement.getAttribute("textContent"));
    } catch (final Exception exception) {
      final String msg = "failed to read element value, unable to read input element";
      log.error("executionId: {}, message: {}", executionId, msg, exception);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, msg);
    }
  }

  private String readValueFromTextAreaElementAsString(
      final WebElement webElement, final String executionId) {
    try {
      return getFirstNonBlankString(
          webElement.getAttribute("value"),
          webElement.getText(),
          webElement.getAttribute("textContent"));
    } catch (final Exception exception) {
      final String msg = "failed to read element value, unable to read textarea element";
      log.error("executionId: {}, message: {}", executionId, msg, exception);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, msg);
    }
  }

  private String readValueFromSelectElementAsString(
      final WebElement webElement, final String executionId) {
    try {
      final Select select = new Select(webElement);
      final WebElement selected = select.getFirstSelectedOption();
      if (selected == null) {
        final String msg =
            "failed to read element value, no selected option found in select element";
        log.error("executionId: {}, message: {}", executionId, msg);
        throw new TestCaseStepExecutionFailException(
            BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, msg);
      }
      return getFirstNonBlankString(
          selected.getText(), selected.getAttribute("value"), selected.getAttribute("textContent"));
    } catch (final Exception exception) {
      if (exception instanceof TestCaseStepExecutionFailException) {
        throw (TestCaseStepExecutionFailException) exception;
      }
      final String msg = "failed to read element value, unable to read select element";
      log.error("executionId: {}, message: {}", executionId, msg, exception);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, msg);
    }
  }

  private String readValueFromImageElementAsString(
      final WebElement webElement, final String executionId) {
    try {
      final String src = webElement.getAttribute("src");
      if (isBlank(src)) {
        final String msg = "failed to read element value, image src attribute is missing";
        log.error("executionId: {}, message: {}", executionId, msg);
        throw new TestCaseStepExecutionFailException(
            BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, msg);
      }
      return src.trim();
    } catch (final Exception exception) {
      if (exception instanceof TestCaseStepExecutionFailException) {
        throw (TestCaseStepExecutionFailException) exception;
      }
      final String msg = "failed to read element value, unable to read image element";
      log.error("executionId: {}, message: {}", executionId, msg, exception);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, msg);
    }
  }

  private String readValueFromAnchorElementAsString(
      final WebElement webElement, final String executionId) {
    try {
      // Empty is allowed (anchor text can be empty), but href missing might be real issue depending
      // on your need.
      // Here we allow empty overall, because sometimes only text is relevant.
      return getFirstNonBlankString(
          webElement.getText(),
          webElement.getAttribute("href"),
          webElement.getAttribute("textContent"));
    } catch (final Exception exception) {
      final String msg = "failed to read element value, unable to read anchor element";
      log.error("executionId: {}, message: {}", executionId, msg, exception);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, msg);
    }
  }

  private String readValueFromTextBasedElementAsString(
      final WebElement webElement, final String executionId) {
    try {
      return getFirstNonBlankString(
          webElement.getText(),
          webElement.getAttribute("value"),
          webElement.getAttribute("textContent"));
    } catch (final Exception exception) {
      final String msg = "failed to read element value, unable to read text based element";
      log.error("executionId: {}, message: {}", executionId, msg, exception);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, msg);
    }
  }

  private String readValueFromContentEditableElementAsString(
      final WebElement webElement, final String executionId) {
    try {
      return getFirstNonBlankString(
          webElement.getAttribute("innerText"),
          webElement.getAttribute("textContent"),
          webElement.getText());
    } catch (final Exception exception) {
      final String msg = "failed to read element value, unable to read contenteditable element";
      log.error("executionId: {}, message: {}", executionId, msg, exception);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, msg);
    }
  }

  private boolean isContentEditable(final WebElement webElement) {
    final String ce = normalizeToLowerOrEmpty(webElement.getAttribute("contenteditable"));
    return "true".equals(ce);
  }

  private String convertBooleanToString(final boolean booleanValue) {
    return booleanValue ? "true" : "false";
  }

  private String normalizeToLowerOrEmpty(final String value) {
    return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
  }

  private String getFirstNonBlankString(final String... candidates) {
    if (candidates == null) {
      return "";
    }
    for (final String s : candidates) {
      if (s != null) {
        final String t = s.trim();
        if (!t.isEmpty()) {
          return t;
        }
      }
    }
    return "";
  }

  private boolean isBlank(final String value) {
    return value == null || value.trim().isEmpty();
  }

  private String safe(final String value) {
    return value == null ? "" : value;
  }
}
