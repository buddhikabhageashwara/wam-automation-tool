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
import static wam.automationtool.application.config.AppConstant.TestCaseStepAssertParameterTypeConstant.TCS_ASSERT_PARAMETER_TYPE_CACHE_ITEM_READ;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_CACHE_VALUE_EXPECTED_VALUE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_INPUT_CACHE_VALUE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_INCLUDE_REGEX;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_INVERT_RESULT;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_REGEX_GROUP_INDEX_NUMBER;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_STRING_CACHE_MAP;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_INCLUDE_REGEX;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_INVERT_RESULT;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_REGEX_GROUP_INDEX_NUMBER;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEMS;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_KEY;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_VALUE;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import wam.automationtool.application.util.DateTimeManager;
import wam.automationtool.application.util.RegexAssertUtil;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepExecutionStatus;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerifyCacheItemImpl extends TestCaseStepExecutorBase implements TestCaseStepExecutor {

  @Override
  public TestCaseStepType getTestCaseStepType() {
    return TestCaseStepType.A_VERIFY_CACHE_ITEM;
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
    final Map<String, String> extractedAssertParameters =
        extractAssertParameters(testCaseStepExecuteRequestDto);
    final String cacheKey = resolveCacheKey(extractedPreferenceParameters);
    putBaseResultParameters(
        resultParameters, extractedPreferenceParameters, extractedAssertParameters, cacheKey);
    final String cachedValue =
        resolveCachedValueOrThrow(resultParameters, testCaseStepExecuteRequestDto, cacheKey);
    final int regexGroupIndex = resolveRegexGroupIndex(extractedPreferenceParameters);
    final String expectedRegex = resolveExpectedRegex(extractedAssertParameters);
    final boolean invertResult = resolveInvertResult(extractedPreferenceParameters);
    final boolean matched =
        RegexAssertUtil.assertByRegexGroup(
            regexGroupIndex, cachedValue, expectedRegex, invertResult);
    throwIfNotMatched(
        matched,
        testCaseStepExecuteRequestDto,
        cacheKey,
        regexGroupIndex,
        invertResult,
        expectedRegex,
        cachedValue);
    log.info(
        "Regex assertion passed, executionId: {}, cacheKey: {},"
            + " regexGroupIndex: {}, invertResult: {}, expectedRegex: {}",
        testCaseStepExecuteRequestDto.getExecutionId(),
        cacheKey,
        regexGroupIndex,
        invertResult,
        expectedRegex);
  }

  /**
   * Resolves and validates the cache-key preference parameter.
   *
   * @param preferenceParameters extracted preference parameters
   * @return cache key to read from string cache map
   */
  private String resolveCacheKey(final Map<String, String> preferenceParameters) {
    return getAndValidateTCSPreferenceParameterTypeExistence(
        preferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEMS.getParameterName());
  }

  /**
   * Writes commonly used values into {@code resultParameters} for reporting/debug visibility.
   *
   * @param resultParameters output parameter map
   * @param extractedPreferenceParameters extracted preference parameters
   * @param extractedAssertParameters extracted assert parameters
   * @param cacheKey resolved cache key
   */
  private void putBaseResultParameters(
      final LinkedHashMap<String, String> resultParameters,
      final Map<String, String> extractedPreferenceParameters,
      final Map<String, String> extractedAssertParameters,
      final String cacheKey) {
    resultParameters.put(TCS_RESULT_STRING_CACHE_MAP, cacheKey);
    resultParameters.put(
        TCS_RESULT_INCLUDE_REGEX,
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_INCLUDE_REGEX.getParameterName()));
    resultParameters.put(
        TCS_RESULT_REGEX_GROUP_INDEX_NUMBER,
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_REGEX_GROUP_INDEX_NUMBER.getParameterName()));
    resultParameters.put(
        TCS_RESULT_INVERT_RESULT,
        extractedPreferenceParameters.get(
            TCS_PREFERENCE_PARAMETER_TYPE_INVERT_RESULT.getParameterName()));
    resultParameters.put(
        TCS_RESULT_CACHE_VALUE_EXPECTED_VALUE,
        extractedAssertParameters.get(TCS_ASSERT_PARAMETER_TYPE_CACHE_ITEM_READ));
  }

  /**
   * Reads the cached value from the string cache map by {@code cacheKey}. Stores the cached value
   * into {@code resultParameters}.
   *
   * <p>Throws {@link TestCaseStepExecutionFailException} if cached value is not found.
   *
   * @param resultParameters output parameter map
   * @param request execution request
   * @param cacheKey cache key to read
   * @return cached value
   */
  private String resolveCachedValueOrThrow(
      final LinkedHashMap<String, String> resultParameters,
      final TestCaseStepExecuteRequestDto request,
      final String cacheKey) {
    final Map<String, String> stringCacheMap = getStringCacheMap(request);
    final String cachedValue = stringCacheMap.get(cacheKey);
    resultParameters.put(TCS_RESULT_ELEMENT_INPUT_CACHE_VALUE, cachedValue);
    if (Objects.isNull(cachedValue)) {
      log.warn(
          "No cached value found, executionId: {}, cacheKey: {}",
          request.getExecutionId(),
          cacheKey);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "No cached value found for key: "
              + cacheKey
              + " (executionId: "
              + request.getExecutionId()
              + ")");
    }
    return cachedValue;
  }

  /**
   * Resolves regex group index from preference parameters.
   *
   * @param extractedPreferenceParameters extracted preference parameters
   * @return regex group index
   */
  private int resolveRegexGroupIndex(final Map<String, String> extractedPreferenceParameters) {
    return Integer.parseInt(
        extractedPreferenceParameters.get(
            TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_ACTION_REGEX
                .getParameterName()));
  }

  /**
   * Resolves expected regex from assert parameters.
   *
   * @param extractedAssertParameters extracted assert parameters
   * @return expected regex
   */
  private String resolveExpectedRegex(final Map<String, String> extractedAssertParameters) {
    return extractedAssertParameters.get(TCS_ASSERT_PARAMETER_TYPE_CACHE_ITEM_READ);
  }

  /**
   * Resolves invert-result flag from preference parameters.
   *
   * @param extractedPreferenceParameters extracted preference parameters
   * @return invert-result flag
   */
  private boolean resolveInvertResult(final Map<String, String> extractedPreferenceParameters) {
    return Boolean.parseBoolean(
        extractedPreferenceParameters.get(
            TestCaseStepPreferenceParameterType.TCS_PREFERENCE_PARAMETER_TYPE_INVERT_RESULT
                .getParameterName()));
  }

  /**
   * Throws {@link TestCaseStepExecutionFailException} if the match result is false.
   *
   * @param matched match result
   * @param request execution request
   * @param cacheKey cache key used
   * @param regexGroupIndex regex group index used
   * @param invertResult invert flag used
   * @param expectedRegex expected regex used
   * @param cachedValue cached value tested
   */
  private void throwIfNotMatched(
      final boolean matched,
      final TestCaseStepExecuteRequestDto request,
      final String cacheKey,
      final int regexGroupIndex,
      final boolean invertResult,
      final String expectedRegex,
      final String cachedValue) {
    if (matched) {
      return;
    }
    log.warn(
        "Regex assertion failed, executionId: {}, cacheKey: {},"
            + " regexGroupIndex: {}, invertResult: {}, expectedRegex: {}, cachedValue: {}",
        request.getExecutionId(),
        cacheKey,
        regexGroupIndex,
        invertResult,
        expectedRegex,
        cachedValue);
    throw new TestCaseStepExecutionFailException(
        BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, "Not matched with the expected value");
  }

  private Map<String, String> getStringCacheMap(
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
    final CacheDataDto cacheDataDto =
        getAndValidateCacheDataDto(testCaseStepExecuteRequestDto.getExecutionId());
    return getAndValidateStringCacheMap(
        cacheDataDto, testCaseStepExecuteRequestDto.getExecutionId());
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
        extractedPreferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_KEY.getParameterName());
    validateRequiredPreferenceParameter(
        extractedPreferenceParameters,
        TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_VALUE.getParameterName());
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
}
