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

package wam.automationtool.application.impl.testcasestep.execute.api;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_STEP_EXECUTION_FAIL_CODE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepAssertParameterTypeConstant.TCS_ASSERT_PARAMETER_TYPE_LOG_READ;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ACTION_REGEX;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_EXCLUDE_REGEX;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_INCLUDE_REGEX;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_INVERT_RESULT;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_LOG_FILE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_LOG_FILE_LOCATION;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_LOG_READ_ASSERT_VALUE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_REGEX_GROUP_INDEX_NUMBER;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_TEMP_LOG_FILE_NAME;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepAliasParameterType.ALIAS_PARAMETER_TYPE_LOG_FILE_LOCATION;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_ACTION_REGEX;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_EXCLUDE_REGEX;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_INCLUDE_REGEX;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_INVERT_RESULT;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_REGEX_GROUP_INDEX_NUMBER;
import static wam.automationtool.application.util.LogTailerUtil.getTempFilePath;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.alias.AliasParameterDto;
import wam.automationtool.application.dto.execute.ActualAndExpectedResultDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;
import wam.automationtool.application.exception.TestCaseStepExecutionFailException;
import wam.automationtool.application.impl.testcasestep.execute.TestCaseStepExecutor;
import wam.automationtool.application.impl.testcasestep.execute.TestCaseStepExecutorBase;
import wam.automationtool.application.util.AgentRequestManager;
import wam.automationtool.application.util.AliasManager;
import wam.automationtool.application.util.DateTimeManager;
import wam.automationtool.application.util.LogRegexMatcherUtil;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepExecutionStatus;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogFileLineReadImpl extends TestCaseStepExecutorBase implements TestCaseStepExecutor {

  @Override
  public TestCaseStepType getTestCaseStepType() {
    return TestCaseStepType.A_LOG_FILE_LINE_READ;
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
    try {
      final Map<String, String> extractedPreferenceParameters =
          extractPreferenceParameters(testCaseStepExecuteRequestDto);
      validateMandatoryPreferenceParameters(extractedPreferenceParameters);
      final Map<String, String> extractedAssertParameters =
          extractAssertParameters(testCaseStepExecuteRequestDto);
      validateMandatoryAssertParameters(extractedAssertParameters);
      putBaseResultParameters(
          resultParameters, extractedPreferenceParameters, extractedAssertParameters);
      final String logFileLocation = resolveLogFileLocation(testCaseStepExecuteRequestDto);
      throwIfLogFileLocationMissing(logFileLocation);
      resultParameters.put(TCS_RESULT_LOG_FILE_LOCATION, logFileLocation);
      final String tempLogFileName =
          buildTempLogFileName(extractedPreferenceParameters, testCaseStepExecuteRequestDto);
      final Path tempLogFilePath = getTempFilePath(tempLogFileName);
      final LogRegexMatcherUtil.LogRegexResult logRegexResult =
          executeLogRegexValidation(
              tempLogFilePath, extractedPreferenceParameters, extractedAssertParameters);
      throwIfExpectedLogNotFound(
          logRegexResult,
          testCaseStepExecuteRequestDto,
          extractedPreferenceParameters,
          extractedAssertParameters,
          tempLogFileName);
      resultParameters.put(TCS_RESULT_TEMP_LOG_FILE_NAME, tempLogFileName);
      log.info(
          "Log read assertion passed, executionId: {}, tempLogFileName: {}, logFileLocation: {}",
          testCaseStepExecuteRequestDto.getExecutionId(),
          tempLogFileName,
          logFileLocation);
    } catch (final Exception exception) {
      log.warn(
          "Log read assertion failed, executionId: {}, message: {}",
          testCaseStepExecuteRequestDto.getExecutionId(),
          exception.getMessage());
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "Expected log reading result is not found");
    }
  }

  /**
   * Validates required preference parameters existence for this step.
   *
   * @param extractedPreferenceParameters extracted preference parameters
   */
  private void validateMandatoryPreferenceParameters(
      final Map<String, String> extractedPreferenceParameters) {
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters, TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE.getParameterName());
  }

  /**
   * Validates required assert parameters existence for this step.
   *
   * @param extractedAssertParameters extracted assert parameters
   */
  private void validateMandatoryAssertParameters(
      final Map<String, String> extractedAssertParameters) {
    getAndValidateAssertParameterTypeExistence(
        extractedAssertParameters, TCS_ASSERT_PARAMETER_TYPE_LOG_READ);
  }

  /**
   * Writes commonly used values into {@code resultParameters} for reporting/debug visibility.
   *
   * @param resultParameters output parameter map
   * @param extractedPreferenceParameters extracted preference parameters
   * @param extractedAssertParameters extracted assert parameters
   */
  private void putBaseResultParameters(
      final LinkedHashMap<String, String> resultParameters,
      final Map<String, String> extractedPreferenceParameters,
      final Map<String, String> extractedAssertParameters) {
    resultParameters.put(
        TCS_RESULT_LOG_FILE,
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE.getParameterName()));
    resultParameters.put(
        TCS_RESULT_INCLUDE_REGEX,
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_INCLUDE_REGEX.getParameterName()));
    resultParameters.put(
        TCS_RESULT_EXCLUDE_REGEX,
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_EXCLUDE_REGEX.getParameterName()));
    resultParameters.put(
        TCS_RESULT_ACTION_REGEX,
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_ACTION_REGEX.getParameterName()));
    resultParameters.put(
        TCS_RESULT_REGEX_GROUP_INDEX_NUMBER,
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_REGEX_GROUP_INDEX_NUMBER.getParameterName()));
    resultParameters.put(
        TCS_RESULT_LOG_READ_ASSERT_VALUE,
        extractedAssertParameters.get(TCS_ASSERT_PARAMETER_TYPE_LOG_READ));
    resultParameters.put(
        TCS_RESULT_INVERT_RESULT,
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_INVERT_RESULT.getParameterName()));
  }

  /**
   * Resolves log file location from alias parameters.
   *
   * @param request execution request DTO
   * @return resolved log file location or null (based on existing behavior)
   */
  private String resolveLogFileLocation(final TestCaseStepExecuteRequestDto request) {
    final List<AliasParameterDto> aliasParameterDtoList =
        AliasManager.getAliasParametersForAlias(
            request, TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE.getParameterName());
    return Optional.ofNullable(aliasParameterDtoList).orElseGet(Collections::emptyList).stream()
        .filter(aliasParameterDto -> Objects.nonNull(aliasParameterDto))
        .filter(
            aliasParameterDto ->
                ALIAS_PARAMETER_TYPE_LOG_FILE_LOCATION
                    .getParameterName()
                    .equals(aliasParameterDto.getParameterName()))
        .map(AliasParameterDto::getParameterValue)
        .findFirst()
        .orElse(null);
  }

  /**
   * Throws if log file location is missing.
   *
   * @param logFileLocation resolved log file location
   */
  private void throwIfLogFileLocationMissing(final String logFileLocation) {
    if (Objects.nonNull(logFileLocation)) {
      return;
    }
    log.warn("Failed to start log reading, reason: log file location not found");
    throw new TestCaseStepExecutionFailException(
        BAD_REQUEST,
        TEST_CASE_STEP_EXECUTION_FAIL_CODE,
        "Failed to start log reading due to log file location not found");
  }

  /**
   * Builds the temp log file name using executionId, testCaseId and alias name.
   *
   * @param extractedPreferenceParameters extracted preference parameters
   * @param request execution request DTO
   * @return temp log file name
   */
  private String buildTempLogFileName(
      final Map<String, String> extractedPreferenceParameters,
      final TestCaseStepExecuteRequestDto request) {
    return "EXECUTION_ID_TC_ID_ALIAS_NAME_"
        + request.getExecutionId()
        + "_"
        + request.getTestCaseStepDto().getTestCaseId()
        + "_"
        + extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE.getParameterName());
  }

  /**
   * Executes log regex validation against the temp log file.
   *
   * @param tempLogFilePath temp log file path
   * @param extractedPreferenceParameters extracted preference parameters
   * @param extractedAssertParameters extracted assert parameters
   * @return regex evaluation result
   */
  private LogRegexMatcherUtil.LogRegexResult executeLogRegexValidation(
      final Path tempLogFilePath,
      final Map<String, String> extractedPreferenceParameters,
      final Map<String, String> extractedAssertParameters) {
    final String include =
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_INCLUDE_REGEX.getParameterName());
    final String exclude =
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_EXCLUDE_REGEX.getParameterName());
    final String regex =
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_ACTION_REGEX.getParameterName());
    final int regexGroupNumber =
        Integer.parseInt(
            extractedPreferenceParameters.get(
                TCS_PREFERENCE_PARAMETER_TYPE_REGEX_GROUP_INDEX_NUMBER.getParameterName()));
    final boolean invertResult =
        Boolean.parseBoolean(
            extractedPreferenceParameters.get(
                TCS_PREFERENCE_PARAMETER_TYPE_INVERT_RESULT.getParameterName()));
    return LogRegexMatcherUtil.findAndValidate(
        tempLogFilePath,
        include,
        exclude,
        regex,
        regexGroupNumber,
        extractedAssertParameters.get(TCS_ASSERT_PARAMETER_TYPE_LOG_READ),
        invertResult);
  }

  /**
   * Throws if the expected log reading result is not found based on matcher result.
   *
   * @param logRegexResult regex result returned by matcher utility
   * @param request execution request DTO
   * @param extractedPreferenceParameters extracted preference parameters
   * @param extractedAssertParameters extracted assert parameters
   * @param tempLogFileName temp log file name
   */
  private void throwIfExpectedLogNotFound(
      final LogRegexMatcherUtil.LogRegexResult logRegexResult,
      final TestCaseStepExecuteRequestDto request,
      final Map<String, String> extractedPreferenceParameters,
      final Map<String, String> extractedAssertParameters,
      final String tempLogFileName) {
    if (!logRegexResult.matched()) {
      return;
    }
    log.warn(
        "Expected log reading result not found, executionId: {}, logFile: {},"
            + " tempLogFileName: {}, includeRegex: {}, excludeRegex: {}, "
            + " actionRegex: {}, regexGroupIndexNumber: {}, invertResult: {}, expectedAssertValue: {}",
        request.getExecutionId(),
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE.getParameterName()),
        tempLogFileName,
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_INCLUDE_REGEX.getParameterName()),
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_EXCLUDE_REGEX.getParameterName()),
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_ACTION_REGEX.getParameterName()),
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_REGEX_GROUP_INDEX_NUMBER.getParameterName()),
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_INVERT_RESULT.getParameterName()),
        extractedAssertParameters.get(TCS_ASSERT_PARAMETER_TYPE_LOG_READ));
    throw new TestCaseStepExecutionFailException(
        BAD_REQUEST,
        TEST_CASE_STEP_EXECUTION_FAIL_CODE,
        "Expected log reading result is not found");
  }
}
