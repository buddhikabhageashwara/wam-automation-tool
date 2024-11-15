package wam.automationtool.domain.entity.testcasestep;

import lombok.Getter;

@Getter
public enum TestCaseStepType {

  A_SEND_HTTP_REQUEST("A_SEND_HTTP_REQUEST"),
  A_LOG_FILE_EXTRACT("A_LOG_FILE_EXTRACT"),
  A_REMOVE_EXECUTION_CACHE("A_REMOVE_EXECUTION_CACHE"),
  A_DESTROY_CACHE("A_DESTROY_CACHE"),
  W_OPEN_BROWSER("W_OPEN_BROWSER"),
  M_OPEN_APP("M_OPEN_APP");

  private final String id;

  TestCaseStepType(final String id) {
    this.id = id;
  }

  public static boolean isNotExist(final String testCaseStepTypeId) {
    for (final TestCaseStepType type : values()) {
      if (type.getId().equals(testCaseStepTypeId)) {
        return false;
      }
    }
    return true;
  }

}
