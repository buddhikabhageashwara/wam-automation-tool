package wam.automationtool.application.exception.enums;

/** Enum for keep exception types. */
public enum ExceptionType {
  PROCESSING_FAILED(0),
  VALIDATION_FAILED(1);

  private final int id;

  ExceptionType(final int id) {
    this.id = id;
  }

  /**
   * Get detailed description of enum value.
   *
   * @param id given enum id
   * @return a enum name string
   */
  public static String getDescription(final int id) {
    switch (id) {
      case 0:
        return "Processing Failed";
      case 1:
        return "Validation Failed";
      default:
        return null;
    }
  }
}
