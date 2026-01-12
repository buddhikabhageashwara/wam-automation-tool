/*
 * MIT License
 *
 * Copyright (c) 2024 buddhika bhageashwara alwis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
