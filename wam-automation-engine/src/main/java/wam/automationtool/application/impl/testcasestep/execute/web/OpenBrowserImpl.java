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
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_BROWSER_LINK;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_WEB_DRIVER_CACHE_NAME;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_WEB_DRIVER_TYPE;
import static wam.automationtool.application.config.pre.action.seed.TestCaseStepPreferenceParameterType.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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

  private static final Map<String, WebDriver> activeDrivers = new ConcurrentHashMap<>();

  @Value("${selenium.grid.base.url}")
  private String seleniumGridBaseURL;

  public static Map<String, WebDriver> getActiveDrivers() {
    return activeDrivers;
  }

  public static WebDriver removeActiveDriver(final String webDriverCacheName) {
    if (Objects.isNull(webDriverCacheName)) {
      log.warn("webDriverCacheName is null/empty");
      return null;
    }
    final WebDriver isRemoved = activeDrivers.remove(webDriverCacheName);
    if (Objects.nonNull(isRemoved)) {
      log.info(
          "Removed WebDriver for webDriverCacheName: {}",
          webDriverCacheName);
    } else {
      log.warn(
          "No WebDriver found for webDriverCacheName: {}",
          webDriverCacheName);
    }
    return isRemoved; // return in case caller wants to handle it (but not quit here)
  }

  public static void clearActiveDrivers() {
    final int size = activeDrivers.size();
    activeDrivers.clear();
    log.info(
        "Cleared activeDrivers map. Previous size: {}", size);
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
              extractedPreferenceParameters, TCS_PREFERENCE_PARAMETER_TYPE_BROWSER_LINK.getParameterName());
      getAndValidateTCSPreferenceParameterTypeExistence(
              extractedPreferenceParameters, TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER.getParameterName());
      getAndValidateTCSPreferenceParameterTypeExistence(
              extractedPreferenceParameters, TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME.getParameterName());
    resultParameters.put(
            TCS_RESULT_BROWSER_LINK,
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_BROWSER_LINK.getParameterName()));
    resultParameters.put(
            TCS_RESULT_WEB_DRIVER_TYPE,
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER.getParameterName()));
    resultParameters.put(
            TCS_RESULT_WEB_DRIVER_CACHE_NAME,
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME.getParameterName()));
    final WebDriver driver =
        setupWebDriver(
            extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER.getParameterName()));
    updateCache(
        extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME.getParameterName()),
        driver);
    openBrowser(
        driver, extractedPreferenceParameters.get(TCS_PREFERENCE_PARAMETER_TYPE_BROWSER_LINK.getParameterName()));
  }

  private void updateCache(
      final String webDriverCacheName,
      final WebDriver driver) {
    activeDrivers.put(webDriverCacheName, driver);
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
      final URL gridUrl = new URL(seleniumGridBaseURL);
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
          final EdgeOptions edgeOptions = new EdgeOptions();
          final String edgeBinary = resolveEdgeBinary();
          if (Objects.nonNull(edgeBinary)) {
            edgeOptions.setBinary(edgeBinary);
          }
          driver = new RemoteWebDriver(gridUrl, edgeOptions);
          log.info("Edge WebDriver setup successful.");
          break;
        default:
          log.error("Invalid web driver type specified: {}", webDriverType);
          throw new TestCaseStepExecutionFailException(
              BAD_REQUEST,
              TEST_CASE_STEP_EXECUTION_FAIL_CODE,
              "Invalid web driver type specified: " + webDriverType);
      }
    } catch (final MalformedURLException malformedURLException) {
      log.error(
          "MalformedURLException occurred while setting up WebDriver: {}",
          malformedURLException.getMessage());
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "MalformedURLException occurred while setting up WebDriver: {}"
              + malformedURLException.getMessage());
    } catch (final Exception exception) {
      log.error(
          "Exception occurred while setting up WebDriver: {}", exception.getMessage(), exception);
      throw new TestCaseStepExecutionFailException(
          BAD_REQUEST,
          TEST_CASE_STEP_EXECUTION_FAIL_CODE,
          "Exception occurred while setting up WebDriver: {}" + exception.getMessage());
    }
    return driver;
  }

  private String resolveEdgeBinary() {
    final String path1 = "C:\\Program Files\\Microsoft\\Edge\\Application\\msedge.exe";
    final String path2 = "C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe";
    if (Files.exists(Paths.get(path1))) return path1;
    if (Files.exists(Paths.get(path2))) return path2;
    return null; // let EdgeDriver try default
  }
}
