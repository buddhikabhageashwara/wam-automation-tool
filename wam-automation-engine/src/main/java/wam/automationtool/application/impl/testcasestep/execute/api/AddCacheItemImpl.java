package wam.automationtool.application.impl.testcasestep.execute.api;

import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_STRING_CACHE_MAP_KEY;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_STRING_CACHE_MAP_VALUE;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_KEY;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_VALUE;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class AddCacheItemImpl extends TestCaseStepExecutorBase implements TestCaseStepExecutor {

  @Override
  public TestCaseStepType getTestCaseStepType() {
    return TestCaseStepType.A_ADD_CACHE_ITEM;
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
        extractAndValidatePreferenceParameters(testCaseStepExecuteRequestDto);
    putExtractedParametersToResult(resultParameters, extractedPreferenceParameters);
    final String executionId = getExecutionId(testCaseStepExecuteRequestDto);
    final CacheDataDto cacheDataDto = getCacheDataDto(executionId);
    final Map<String, String> updatedStringCacheMap =
        buildUpdatedStringCacheMap(cacheDataDto, extractedPreferenceParameters);
    persistCache(executionId, cacheDataDto, updatedStringCacheMap);
  }

  /**
   * Extracts preference parameters from the request and validates the required parameter types
   * exist.
   *
   * @param testCaseStepExecuteRequestDto request DTO
   * @return extracted preference parameters map
   */
  private Map<String, String> extractAndValidatePreferenceParameters(
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
    final Map<String, String> extractedPreferenceParameters =
        extractPreferenceParameters(testCaseStepExecuteRequestDto);
    validateRequiredPreferenceParameter(
        extractedPreferenceParameters, TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_KEY.getParameterName());
    validateRequiredPreferenceParameter(
        extractedPreferenceParameters, TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_VALUE.getParameterName());
    return extractedPreferenceParameters;
  }

  /**
   * Validates that the given preference parameter type exists in the extracted preference map.
   *
   * @param extractedPreferenceParameters extracted preference parameters
   * @param preferenceParameterType required parameter type to validate
   */
  private void validateRequiredPreferenceParameter(
      final Map<String, String> extractedPreferenceParameters,
      final String preferenceParameterType) {

    log.debug("Validating required preference parameter type: {}", preferenceParameterType);
    getAndValidateTCSPreferenceParameterTypeExistence(
        extractedPreferenceParameters, preferenceParameterType);
  }

  /**
   * Stores extracted preference parameters into the result parameters map.
   *
   * @param resultParameters output map
   * @param extractedPreferenceParameters extracted preference parameters
   */
  private void putExtractedParametersToResult(
      final LinkedHashMap<String, String> resultParameters,
      final Map<String, String> extractedPreferenceParameters) {
    resultParameters.put(
            TCS_RESULT_STRING_CACHE_MAP_KEY,
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_KEY.getParameterName()));
    resultParameters.put(
            TCS_RESULT_STRING_CACHE_MAP_VALUE,
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_VALUE.getParameterName()));
  }

  /**
   * Extracts the executionId from the request DTO.
   *
   * @param testCaseStepExecuteRequestDto request DTO
   * @return execution id
   */
  private String getExecutionId(final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
    final String executionId = testCaseStepExecuteRequestDto.getExecutionId();
    log.debug("Execution id resolved: {}", executionId);
    return executionId;
  }

  /**
   * Builds the updated string cache map by reading the existing map (or initializing if needed) and
   * inserting the extracted key/value.
   *
   * @param cacheDataDto cache data DTO
   * @param extractedPreferenceParameters extracted preference parameters
   * @return updated string cache map
   */
  private Map<String, String> buildUpdatedStringCacheMap(
      final CacheDataDto cacheDataDto, final Map<String, String> extractedPreferenceParameters) {
    Map<String, String> stringCacheMap = cacheDataDto.getStringCacheMap();
    if (Objects.isNull(stringCacheMap) || stringCacheMap.isEmpty()) {
      stringCacheMap = new HashMap<>();
    }
    stringCacheMap.put(
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_KEY.getParameterName()),
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_VALUE.getParameterName()));
    return stringCacheMap;
  }

  /**
   * Persists the updated cache data into WAM cache.
   *
   * @param executionId execution id
   * @param cacheDataDto cache data DTO
   * @param updatedStringCacheMap updated string cache map
   */
  private void persistCache(
      final String executionId,
      final CacheDataDto cacheDataDto,
      final Map<String, String> updatedStringCacheMap) {
    cacheDataDto.setStringCacheMap(updatedStringCacheMap);
    getWamCacheManager().addToCache(executionId, cacheDataDto);
  }
}
