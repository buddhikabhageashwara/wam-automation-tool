package wam.automationtool.application.config.pre.action.seed;

import lombok.Getter;

@Getter
public enum TestCaseStepAliasParameterType {

    ALIAS_PARAMETER_TYPE_AGENT_URL("Agent URL", "agentURL"),
    ALIAS_PARAMETER_TYPE_LOG_FILE_LOCATION("Log File Location", "logFileLocation");

    private final String displayName;
    private final String parameterName;

    TestCaseStepAliasParameterType(final String displayName, final String parameterName) {
        this.displayName = displayName;
        this.parameterName = parameterName;
    }
}

