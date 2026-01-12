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
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_STRING_CACHE_MAP;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEMS;

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
                    extractedPreferenceParameters, TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEMS.getParameterName());
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
