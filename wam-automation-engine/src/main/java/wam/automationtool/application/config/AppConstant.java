package wam.automationtool.application.config;

public final class AppConstant {
  public static final int BEARER_LENGTH = 7;
  public static final String AUTH_HEADER = "Authorization";
  public static final String BEARER_VALUE = "Bearer ";
  public static final String UTILITY_CLASS = "UTILITY_CLASS";
  public static final String WAM_AUTOMATION_TOKEN_TTL = "wamAutomationTokenTTL";
  public static final int WAM_AUTOMATION_TOKEN_EXPIRATION = 108000;
  public static final String INTERNAL_ERROR_MSG_KEY = "internal.error.occurred";
  public static final String CORRELATION_ID_LOG_VAR_NAME = "correlationId";

  private AppConstant() {

  }

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
    public static final String SUCCESS_STATUS = "200";
    public static final String FAILED_STATUS = "500";
    public static final String MESSAGE_OK = "OK";
    private AuthConstants() {
      throw new IllegalStateException(UTILITY_CLASS);
    }

  }

  public static final class CustomHeaders {
    private CustomHeaders() {
      throw new IllegalStateException(UTILITY_CLASS);
    }

    public static final String USER_DETAILS_HEADER = "userDetails";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  }
}

