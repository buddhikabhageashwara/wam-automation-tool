package wam.automationtool.application.impl.testcasestep.execute;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_STEP_EXECUTION_FAIL_CODE;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wam.automationtool.application.dto.cache.CacheDataDto;
import wam.automationtool.application.dto.execute.ActualAndExpectedResultDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteRequestDto;
import wam.automationtool.application.dto.execute.TestCaseStepExecuteResponseDto;
import wam.automationtool.application.dto.testcasestep.LogFileBase64Dto;
import wam.automationtool.application.exception.TestCaseStepExecutionFailException;
import wam.automationtool.application.impl.testcasestep.execute.web.OpenBrowserImpl;
import wam.automationtool.application.util.AgentRequestManager;
import wam.automationtool.application.util.TestCaseStepActualAndExpectedResultManager;
import wam.automationtool.application.util.WAMCacheManager;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

@Service
@Slf4j
public abstract class TestCaseStepExecutorBase {

  @Autowired private WAMCacheManager wamCacheManager;

  protected WAMCacheManager getWamCacheManager() {
    return wamCacheManager;
  }

  protected boolean isNotRemoteExecution(
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto, final String agentURL) {
    // Checks if the request is not for remote execution
    return testCaseStepExecuteRequestDto.isAgentRequest()
        ? testCaseStepExecuteRequestDto.isAgentRequest()
        : Objects.isNull(agentURL);
  }

  protected TestCaseStepExecuteResponseDto buildResponse(
      final String status,
      final ActualAndExpectedResultDto actualAndExpectedResult,
      final String startTime,
      final String endTime,
      final LogFileBase64Dto logFileBase64Dto) {
    // Builds and returns the response DTO
    return TestCaseStepExecuteResponseDto.builder()
        .status(status)
        .expectedResult(actualAndExpectedResult.getExpectedResult())
        .actualResult(actualAndExpectedResult.getActualResult())
        .startTime(startTime)
        .endTime(endTime)
        .logFileBase64Dto(logFileBase64Dto)
        .build();
  }

  protected ActualAndExpectedResultDto getActualAndExpectedResult(
      final LinkedHashMap<String, String> resultParameters,
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto,
      final boolean isUnknown,
      final String status,
      final String unknownReason) {
    // Retrieves the actual and expected result based on the parameters
    return TestCaseStepActualAndExpectedResultManager.getActualAndExpectedResult(
        resultParameters,
        TestCaseStepType.valueOf(testCaseStepExecuteRequestDto.getTestCaseStepType()),
        isUnknown,
        status,
        unknownReason);
  }

  protected TestCaseStepExecuteResponseDto submitToAgent(
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto, final String agentURL) {
    // Submits the request to the agent for remote execution
    return AgentRequestManager.submitToAgent(testCaseStepExecuteRequestDto, agentURL);
  }

  protected Map<String, String> extractPreferenceParameters(
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
    if (Objects.isNull(testCaseStepExecuteRequestDto)
        || Objects.isNull(testCaseStepExecuteRequestDto.getTestCaseStepDto())
        || Objects.isNull(
            testCaseStepExecuteRequestDto.getTestCaseStepDto().getPreferenceParameterDtoList())
        || testCaseStepExecuteRequestDto
            .getTestCaseStepDto()
            .getPreferenceParameterDtoList()
            .isEmpty()) {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "TCS preference parameters were not found.");
    }
    final Map<String, String> parameters = new LinkedHashMap<>();
    testCaseStepExecuteRequestDto
        .getTestCaseStepDto()
        .getPreferenceParameterDtoList()
        .forEach(
            preference -> {
              if (Objects.nonNull(preference) && Objects.nonNull(preference.getParameterName())) {
                parameters.put(preference.getParameterName(), preference.getParameterValue());
              }
            });
    if (parameters.isEmpty()) {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "TCS preference parameters were not found.");
    }
    return parameters;
  }

  protected Map<String, String> extractAssertParameters(
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
    if (Objects.isNull(testCaseStepExecuteRequestDto)
        || Objects.isNull(testCaseStepExecuteRequestDto.getTestCaseStepDto())
        || Objects.isNull(
            testCaseStepExecuteRequestDto.getTestCaseStepDto().getAssertParameterDtoList())
        || testCaseStepExecuteRequestDto
            .getTestCaseStepDto()
            .getAssertParameterDtoList()
            .isEmpty()) {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, "TCS assert parameters were not found.");
    }
    final Map<String, String> parameters = new LinkedHashMap<>();
    testCaseStepExecuteRequestDto
        .getTestCaseStepDto()
        .getAssertParameterDtoList()
        .forEach(
            assertParameterDto -> {
              if (Objects.nonNull(assertParameterDto)
                  && Objects.nonNull(assertParameterDto.getParameterName())) {
                parameters.put(
                    assertParameterDto.getParameterName(), assertParameterDto.getParameterValue());
              }
            });
    if (parameters.isEmpty()) {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, "TCS assert parameters were not found.");
    }
    return parameters;
  }

  protected WebDriver getActiveWebDriverByName(final String webDriverCacheName) {
    final WebDriver existingDriver = OpenBrowserImpl.getActiveDrivers().get(webDriverCacheName);
    if (Objects.isNull(existingDriver)) {
      log.warn("No WebDriver found for the given cache name: {}", webDriverCacheName);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, "driver not found");
    } else {
      return existingDriver;
    }
  }

  /**
   * Retrieves and validates the given TCS preference parameter value from the extracted preference
   * map.
   *
   * <p>Precondition: {@code extractedPreferenceParameters} is already validated as non-null by the
   * caller.
   *
   * <p>Throws {@link TestCaseStepExecutionFailException} if the preference parameter value is
   * missing or blank.
   *
   * @param extractedPreferenceParameters extracted preference parameters map (non-null)
   * @param tcsPreferenceParameterType preference parameter key to retrieve (e.g., "stringCacheMap")
   * @return the non-blank preference parameter value
   */
  protected String getAndValidateTCSPreferenceParameterTypeExistence(
      final Map<String, String> extractedPreferenceParameters,
      final String tcsPreferenceParameterType) {
    log.debug(
        "Validating TCS preference parameter existence | preferenceKey: {}",
        tcsPreferenceParameterType);
    final String tcsPreferenceParameterTypeValue =
        extractedPreferenceParameters.get(tcsPreferenceParameterType);
    if (Objects.isNull(tcsPreferenceParameterTypeValue)
        || tcsPreferenceParameterTypeValue.trim().isEmpty()) {
      log.error(
          "TCS preference parameter is missing or blank | preferenceKey; {}",
          tcsPreferenceParameterType);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "TCS preference parameter " + tcsPreferenceParameterType + " is not found.");
    }
    log.debug(
        "TCS preference parameter validated successfully | preferenceKey: {} | value; {}",
        tcsPreferenceParameterType,
        tcsPreferenceParameterTypeValue);
    return tcsPreferenceParameterTypeValue;
  }

  /**
   * Retrieves and validates the given assert parameter value from the extracted assert parameter
   * map.
   *
   * <p>Precondition: {@code extractedAssertParameters} is already validated as non-null by the
   * caller.
   *
   * <p>Throws {@link TestCaseStepExecutionFailException} if the assert parameter value is missing
   * or blank.
   *
   * @param extractedAssertParameters extracted assert parameters map (non-null)
   * @param assertParameterType assert parameter key to retrieve (e.g., "stringCacheMap")
   * @return the non-blank assert parameter value
   */
  protected String getAndValidateAssertParameterTypeExistence(
      final Map<String, String> extractedAssertParameters, final String assertParameterType) {
    log.debug("Validating assert parameter existence | assertKey: {}", assertParameterType);
    final String assertParameterValue = extractedAssertParameters.get(assertParameterType);
    if (Objects.isNull(assertParameterValue) || assertParameterValue.trim().isEmpty()) {
      log.error("Assert parameter is missing or blank | assertKey: {}", assertParameterType);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "Assert parameter " + assertParameterType + " is not found.");
    }
    log.debug(
        "Assert parameter validated successfully | assertKey: {} | value: {}",
        assertParameterType,
        assertParameterValue);
    return assertParameterValue;
  }

  /**
   * Retrieves and validates {@link CacheDataDto} from cache for the given execution id.
   *
   * <p>Throws {@link TestCaseStepExecutionFailException} if cache data is missing.
   *
   * @param executionId execution id used as the cache key
   * @return {@link CacheDataDto} found in cache
   */
  protected CacheDataDto getAndValidateCacheDataDto(final String executionId) {
    final CacheDataDto cacheDataDto = getWamCacheManager().getCacheDataDto(executionId);
    if (Objects.isNull(cacheDataDto)) {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "Cache data is not found for executionId: " + executionId);
    }
    return cacheDataDto;
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
  public Map<String, String> getAndValidateStringCacheMap(
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
   * Retrieves and validates {@link CacheDataDto} from cache for the given execution id.
   *
   * <p>Throws {@link TestCaseStepExecutionFailException} if cache data is missing.
   *
   * @param executionId execution id used as the cache key
   * @return {@link CacheDataDto} found in cache
   */
  protected CacheDataDto getCacheDataDto(final String executionId) {
    CacheDataDto cacheDataDto = getWamCacheManager().getCacheDataDto(executionId);
    if (Objects.isNull(cacheDataDto)) {
      cacheDataDto = CacheDataDto.builder().build();
    }
    return cacheDataDto;
  }

  /**
   * Returns a WebElement based on locator type/value and optional index.
   *
   * <p>Supported locator types (case-insensitive):
   *
   * <ul>
   *   <li>id
   *   <li>name
   *   <li>class
   *   <li>xpath
   *   <li>css
   *   <li>tag
   *   <li>linkText
   *   <li>partialLinkText
   * </ul>
   *
   * <p>Index behavior:
   *
   * <ul>
   *   <li>If index is negative, it defaults to 0
   *   <li>If multiple elements match, the element at the index is returned
   * </ul>
   *
   * @param driver WebDriver instance
   * @param locatorType locator type (id, name, class, xpath, css, tag, linkText, partialLinkText)
   * @param locatorValue locator value (actual id/name/xpath/css selector etc.)
   * @param locatorIndex element index (optional) - if invalid, defaults to 0
   * @return matched WebElement
   * @throws TestCaseStepExecutionFailException if element lookup fails for any reason
   */
  public WebElement getWebElement(
      final WebDriver driver,
      final String locatorType,
      final String locatorValue,
      final int locatorIndex) {
    try {
      if (Objects.isNull(driver)) {
        log.error("driver is null");
        throw new IllegalArgumentException("driver is null");
      }
      if (Objects.isNull(locatorType) || locatorType.trim().isEmpty()) {
        log.error("locatorType is null or empty");
        throw new IllegalArgumentException("locatorType is null or empty");
      }
      if (Objects.isNull(locatorValue) || locatorValue.trim().isEmpty()) {
        log.error("locatorValue is null or empty");
        throw new IllegalArgumentException("locatorValue is null or empty");
      }
      final int safeIndex = Math.max(locatorIndex, 0);
      final String normalizedType = locatorType.trim().toLowerCase(Locale.ROOT);
      log.info("started");
      log.info("locatorType : {}", normalizedType);
      log.info("locatorValue : {}", locatorValue);
      log.info("locatorIndex : {}", safeIndex);
      final By by = buildBy(normalizedType, locatorValue);
      final List<WebElement> elements = driver.findElements(by);
      if (elements == null || elements.isEmpty()) {
        log.error("no elements found");
        throw new NoSuchElementException("no elements found for given locator");
      }
      if (safeIndex >= elements.size()) {
        log.error("index out of range : {} / {}", safeIndex, elements.size());
        throw new NoSuchElementException("locatorIndex out of range");
      }
      final WebElement webElement = elements.get(safeIndex);
      log.info("element found");
      log.info("completed");
      return webElement;
    } catch (final Exception exception) {
      log.error("web element get failure : {}", exception.getMessage(), exception);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, "web element get failure");
    }
  }

  /**
   * Builds a Selenium {@link By} instance from locator type and value.
   *
   * @param normalizedLocatorType locator type in lower-case (id, name, class, xpath, css, tag,
   *     linktext, partiallinktext)
   * @param locatorValue locator value
   * @return {@link By} strategy
   */
  private By buildBy(final String normalizedLocatorType, final String locatorValue) {
    switch (normalizedLocatorType) {
      case "id":
        return By.id(locatorValue);
      case "name":
        return By.name(locatorValue);
      case "class":
      case "classname":
        return By.className(locatorValue);
      case "xpath":
        return By.xpath(locatorValue);
      case "css":
      case "cssselector":
        return By.cssSelector(locatorValue);
      case "tag":
      case "tagname":
        return By.tagName(locatorValue);
      case "linktext":
        return By.linkText(locatorValue);
      case "partiallinktext":
        return By.partialLinkText(locatorValue);
      default:
        log.error("unsupported locatorType : {}", normalizedLocatorType);
        throw new IllegalArgumentException("unsupported locatorType");
    }
  }
}
