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
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_CSV_FILE_PATH_TO_SAVE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_INCLUDE_CSS_SELECTOR;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ONLY_INTERACTIVE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_WEB_DRIVER_CACHE_NAME;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_CSV_SAVE_LOCATION;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_INCLUDE_CSS_SELECTOR;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_ONLY_INTERACTIVE;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
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
public class BulkElementAttributeListImpl extends TestCaseStepExecutorBase
    implements TestCaseStepExecutor {

  @Override
  public TestCaseStepType getTestCaseStepType() {
    return TestCaseStepType.W_BULK_ELEMENT_ATTRIBUTE_LIST;
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
        TCS_PREFERENCE_PARAMETER_TYPE_CSV_SAVE_LOCATION.getParameterName());
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_ONLY_INTERACTIVE.getParameterName());
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_INCLUDE_CSS_SELECTOR.getParameterName());

    final String webDriverCacheName =
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME.getParameterName());
    final String csvFilePathToSave =
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_CSV_SAVE_LOCATION.getParameterName());
    final boolean isOnlyInteractive =
        Boolean.parseBoolean(
            extractedPreferenceParameters.get(
                TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_ONLY_INTERACTIVE
                    .getParameterName()));
    final boolean isIncludeCSSSelector =
        Boolean.parseBoolean(
            extractedPreferenceParameters.get(
                TestCaseStepPreferenceParameterType
                    .TCS_PREFERENCE_PARAMETER_TYPE_INCLUDE_CSS_SELECTOR
                    .getParameterName()));

    resultParameters.put(TCS_RESULT_WEB_DRIVER_CACHE_NAME, webDriverCacheName);
    resultParameters.put(TCS_RESULT_CSV_FILE_PATH_TO_SAVE, csvFilePathToSave);
    resultParameters.put(TCS_RESULT_ONLY_INTERACTIVE, String.valueOf(isOnlyInteractive));
    resultParameters.put(TCS_RESULT_INCLUDE_CSS_SELECTOR, String.valueOf(isIncludeCSSSelector));

    final WebDriver driver = getActiveWebDriverByName(webDriverCacheName);

    final Path csvPath = Path.of(csvFilePathToSave);
    ListingElementAttributes(
        driver,
        csvPath,
        testCaseStepExecuteRequestDto.getTestCaseStepDto().getExecutionOrder(),
        isOnlyInteractive,
        isIncludeCSSSelector);
  }

  private void ListingElementAttributes(
      final WebDriver driver,
      final Path csvPath,
      final long startNumber,
      final boolean onlyInteractive,
      final boolean includeCssSelector) {
    try {
      AutoLocatorDetector.exportDomToCsv(
          driver, csvPath, startNumber, onlyInteractive, includeCssSelector);
    } catch (final Exception exception) {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "error occurred while listing element attributes");
    }
  }
}
