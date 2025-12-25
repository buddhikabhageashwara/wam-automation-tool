package wam.automationtool.application.config;

public final class AppConstant {
  public static final int BEARER_LENGTH = 7;
  public static final String AUTH_HEADER = "Authorization";
  public static final String BEARER_VALUE = "Bearer ";
  public static final String UTILITY_CLASS = "UTILITY_CLASS";
  public static final String WAM_AUTOMATION_TOKEN_TTL = "wamAutomationTokenTTL";
  public static final int WAM_AUTOMATION_TOKEN_EXPIRATION = 108000;
  public static final int WAM_AUTOMATION_EXECUTION_TOKEN_EXPIRATION = 9999999;
  public static final String INTERNAL_ERROR_MSG_KEY = "internal.error.occurred";
  public static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";
  public static final String CREATED_MODIFIED_USER_ID = "createdModifiedUserId";
  public static final String WAM_AUTOMATION_BASE_PATH = "/v1/wam/automation/";
  public static final String WAM_CACHE_MANAGER = "WAMCache";

  private AppConstant() {}

  public static final class ClaimName {
    public static final String USER_EMAIL = "userEmail";
    public static final String IS_SUPER_ADMIN = "isSuperAdmin";
    public static final String PERMISSION_TYPE_LIST = "permissionTypeList";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String USER_ID = "userId";
    public static final String WAM_AUTOMATION_USER_DETAILS = "wamAutomationUserDetails";

    private ClaimName() {
      throw new IllegalStateException(UTILITY_CLASS);
    }
  }

  public static final class AuthConstants {

    public static final String SUCCESS_CODE = "S-200";
    public static final String FAILED_CODE = "WAM-500";
    public static final String WAM_AUTOMATION_USER_ERROR = "WAM-501";
    public static final String AUTH_TOKEN_MISSING_CODE = "WAM-502";
    public static final String SERVICE_PERMISSION_CODE = "WAM-503";
    public static final String AUTH_TOKEN_VALIDATION_CODE = "WAM-504";
    public static final String REQUEST_FIELD_VALIDATION_CODE = "WAM-505";
    public static final String PASSWORD_MISMATCHED_CODE = "WAM-506";
    public static final String USER_TYPE_ALREADY_EXIST_CODE = "WAM-507";
    public static final String USER_TYPE_OR_PERMISSION_NOT_EXIST_CODE = "WAM-508";
    public static final String TEST_PLAN_ALREADY_EXIST_CODE = "WAM-509";
    public static final String TEST_PLAN_NOT_FOUND_CODE = "WAM-510";
    public static final String TEST_CASE_ALREADY_EXIST_CODE = "WAM-511";
    public static final String TEST_CASE_NOT_FOUND_CODE = "WAM-512";
    public static final String PREFERENCE_PARAMETER_TYPE_NOT_FOUND_CODE = "WAM-513";
    public static final String PREFERENCE_PARAMETER_TYPE_ALREADY_EXIST_CODE = "WAM-514";
    public static final String ALIAS_PARAMETER_TYPE_ALREADY_EXIST_CODE = "WAM-515";
    public static final String ALIAS_PARAMETER_TYPE_NOT_FOUND_CODE = "WAM-516";
    public static final String INVALID_TEST_CASE_STEP_TYPE_CODE = "WAM-517";
    public static final String INVALID_CODE = "WAM-518";
    public static final String TEST_CASE_STEP_NOT_FOUND_CODE = "WAM-519";
    public static final String ALIAS_ALREADY_EXIST_CODE = "WAM-520";
    public static final String INVALID_ALIAS_TYPE_CODE = "WAM-521";
    public static final String ALIAS_NOT_FOUND_CODE = "WAM-522";
    public static final String TEST_CASE_STEP_EXECUTION_FAIL_CODE = "WAM-513";
    public static final String SUCCESS_STATUS = "200";
    public static final String FAILED_STATUS = "500";
    public static final String MESSAGE_OK = "OK";

    private AuthConstants() {
      throw new IllegalStateException(UTILITY_CLASS);
    }
  }

  public static final class CustomHeaders {
    public static final String USER_DETAILS_HEADER = "userDetails";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    private CustomHeaders() {
      throw new IllegalStateException(UTILITY_CLASS);
    }
  }

  public static final class TestCaseStepPreferenceParameterTypeConstant {
    public static final String TCS_PREFERENCE_PARAMETER_TYPE_AGENT_URL =
        "agentURL"; // this is common to all tcs

    public static final String TCS_PREFERENCE_PARAMETER_TYPE_WAIT_TIME = "waitTime";
    public static final String TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_TYPE = "webDriver";
    public static final String TCS_PREFERENCE_PARAMETER_TYPE_WEB_DRIVER_CACHE_NAME =
        "webDriverCacheName";
    public static final String TCS_PREFERENCE_PARAMETER_TYPE_BROWSER_LINK = "browserLink";
    public static final String TCS_PREFERENCE_PARAMETER_TYPE_STRING_CACHE_MAP = "stringCacheMap";
    public static final String TCS_PREFERENCE_PARAMETER_TYPE_STRING_CACHE_MAP_KEY =
        "stringCacheMapKey";
    public static final String TCS_PREFERENCE_PARAMETER_TYPE_STRING_CACHE_MAP_VALUE =
        "stringCacheMapValue";

    private TestCaseStepPreferenceParameterTypeConstant() {
      throw new IllegalStateException(UTILITY_CLASS);
    }
  }

  public static final class TestCaseStepActualAndExpectedResult {
    public static final String TCS_RESULT_EXECUTION_ID = "executionId";

    private TestCaseStepActualAndExpectedResult() {
      throw new IllegalStateException(UTILITY_CLASS);
    }
  }

  public static final class AliasParameterTypeConstant {
    public static final String ALIAS_PARAMETER_TYPE_AGENT_URL =
        "agentURL"; // this is common to all tcs

    private AliasParameterTypeConstant() {
      throw new IllegalStateException(UTILITY_CLASS);
    }
  }
}
