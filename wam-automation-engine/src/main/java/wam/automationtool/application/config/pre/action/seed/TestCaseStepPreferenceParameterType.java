package wam.automationtool.application.config.pre.action.seed;

import lombok.Getter;

@Getter
public enum TestCaseStepPreferenceParameterType {

    WAIT_TIME("Wait Time", "waitTime"),
    AGENT_URL("Agent URL", "agentURL"),
    WEB_DRIVER("Web Driver", "webDriver"),
    WEB_DRIVER_CACHE_NAME("webDriver Cache Name", "webDriverCacheName"),
    BROWSER_LINK("Browser Link", "browserLink"),
    STRING_TYPE_CACHE_ITEMS("String Type Cache Items", "stringCacheMap"),
    STRING_TYPE_CACHE_ITEM_KEY("String Type Cache Item Key", "stringCacheMapKey"),
    STRING_TYPE_CACHE_ITEM_VALUE("String Type Cache Item Value", "stringCacheMapValue"),
    LOG_FILE("Log File", "logFile"),
    INCLUDE_REGEX("Include Regex", "includeRegex"),
    EXCLUDE_REGEX("Exclude Regex", "excludeRegex"),
    ACTION_REGEX("Action Regex", "actionRegex"),
    REGEX_GROUP_INDEX_NUMBER("Regex Group Index Number", "regexGroupIndexNumber"),
    INVERT_RESULT("Invert Result", "invertResult"),
    ELEMENT_LOCATOR_TYPE("Element Locator Type", "elementLocatorType"),
    ELEMENT_LOCATOR_VALUE("Element Locator Value", "elementLocatorValue"),
    ELEMENT_LOCATOR_INDEX("Element Locator Index", "elementLocatorIndex"),
    ELEMENT_INPUT_VALUE("Element Input Value", "elementInputValue");

    private final String displayName;
    private final String parameterName;

    TestCaseStepPreferenceParameterType(final String displayName, final String parameterName) {
        this.displayName = displayName;
        this.parameterName = parameterName;
    }
}

