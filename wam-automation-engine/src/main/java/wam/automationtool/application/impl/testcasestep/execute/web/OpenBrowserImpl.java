package wam.automationtool.application.impl.testcasestep.execute.web;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static wam.automationtool.application.config.AppConstant.AuthConstants.TEST_CASE_STEP_EXECUTION_FAIL_CODE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepTypeConstant.W_BROWSER_LINK;
import static wam.automationtool.application.config.AppConstant.TestCaseStepTypeConstant.W_WEB_DRIVER_CACHE_NAME;
import static wam.automationtool.application.config.AppConstant.TestCaseStepTypeConstant.W_WEB_DRIVER_TYPE;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.springframework.beans.factory.annotation.Value;
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
public class OpenBrowserImpl extends TestCaseStepExecutorBase implements TestCaseStepExecutor {

  @Value("${selenium.grid.base.url}")
  private String seleniumGridBaseURL;

  private static final Map<String, WebDriver> activeDrivers = new ConcurrentHashMap<>();

  public static Map<String, WebDriver> getActiveDrivers() {
    return activeDrivers;
  }

  @Override
  public TestCaseStepType getTestCaseStepType() {
    return TestCaseStepType.W_OPEN_BROWSER;
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
    }  catch (final TestCaseStepExecutionFailException exception) {
        status = TestCaseStepExecutionStatus.FAILED.toString();
        isUnknown = false;
    }  catch (final Exception exception) {
      status = TestCaseStepExecutionStatus.FAILED.toString();
      isUnknown = true;
      unknownReason = exception.getMessage();
    } finally {
      endTime = DateTimeManager.getCurrentUTCDateTime();
      actualAndExpectedResult =
          getActualAndExpectedResult(
              resultParameters, testCaseStepExecuteRequestDto, isUnknown, status, unknownReason);
    }
    return buildResponse(status, actualAndExpectedResult, startTime, endTime);
  }

  private void start(
      final LinkedHashMap<String, String> resultParameters,
      final TestCaseStepExecuteRequestDto testCaseStepExecuteRequestDto) {
    final CacheDataDto cacheDataDto =
        getWamCacheManager().getCacheDataDto(testCaseStepExecuteRequestDto.getExecutionId());
    final Map<String, String> extractedPreferenceParameters =
        extractPreferenceParameters(testCaseStepExecuteRequestDto);
    resultParameters.put(W_BROWSER_LINK, extractedPreferenceParameters.get(W_BROWSER_LINK));
    resultParameters.put(W_WEB_DRIVER_TYPE, extractedPreferenceParameters.get(W_WEB_DRIVER_TYPE));
    resultParameters.put(W_WEB_DRIVER_CACHE_NAME, extractedPreferenceParameters.get(W_WEB_DRIVER_CACHE_NAME));
    final WebDriver driver = setupWebDriver(extractedPreferenceParameters.get(W_WEB_DRIVER_TYPE));
    updateCache(
        cacheDataDto,
        extractedPreferenceParameters.get(W_WEB_DRIVER_CACHE_NAME),
        driver,
        testCaseStepExecuteRequestDto);
    openBrowser(driver, extractedPreferenceParameters.get(W_BROWSER_LINK));
  }

  private void updateCache(
          CacheDataDto cacheDataDto,
          final String webDriverCacheName,
          final WebDriver driver,
          final TestCaseStepExecuteRequestDto requestDto) {
    if (Objects.isNull(cacheDataDto)) {
      cacheDataDto = CacheDataDto.builder().build();
    }
    activeDrivers.put(webDriverCacheName, driver);
    getWamCacheManager().addToCache(requestDto.getExecutionId(), cacheDataDto);
  }

  private void openBrowser(final WebDriver driver, final String browserLink) {
    if (browserLink != null && !browserLink.isEmpty()) {
      driver.get(browserLink);
    } else {
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE, "browserLink is empty");
    }
  }

  public WebDriver setupWebDriver(final String webDriverType) {
    WebDriver driver = null;
    try {
      final URL gridUrl = new URL(seleniumGridBaseURL + "/wd/hub");
      log.info("Setting up WebDriver for browser type: {}", webDriverType);
      switch (webDriverType.toLowerCase()) {
        case "chrome":
          driver = new RemoteWebDriver(gridUrl, new ChromeOptions());
          log.info("Chrome WebDriver setup successful.");
          break;
        case "firefox":
          driver = new RemoteWebDriver(gridUrl, new FirefoxOptions());
          log.info("Firefox WebDriver setup successful.");
          break;
        case "edge":
          driver = new RemoteWebDriver(gridUrl, new EdgeOptions());
          log.info("Edge WebDriver setup successful.");
          break;
        case "safari":
          // Assuming the driver setup for Safari in Grid is available
          driver = new RemoteWebDriver(gridUrl, new SafariOptions());
          log.info("Safari WebDriver setup successful.");
          break;
        case "ie":
          // Assuming the driver setup for Internet Explorer in Grid is available
          driver = new RemoteWebDriver(gridUrl, new InternetExplorerOptions());
          log.info("Internet Explorer WebDriver setup successful.");
          break;
        default:
          log.error("Invalid web driver type specified: {}", webDriverType);
          throw new TestCaseStepExecutionFailException(
                  BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE,
                  "Invalid web driver type specified: " + webDriverType);
      }
    } catch (final MalformedURLException malformedURLException) {
      log.error("MalformedURLException occurred while setting up WebDriver: {}", malformedURLException.getMessage());
      throw new TestCaseStepExecutionFailException(
              BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE,
              "MalformedURLException occurred while setting up WebDriver: {}" +
                      malformedURLException.getMessage());
    } catch (final Exception exception) {
      log.error("Exception occurred while setting up WebDriver: {}", exception.getMessage(), exception);
      throw new TestCaseStepExecutionFailException(
              BAD_REQUEST, TEST_CASE_STEP_EXECUTION_FAIL_CODE,
              "Exception occurred while setting up WebDriver: {}" + exception.getMessage());
    }
    return driver;
  }
}
