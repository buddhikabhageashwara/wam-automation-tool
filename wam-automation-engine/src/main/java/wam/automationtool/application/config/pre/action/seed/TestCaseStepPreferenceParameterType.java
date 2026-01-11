package wam.automationtool.application.config.pre.action.seed;

import lombok.Getter;

@Getter
public enum TestCaseStepPreferenceParameterType {

    TCS_PREFERENCE_PARAMETER_TYPE_WAIT_TIME("Wait Time", "waitTime"),
    TCS_PREFERENCE_PARAMETER_TYPE_AGENT_URL("Agent URL", "agentURL"),
    TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER("Web Driver", "webDriver"),
    TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME("webDriver Cache Name", "webDriverCacheName"),
    TCS_PREFERENCE_PARAMETER_TYPE_BROWSER_LINK("Browser Link", "browserLink"),
    TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEMS("String Type Cache Items", "stringCacheMap"),
    TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_KEY("String Type Cache Item Key", "stringCacheMapKey"),
    TCS_PREFERENCE_PARAMETER_TYPE_STRING_TYPE_CACHE_ITEM_VALUE("String Type Cache Item Value", "stringCacheMapValue"),
    TCS_PREFERENCE_PARAMETER_TYPE_LOG_FILE("Log File", "logFile"),
    TCS_PREFERENCE_PARAMETER_TYPE_INCLUDE_REGEX("Include Regex", "includeRegex"),
    TCS_PREFERENCE_PARAMETER_TYPE_EXCLUDE_REGEX("Exclude Regex", "excludeRegex"),
    TCS_PREFERENCE_PARAMETER_TYPE_ACTION_REGEX("Action Regex", "actionRegex"),
    TCS_PREFERENCE_PARAMETER_TYPE_REGEX_GROUP_INDEX_NUMBER("Regex Group Index Number", "regexGroupIndexNumber"),
    TCS_PREFERENCE_PARAMETER_TYPE_INVERT_RESULT("Invert Result", "invertResult"),
    TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_TYPE("Element Locator Type", "elementLocatorType"),
    TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_VALUE("Element Locator Value", "elementLocatorValue"),
    TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_LOCATOR_INDEX("Element Locator Index", "elementLocatorIndex"),
    TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INPUT_VALUE("Element Input Value", "elementInputValue"),
    TCS_PREFERENCE_PARAMETER_TYPE_ELEMENT_INPUT_VALUE_CACHE_KEY("Element Input Value Cache Key", "elementInputValueCacheKey");

    private final String displayName;
    private final String parameterName;

    TestCaseStepPreferenceParameterType(final String displayName, final String parameterName) {
        this.displayName = displayName;
        this.parameterName = parameterName;
    }
}

