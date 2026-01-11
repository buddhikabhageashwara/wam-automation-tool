package wam.automationtool.application.impl.testcasestep.execute.api;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_STEP_EXECUTION_FAIL_CODE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepAssertParameterTypeConstant.TCS_ASSERT_PARAMETER_TYPE_LOG_READ;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.*;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepAliasParameterType.ALIAS_PARAMETER_TYPE_LOG_FILE_LOCATION;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.*;
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
            getAndValidateTCSPreferenceParameterTypeExistence(
                    extractedPreferenceParameters, TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE.getParameterName());

            final Map<String, String> extractedAssertParameters =
                    extractAssertParameters(testCaseStepExecuteRequestDto);
            getAndValidateAssertParameterTypeExistence(
                    extractedAssertParameters, TCS_ASSERT_PARAMETER_TYPE_LOG_READ);

            resultParameters.put(
                    TCS_RESULT_LOG_FILE,
                    extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE.getParameterName()));
            resultParameters.put(
                    TCS_RESULT_INCLUDE_REGEX,
                    extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_INCLUDE_REGEX.getParameterName()));
            resultParameters.put(
                    TCS_RESULT_EXCLUDE_REGEX,
                    extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_EXCLUDE_REGEX.getParameterName()));
            resultParameters.put(
                    TCS_RESULT_ACTION_REGEX,
                    extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_ACTION_REGEX.getParameterName()));
            resultParameters.put(
                    TCS_RESULT_REGEX_GROUP_INDEX_NUMBER,
                    extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_REGEX_GROUP_INDEX_NUMBER.getParameterName()));
            resultParameters.put(
                    TCS_RESULT_LOG_READ_ASSERT_VALUE,
                    extractedAssertParameters.get(TCS_ASSERT_PARAMETER_TYPE_LOG_READ));

            final List<AliasParameterDto> aliasParameterDtoList =
                    AliasManager.getAliasParametersForAlias(testCaseStepExecuteRequestDto,
                            TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE.getParameterName());
            final String logFileLocation = Optional.ofNullable(aliasParameterDtoList)
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .filter(aliasParameterDto -> Objects.nonNull(aliasParameterDto))
                    .filter(aliasParameterDto ->
                            ALIAS_PARAMETER_TYPE_LOG_FILE_LOCATION.getParameterName().equals(
                                    aliasParameterDto.getParameterName()))
                    .map(AliasParameterDto::getParameterValue)
                    .findFirst()
                    .orElse(null);
            if (Objects.isNull(logFileLocation)) {
                throw new TestCaseStepExecutionFailException(
                        BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE,
                        "Failed to start log reading due to log file location not found");
            } else {
                resultParameters.put(TCS_RESULT_LOG_FILE_LOCATION, logFileLocation);
                final String tempLogFileName  = "EXECUTION_ID_TC_ID_ALIAS_NAME_" +
                        testCaseStepExecuteRequestDto.getExecutionId() + "_" +
                        testCaseStepExecuteRequestDto.getTestCaseStepDto().getTestCaseId() + "_" +
                        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE.getParameterName());
                final String include = extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_INCLUDE_REGEX.getParameterName());
                final String exclude = extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_EXCLUDE_REGEX.getParameterName());
                final String regex = extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_ACTION_REGEX.getParameterName());
                final int regexGroupNumber = Integer.parseInt(
                        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_REGEX_GROUP_INDEX_NUMBER.getParameterName()));
                final boolean invertResult = Boolean.parseBoolean(
                        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_INVERT_RESULT.getParameterName()));
                final Path tempLogFilePath = getTempFilePath(tempLogFileName);
                final LogRegexMatcherUtil.LogRegexResult logRegexResult =
                        LogRegexMatcherUtil.findAndValidate(
                                tempLogFilePath,
                                include,
                                exclude,
                                regex,
                                regexGroupNumber,
                                extractedAssertParameters.get(TCS_ASSERT_PARAMETER_TYPE_LOG_READ),
                                invertResult);
                if (logRegexResult.matched()) {
                    throw new TestCaseStepExecutionFailException(
                            BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE,
                            "Expected log reading result is not found");
                }
                resultParameters.put(TCS_RESULT_TEMP_LOG_FILE_NAME, tempLogFileName);
            }
        } catch (final Exception exception) {
            throw new TestCaseStepExecutionFailException(
                    BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, "Expected log reading result is not found");
        }
    }
}
