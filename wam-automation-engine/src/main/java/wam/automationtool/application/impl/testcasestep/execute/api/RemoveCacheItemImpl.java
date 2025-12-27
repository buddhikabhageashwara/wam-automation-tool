package wam.automationtool.application.impl.testcasestep.execute.api;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_STEP_EXECUTION_FAIL_CODE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepPreferenceParameterTypeConstant.TCS_PREFERENCE_PARAMETER_TYPE_STRING_CACHE_MAP;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_STRING_CACHE_MAP;

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
public class RemoveCacheItemImpl extends TestCaseStepExecutorBase implements TestCaseStepExecutor {

  @Override
  public TestCaseStepType getTestCaseStepType() {
    return TestCaseStepType.A_REMOVE_CACHE_ITEM;
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
    final String stringCacheMapTCSPreferenceParameterType =
            getAndValidateTCSPreferenceParameterTypeExistence(
                    extractedPreferenceParameters, TCS_PREFERENCE_PARAMETER_TYPE_STRING_CACHE_MAP);
    resultParameters.put(
            TCS_RESULT_STRING_CACHE_MAP,
            stringCacheMapTCSPreferenceParameterType);
    final String executionId = testCaseStepExecuteRequestDto.getExecutionId();
    final CacheDataDto cacheDataDto = getAndValidateCacheDataDto(executionId);
    final Map<String, String> stringCacheMap =
        getAndValidateStringCacheMap(cacheDataDto, executionId);
    removeAndValidateStringCacheMapItem(
        stringCacheMap, stringCacheMapTCSPreferenceParameterType);
    updateCacheWithUpdatedStringCacheMap(executionId, cacheDataDto, stringCacheMap);
  }

  /**
   * Extracts and validates the string cache map inside the given {@link CacheDataDto}.
   *
   * <p>Throws {@link TestCaseStepExecutionFailException} if the map is missing or empty.
   *
   * @param cacheDataDto cache dto containing the string cache map
   * @param executionId execution id used only for error message context
   * @return non-null, non-empty string cache map
   */
  private Map<String, String> getAndValidateStringCacheMap(
      final CacheDataDto cacheDataDto, final String executionId) {
    final Map<String, String> stringCacheMap = cacheDataDto.getStringCacheMap();
    if (Objects.isNull(stringCacheMap) || stringCacheMap.isEmpty()) {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "String cache map is not found in cache for executionId: " + executionId);
    }
    return stringCacheMap;
  }

  /**
   * Removes and validates the presence of an item from the given string cache map by key.
   *
   * <p>Throws {@link TestCaseStepExecutionFailException} if no value exists for the given key.
   *
   * @param stringCacheMap string cache map to remove from
   * @param key key used for removal
   * @return removed value
   */
  private String removeAndValidateStringCacheMapItem(
      final Map<String, String> stringCacheMap, final String key) {
    final String removedValue = stringCacheMap.remove(key);
    if (Objects.isNull(removedValue)) {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "String cache map item is not found for key: " + key);
    }
    return removedValue;
  }

  /**
   * Updates the cache with the modified string cache map by setting it back into {@link
   * CacheDataDto} and persisting the dto using {@code addToCache}.
   *
   * @param executionId execution id used as cache key
   * @param cacheDataDto cache dto to update
   * @param updatedStringCacheMap updated string cache map to set
   */
  private void updateCacheWithUpdatedStringCacheMap(
      final String executionId,
      final CacheDataDto cacheDataDto,
      final Map<String, String> updatedStringCacheMap) {
    cacheDataDto.setStringCacheMap(updatedStringCacheMap);
    getWamCacheManager().addToCache(executionId, cacheDataDto);
  }
}
