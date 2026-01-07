package wam.automationtool.application.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import wam.automationtool.application.dto.execute.ActualAndExpectedResultDto;
import wam.automationtool.domain.entity.testcasestep.TestCaseStepType;

import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ACTION_REGEX;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_BROWSER_LINK;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_INPUT_VALUE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_LOCATOR_INDEX;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_LOCATOR_TYPE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_ELEMENT_LOCATOR_VALUE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_EXCLUDE_REGEX;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_EXECUTION_ID;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_INCLUDE_REGEX;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_LOG_FILE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_LOG_FILE_LOCATION;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_LOG_READ_ASSERT_VALUE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_REGEX_GROUP_INDEX_NUMBER;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_STRING_CACHE_MAP;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_STRING_CACHE_MAP_KEY;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_STRING_CACHE_MAP_VALUE;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_TEMP_LOG_FILE_LOCATION;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_TEMP_LOG_FILE_NAME;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_WAIT_TIME;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_WEB_DRIVER_CACHE_NAME;
import static wam.automationtool.application.config.AppConstant.TestCaseStepResultConstant.TCS_RESULT_WEB_DRIVER_TYPE;

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
            case A_LOG_FILE_READING_START:
                passActualResultTemplate =
                        "log reading started successfully: logFile: {"+TCS_RESULT_LOG_FILE+"}, " +
                                "logFileLocation: {"+TCS_RESULT_LOG_FILE_LOCATION+"}, " +
                                "tempLogFileLocation: {"+TCS_RESULT_TEMP_LOG_FILE_LOCATION+"}, " +
                                "tempLogFileName: {"+TCS_RESULT_TEMP_LOG_FILE_NAME+"}.";
                failActualResultTemplate =
                        "failed to start log reading: logFile: {"+TCS_RESULT_LOG_FILE+"}, " +
                                "logFileLocation: {"+TCS_RESULT_LOG_FILE_LOCATION+"}.";
                expectedResultTemplate =
                        "log reading should be started for logFile: {"+TCS_RESULT_LOG_FILE+"} " +
                                "from logFileLocation: {"+TCS_RESULT_LOG_FILE_LOCATION+"}.";
                break;
            case A_LOG_FILE_READING_END:
                passActualResultTemplate =
                        "log reading ended successfully: logFile: {"+TCS_RESULT_LOG_FILE+"}, " +
                                "logFileLocation: {"+TCS_RESULT_LOG_FILE_LOCATION+"}, " +
                                "tempLogFileName: {"+TCS_RESULT_TEMP_LOG_FILE_NAME+"}.";
                failActualResultTemplate =
                        "failed to end log reading: logFile: {"+TCS_RESULT_LOG_FILE+"}, " +
                                "logFileLocation: {"+TCS_RESULT_LOG_FILE_LOCATION+"}.";
                expectedResultTemplate =
                        "log reading should be ended for logFile: {"+TCS_RESULT_LOG_FILE+"} " +
                                "from logFileLocation: {"+TCS_RESULT_LOG_FILE_LOCATION+"}.";
                break;
            case A_LOG_FILE_LINE_READ:
                passActualResultTemplate =
                        "log line read and assertion completed successfully: " +
                                "logFile: {"+TCS_RESULT_LOG_FILE+"}, " +
                                "tempLogFileName: {"+TCS_RESULT_TEMP_LOG_FILE_NAME+"}, " +
                                "includeRegex: {"+TCS_RESULT_INCLUDE_REGEX+"}, " +
                                "excludeRegex: {"+TCS_RESULT_EXCLUDE_REGEX+"}, " +
                                "actionRegex: {"+TCS_RESULT_ACTION_REGEX+"}, " +
                                "regexGroupIndexNumber: {"+TCS_RESULT_REGEX_GROUP_INDEX_NUMBER+"}, " +
                                "logReadAssertValue: {"+TCS_RESULT_LOG_READ_ASSERT_VALUE+"}.";
                failActualResultTemplate =
                        "failed to read log line or validate assertion: " +
                                "logFile: {"+TCS_RESULT_LOG_FILE+"}, " +
                                "tempLogFileName: {"+TCS_RESULT_TEMP_LOG_FILE_NAME+"}, " +
                                "includeRegex: {"+TCS_RESULT_INCLUDE_REGEX+"}, " +
                                "excludeRegex: {"+TCS_RESULT_EXCLUDE_REGEX+"}, " +
                                "actionRegex: {"+TCS_RESULT_ACTION_REGEX+"}, " +
                                "regexGroupIndexNumber: {"+TCS_RESULT_REGEX_GROUP_INDEX_NUMBER+"}, " +
                                "logReadAssertValue: {"+TCS_RESULT_LOG_READ_ASSERT_VALUE+"}.";
                expectedResultTemplate =
                        "log reading should locate a matching line in logFile: {"+TCS_RESULT_LOG_FILE+"} " +
                                "(tempLogFileName: {"+TCS_RESULT_TEMP_LOG_FILE_NAME+"}) " +
                                "that matches includeRegex: {"+TCS_RESULT_INCLUDE_REGEX+"} " +
                                "and does not match excludeRegex: {"+TCS_RESULT_EXCLUDE_REGEX+"}; " +
                                "then actionRegex: {"+TCS_RESULT_ACTION_REGEX+"} should extract group index " +
                                "{"+TCS_RESULT_REGEX_GROUP_INDEX_NUMBER+"} and equal logReadAssertValue: " +
                                "{"+TCS_RESULT_LOG_READ_ASSERT_VALUE+"}.";
                break;
            case A_LOG_FILE_EXTRACT:
                passActualResultTemplate =
                        "Log file extract completed successfully: logFile: {" + TCS_RESULT_LOG_FILE + "}, " +
                                "logFileLocation: {" + TCS_RESULT_LOG_FILE_LOCATION + "}, " +
                                "tempLogFileName: {" + TCS_RESULT_TEMP_LOG_FILE_NAME + "}.";
                failActualResultTemplate =
                        "Log file extract failed: logFile: {" + TCS_RESULT_LOG_FILE + "}, " +
                                "logFileLocation: {" + TCS_RESULT_LOG_FILE_LOCATION + "}, " +
                                "tempLogFileName: {" + TCS_RESULT_TEMP_LOG_FILE_NAME + "}.";
                expectedResultTemplate =
                        "System should extract the log file: {" + TCS_RESULT_LOG_FILE + "} " +
                                "from logFileLocation: {" + TCS_RESULT_LOG_FILE_LOCATION + "} " +
                                "and create tempLogFileName: {" + TCS_RESULT_TEMP_LOG_FILE_NAME + "}.";
                break;
            case A_LOG_FILE_DELETE:
                passActualResultTemplate =
                        "Temp log file deleted successfully: tempLogFileName: {"+TCS_RESULT_TEMP_LOG_FILE_NAME+"}, " +
                                "logFile: {"+TCS_RESULT_LOG_FILE+"}, " +
                                "logFileLocation: {"+TCS_RESULT_LOG_FILE_LOCATION+"}.";
                failActualResultTemplate =
                        "Failed to delete temp log file: logFile: {"+TCS_RESULT_LOG_FILE+"}, " +
                                "logFileLocation: {"+TCS_RESULT_LOG_FILE_LOCATION+"}.";
                expectedResultTemplate =
                        "Temp log file should be deleted for logFile: {"+TCS_RESULT_LOG_FILE+"} " +
                                "from logFileLocation: {"+TCS_RESULT_LOG_FILE_LOCATION+"}.";
                break;
            case A_REMOVE_ALL_EXECUTION_CACHE:
                passActualResultTemplate = "All execution caches were removed successfully.";
                failActualResultTemplate = "Failed to remove all execution caches.";
                expectedResultTemplate = "All the execution caches should not longer exist.";
                break;
            case A_REMOVE_EXECUTION_CACHE:
                passActualResultTemplate =
                        "Execution cache was removed successfully for execution id: {"+TCS_RESULT_EXECUTION_ID+"}.";
                failActualResultTemplate =
                        "Failed to remove the execution cache for execution id: {"+TCS_RESULT_EXECUTION_ID+"}.";
                expectedResultTemplate =
                        "The cache entry for execution id: {"+TCS_RESULT_EXECUTION_ID+"} should no longer exist.";
                break;
            case A_ADD_CACHE_ITEM:
                passActualResultTemplate =
                        "Cache item was added successfully for key: {"+TCS_RESULT_STRING_CACHE_MAP_KEY+"}, " +
                                "along with its corresponding value: {"+TCS_RESULT_STRING_CACHE_MAP_VALUE+"}.";
                failActualResultTemplate =
                        "Failed to add the cache item for key: {"+TCS_RESULT_STRING_CACHE_MAP_KEY+"}, " +
                                "along with its corresponding value: {"+TCS_RESULT_STRING_CACHE_MAP_VALUE+"}.";
                expectedResultTemplate =
                        "The cache item for key: {"+TCS_RESULT_STRING_CACHE_MAP_KEY+"} should be added, " +
                                "along with its corresponding value: {"+TCS_RESULT_STRING_CACHE_MAP_VALUE+"}.";
                break;
            case A_REMOVE_CACHE_ITEM:
                passActualResultTemplate =
                        "Cache item was removed successfully for key: {"+TCS_RESULT_STRING_CACHE_MAP+"}, " +
                                "along with its corresponding value.";
                failActualResultTemplate =
                        "Failed to remove the cache item for key: {"+TCS_RESULT_STRING_CACHE_MAP+"}, " +
                                "along with its corresponding value.";
                expectedResultTemplate =
                        "The cache item for key: {"+TCS_RESULT_STRING_CACHE_MAP+"} should be removed, " +
                                "along with its corresponding value.";
                break;
            case A_WAIT:
                passActualResultTemplate =
                        "Waited successfully for {"+TCS_RESULT_WAIT_TIME+"} milliseconds.";
                failActualResultTemplate =
                        "Failed to wait for {"+TCS_RESULT_WAIT_TIME+"} milliseconds.";
                expectedResultTemplate =
                        "The system should wait for {"+TCS_RESULT_WAIT_TIME+"} milliseconds.";
                break;
            case W_OPEN_BROWSER:
                passActualResultTemplate =
                        "Browser was opened successfully at URL: {"+TCS_RESULT_BROWSER_LINK+"}. " +
                                "Browser type: {"+TCS_RESULT_WEB_DRIVER_TYPE+"}. " +
                                "The driver was saved under the name: {"+TCS_RESULT_WEB_DRIVER_CACHE_NAME+"}.";
                failActualResultTemplate =
                        "Failed to open the browser at URL: {"+TCS_RESULT_BROWSER_LINK+"} " +
                                "using {"+TCS_RESULT_WEB_DRIVER_TYPE+"}. " +
                                "The driver could not be saved under the name: {"+TCS_RESULT_WEB_DRIVER_CACHE_NAME+"}.";
                expectedResultTemplate =
                        "The browser should open at {"+TCS_RESULT_BROWSER_LINK+"} "
                                + "using {"+TCS_RESULT_WEB_DRIVER_TYPE+"}, "
                                + "and the driver should be saved under the name: " +
                                "{"+TCS_RESULT_WEB_DRIVER_CACHE_NAME+"}.";
                break;
            case W_CLOSE_BROWSER:
                passActualResultTemplate =
                        "Browser was closed successfully. The WebDriver was saved under the name: "
                                + "{"+TCS_RESULT_WEB_DRIVER_CACHE_NAME+"}.";
                failActualResultTemplate =
                        "Failed to close the browser, or the WebDriver could not be found under the name: "
                                + "{"+TCS_RESULT_WEB_DRIVER_CACHE_NAME+"}.";
                expectedResultTemplate =
                        "The browser should close, and the WebDriver should be saved under the name:"
                                + " {"+TCS_RESULT_WEB_DRIVER_CACHE_NAME+"}.";
                break;
            case W_ELEMENT_VALUE_INPUT:
                passActualResultTemplate =
                        "Value input completed successfully. " +
                                "webDriverCacheName: {" + TCS_RESULT_WEB_DRIVER_CACHE_NAME + "}, " +
                                "elementLocatorType: {" + TCS_RESULT_ELEMENT_LOCATOR_TYPE + "}, " +
                                "elementLocatorValue: {" + TCS_RESULT_ELEMENT_LOCATOR_VALUE + "}, " +
                                "elementLocatorIndex: {" + TCS_RESULT_ELEMENT_LOCATOR_INDEX + "}, " +
                                "elementInputValue: {" + TCS_RESULT_ELEMENT_INPUT_VALUE + "}.";
                failActualResultTemplate =
                        "Failed to input value. " +
                                "webDriverCacheName: {" + TCS_RESULT_WEB_DRIVER_CACHE_NAME + "}, " +
                                "elementLocatorType: {" + TCS_RESULT_ELEMENT_LOCATOR_TYPE + "}, " +
                                "elementLocatorValue: {" + TCS_RESULT_ELEMENT_LOCATOR_VALUE + "}, " +
                                "elementLocatorIndex: {" + TCS_RESULT_ELEMENT_LOCATOR_INDEX + "}, " +
                                "elementInputValue: {" + TCS_RESULT_ELEMENT_INPUT_VALUE + "}.";
                expectedResultTemplate =
                        "System should input elementInputValue: {" + TCS_RESULT_ELEMENT_INPUT_VALUE + "} " +
                                "into the element identified by elementLocatorType: {" + TCS_RESULT_ELEMENT_LOCATOR_TYPE + "} " +
                                "and elementLocatorValue: {" + TCS_RESULT_ELEMENT_LOCATOR_VALUE + "} " +
                                "at elementLocatorIndex: {" + TCS_RESULT_ELEMENT_LOCATOR_INDEX + "} " +
                                "using webDriverCacheName: {" + TCS_RESULT_WEB_DRIVER_CACHE_NAME + "}.";
                break;
            case W_ELEMENT_CLICK:
                passActualResultTemplate =
                        "Element click completed successfully. " +
                                "webDriverCacheName: {" + TCS_RESULT_WEB_DRIVER_CACHE_NAME + "}, " +
                                "elementLocatorType: {" + TCS_RESULT_ELEMENT_LOCATOR_TYPE + "}, " +
                                "elementLocatorValue: {" + TCS_RESULT_ELEMENT_LOCATOR_VALUE + "}, " +
                                "elementLocatorIndex: {" + TCS_RESULT_ELEMENT_LOCATOR_INDEX + "}.";
                failActualResultTemplate =
                        "Failed to click the element. " +
                                "webDriverCacheName: {" + TCS_RESULT_WEB_DRIVER_CACHE_NAME + "}, " +
                                "elementLocatorType: {" + TCS_RESULT_ELEMENT_LOCATOR_TYPE + "}, " +
                                "elementLocatorValue: {" + TCS_RESULT_ELEMENT_LOCATOR_VALUE + "}, " +
                                "elementLocatorIndex: {" + TCS_RESULT_ELEMENT_LOCATOR_INDEX + "}.";
                expectedResultTemplate =
                        "System should click the element identified by elementLocatorType: {" + TCS_RESULT_ELEMENT_LOCATOR_TYPE + "} " +
                                "and elementLocatorValue: {" + TCS_RESULT_ELEMENT_LOCATOR_VALUE + "} " +
                                "at elementLocatorIndex: {" + TCS_RESULT_ELEMENT_LOCATOR_INDEX + "} " +
                                "using webDriverCacheName: {" + TCS_RESULT_WEB_DRIVER_CACHE_NAME + "}.";
                break;
            case M_OPEN_APP:
                passActualResultTemplate =
                        "Mobile app was launched successfully with package name: {packageName}.";
                failActualResultTemplate =
                        "Failed to launch the mobile app with package name: {packageName}.";
                expectedResultTemplate =
                        "The app should open the home screen for package: {packageName}.";
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
