package wam.automationtool.domain.entity.testplan;

import lombok.Getter;
import java.util.Arrays;
import java.util.Objects;

@Getter
public enum ExecutionFrequency {
  ONCE("ONCE"),
  DAILY("DAILY"),
  HOURLY("HOURLY"),
  WEEKLY("WEEKLY"),
  MONTHLY("MONTHLY");

  private final String id;

  ExecutionFrequency(final String id) {
    this.id = id;
  }


  /**
   * Validates if the provided frequency string corresponds to an ExecutionFrequency.
   * Returns the matched ExecutionFrequency, or ONCE if not valid or null.
   *
   * @param frequency The frequency string to validate.
   * @return Corresponding ExecutionFrequency or ONCE if not valid or null.
   */
  public static ExecutionFrequency checkValidityAndGetFrequency(final String frequency) {
    if (Objects.isNull(frequency)) {
      return ONCE;
    }
    return Arrays.stream(ExecutionFrequency.values())
            .filter(e -> e.name().equalsIgnoreCase(frequency))
            .findFirst()
            .orElse(ONCE);
  }
}
