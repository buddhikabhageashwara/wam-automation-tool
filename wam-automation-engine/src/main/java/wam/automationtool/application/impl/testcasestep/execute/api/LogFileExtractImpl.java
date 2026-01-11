package wam.automationtool.application.impl.testcasestep.execute.api;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_STEP_EXECUTION_FAIL_CODE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_LOG_FILE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_LOG_FILE_LOCATION;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_TEMP_LOG_FILE_NAME;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepAliasParameterType.ALIAS_PARAMETER_TYPE_LOG_FILE_LOCATION;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE;

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
import wam.automationtool.application.dto.testcasestep.LogFileBase64Dto;
import wam.automationtool.application.exception.TestCaseStepExecutionFailException;
import wam.automationtool.application.impl.testcasestep.execute.TestCaseStepExecutor;
import wam.automationtool.application.impl.testcasestep.execute.TestCaseStepExecutorBase;
import wam.automationtool.application.util.AgentRequestManager;
import wam.automationtool.application.util.AliasManager;
import wam.automationtool.application.util.DateTimeManager;
import wam.automationtool.application.util.LogTailerUtil;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepExecutionStatus;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogFileExtractImpl extends TestCaseStepExecutorBase implements TestCaseStepExecutor {

  @Override
  public TestCaseStepType getTestCaseStepType() {
    return TestCaseStepType.A_LOG_FILE_EXTRACT;
  }

    @Override
    public TestCaseStepExecuteResponseDto execute(
            final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
        LogFileBase64Dto logFileBase64Dto = null;
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
                logFileBase64Dto = start(resultParameters, testCaseStepExecuteRequestDto);
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
        return buildResponse(status, actualAndExpectedResult, startTime, endTime, logFileBase64Dto);
    }

    private LogFileBase64Dto start(
            final LinkedHashMap<String, String> resultParameters,
            final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
        LogFileBase64Dto logFileBase64Dto = null;
        try {
            final Map<String, String> extractedPreferenceParameters =
                    extractPreferenceParameters(testCaseStepExecuteRequestDto);
            getAndValidateTCSPreferenceParameterTypeExistence(
                    extractedPreferenceParameters, TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE.getParameterName());
            resultParameters.put(
                    TCS_RESULT_LOG_FILE,
                    extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE.getParameterName()));
            final List<AliasParameterDto> aliasParameterDtoList =
                    AliasManager.getAliasParametersForAlias(testCaseStepExecuteRequestDto,
                            TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE.getParameterName());
            final String logFileLocation = Optional.ofNullable(aliasParameterDtoList)
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .filter(aliasParameterDto -> Objects.nonNull(aliasParameterDto))
                    .filter(aliasParameterDto ->
                            ALIAS_PARAMETER_TYPE_LOG_FILE_LOCATION.equals(
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
                logFileBase64Dto =
                        LogTailerUtil.getTempFileAsBase64(tempLogFileName);
                resultParameters.put(TCS_RESULT_TEMP_LOG_FILE_NAME, tempLogFileName);
            }
        } catch (final Exception exception) {
            throw new TestCaseStepExecutionFailException(
                    BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, "Failed to end log reading");
        }
        return logFileBase64Dto;
    }
}
