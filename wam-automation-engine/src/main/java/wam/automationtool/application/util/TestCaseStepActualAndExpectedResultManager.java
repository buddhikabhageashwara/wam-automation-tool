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
            "HTTP request successfully sent with URL: {url} and payload: {payload}";
        failActualResultTemplate =
            "HTTP request failed to send with URL: {url} and payload: {payload}";
        expectedResultTemplate = "Server should respond with status: {statusCode}";
        break;
      case A_LOG_FILE_EXTRACT:
        passActualResultTemplate =
            "Log file successfully extracted from path: {filePath} with keyword: {keyword}";
        failActualResultTemplate =
            "Log file extraction failed for path: {filePath} with keyword: {keyword}";
        expectedResultTemplate = "Log file should contain entries with keyword: {keyword}";
        break;
      case A_REMOVE_EXECUTION_CACHE:
        passActualResultTemplate = "Execution cache successfully removed for key: {cacheKey}";
        failActualResultTemplate = "Failed to remove execution cache for key: {cacheKey}";
        expectedResultTemplate = "Cache for key: {cacheKey} should no longer exist";
        break;
      case A_DESTROY_CACHE:
        passActualResultTemplate = "Cache successfully destroyed for environment: {environment}";
        failActualResultTemplate = "Failed to destroy cache for environment: {environment}";
        expectedResultTemplate = "All caches in environment: {environment} should be cleared";
        break;
      case A_WAIT:
        passActualResultTemplate = "Successfully waited for {waitTime} milliseconds";
        failActualResultTemplate = "Failed to wait for {waitTime} milliseconds";
        expectedResultTemplate = "{waitTime} milliseconds should be waited";
        break;
      case W_OPEN_BROWSER:
        passActualResultTemplate =
            "Browser successfully opened at URL: {browserLink}, and the web driver is {webDriver}";
        failActualResultTemplate =
            "Failed to open the browser at URL: {browserLink} using {webDriver}";
        expectedResultTemplate =
            "Browser should open at {browserLink}, and the web driver should be {webDriver}";
        break;
      case M_OPEN_APP:
        passActualResultTemplate = "Mobile app successfully launched with package: {packageName}";
        failActualResultTemplate = "Failed to launch mobile app with package: {packageName}";
        expectedResultTemplate = "App should open the home screen of package: {packageName}";
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
