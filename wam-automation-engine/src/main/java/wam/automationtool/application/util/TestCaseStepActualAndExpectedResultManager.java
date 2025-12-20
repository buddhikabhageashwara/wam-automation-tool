package wam.automationtool.application.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import wam.automationtool.application.dto.execute.ActualAndExpectedResultDto;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

public class TestCaseStepActualAndExpectedResultManager {

  public static ActualAndExpectedResultDto getActualAndExpectedResult(
      final LinkedHashMap<String, String> resultParameters,
      final TestCaseStepType testCaseStepType,
      final boolean isUnknown,
      final String status,
      final String unKnownReason) {
    String passActualResultTemplate;
    String failActualResultTemplate;
    String expectedResultTemplate;
    switch (testCaseStepType) {
      case A_SEND_HTTP_REQUEST:
        passActualResultTemplate =
            "HTTP request was sent successfully to URL: {url} with payload: {payload}.";
        failActualResultTemplate =
            "Failed to send the HTTP request to URL: {url} with payload: {payload}.";
        expectedResultTemplate = "The server should respond with status code: {statusCode}.";
        break;
      case A_LOG_FILE_EXTRACT:
        passActualResultTemplate =
            "Log file was extracted successfully from path: {filePath} using keyword: {keyword}.";
        failActualResultTemplate =
            "Failed to extract the log file from path: {filePath} using keyword: {keyword}.";
        expectedResultTemplate = "The log file should contain entries matching keyword: {keyword}.";
        break;
      case A_REMOVE_EXECUTION_CACHE:
        passActualResultTemplate = "Execution cache was removed successfully for key: {cacheKey}.";
        failActualResultTemplate = "Failed to remove the execution cache for key: {cacheKey}.";
        expectedResultTemplate = "The cache entry for key: {cacheKey} should no longer exist.";
        break;
      case A_DESTROY_CACHE:
        passActualResultTemplate =
            "Cache was destroyed successfully for environment: {environment}.";
        failActualResultTemplate = "Failed to destroy the cache for environment: {environment}.";
        expectedResultTemplate = "All caches in environment: {environment} should be cleared.";
        break;
      case A_WAIT:
        passActualResultTemplate = "Waited successfully for {waitTime} milliseconds.";
        failActualResultTemplate = "Failed to wait for {waitTime} milliseconds.";
        expectedResultTemplate = "The system should wait for {waitTime} milliseconds.";
        break;
      case W_OPEN_BROWSER:
        passActualResultTemplate =
            "Browser was opened successfully at URL: {browserLink}. Browser type: {webDriver}. The driver was saved under the name: {webDriverCacheName}.";
        failActualResultTemplate =
            "Failed to open the browser at URL: {browserLink} using {webDriver}. The driver could not be saved under the name: {webDriverCacheName}.";
        expectedResultTemplate =
            "The browser should open at {browserLink} using {webDriver}, and the driver should be saved under the name: {webDriverCacheName}.";
        break;
      case W_CLOSE_BROWSER:
        passActualResultTemplate =
            "Browser was closed successfully. The WebDriver was saved under the name: {webDriverCacheName}.";
        failActualResultTemplate =
            "Failed to close the browser, or the WebDriver could not be found under the name: {webDriverCacheName}.";
        expectedResultTemplate =
            "The browser should close, and the WebDriver should be saved under the name: {webDriverCacheName}.";
        break;
      case M_OPEN_APP:
        passActualResultTemplate =
            "Mobile app was launched successfully with package name: {packageName}.";
        failActualResultTemplate =
            "Failed to launch the mobile app with package name: {packageName}.";
        expectedResultTemplate = "The app should open the home screen for package: {packageName}.";
        break;
      default:
        passActualResultTemplate = "UNKNOWN";
        failActualResultTemplate = "UNKNOWN";
        expectedResultTemplate = "UNKNOWN";
    }
    if (isUnknown) {
      failActualResultTemplate = "Execution failed due to an unknown reason: " + unKnownReason;
    }
    final String actualResult =
        "FAILED".equalsIgnoreCase(status)
            ? replacePlaceholders(failActualResultTemplate, resultParameters)
            : replacePlaceholders(passActualResultTemplate, resultParameters);
    final String expectedResult = replacePlaceholders(expectedResultTemplate, resultParameters);
    return ActualAndExpectedResultDto.builder()
        .actualResult(actualResult)
        .expectedResult(expectedResult)
        .build();
  }

  private static String replacePlaceholders(
      final String template, final LinkedHashMap<String, String> parameters) {
    String result = template;
    if (Objects.nonNull(parameters)) {
      for (final Map.Entry<String, String> entry : parameters.entrySet()) {
        String placeholder = "{" + entry.getKey() + "}";
        String replacement = Objects.nonNull(entry.getValue()) ? entry.getValue() : "";
        result = result.replace(placeholder, replacement);
      }
    }
    return result;
  }
}
