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

package wam.automationtool.application.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LogRegexMatcherUtil {

  private LogRegexMatcherUtil() {
    throw new IllegalStateException("UTILITY_CLASS");
  }

  /**
   * Reads a log file line-by-line, applies optional include/exclude regex checks, then applies a
   * regex. If a match is found, extracts a capturing group value and compares it with the expected
   * value.
   *
   * <p>If {@code invertResult} is true, the final matched result will be inverted:
   *
   * <ul>
   *   <li>normal: matched=true means PASS
   *   <li>invert: matched=false means PASS (useful for "PASS when no error found")
   * </ul>
   *
   * @param logFilePath path to the log file
   * @param includeRegex required regex (nullable/blank = no include filter)
   * @param excludeRegex regex to ignore (nullable/blank = no exclude filter)
   * @param regex regex pattern (must contain the capturing group you want)
   * @param groupIndex capturing group index to extract (0..n). 0 = full match
   * @param expectedValue expected value to match (nullable means “just return extracted values”)
   * @param invertResult if true, invert the final matched result
   * @return result object with extracted values and match status
   */
  public static LogRegexResult findAndValidate(
      final Path logFilePath,
      final String includeRegex,
      final String excludeRegex,
      final String regex,
      final int groupIndex,
      final String expectedValue,
      final boolean invertResult) {
    final List<String> extractedValues = new ArrayList<>();
    if (Objects.isNull(logFilePath)
        || !Files.exists(logFilePath)
        || !Files.isReadable(logFilePath)) {
      return applyInvertIfRequired(
          new LogRegexResult(
              false, extractedValues, "log file not found or not readable: " + logFilePath),
          invertResult);
    }
    if (Objects.isNull(regex) || regex.isBlank()) {
      return applyInvertIfRequired(
          new LogRegexResult(false, extractedValues, "regex is null or blank"), invertResult);
    }
    if (groupIndex < 0) {
      return applyInvertIfRequired(
          new LogRegexResult(false, extractedValues, "groupIndex is invalid: " + groupIndex),
          invertResult);
    }
    final Pattern mainPattern;
    final Pattern includePattern;
    final Pattern excludePattern;
    try {
      mainPattern = Pattern.compile(regex);
    } catch (final Exception exception) {
      return applyInvertIfRequired(
          new LogRegexResult(false, extractedValues, "invalid regex: " + exception.getMessage()),
          invertResult);
    }
    try {
      includePattern =
          (Objects.nonNull(includeRegex) && !includeRegex.isBlank())
              ? Pattern.compile(includeRegex)
              : null;
    } catch (final Exception exception) {
      return applyInvertIfRequired(
          new LogRegexResult(
              false, extractedValues, "invalid includeRegex: " + exception.getMessage()),
          invertResult);
    }
    try {
      excludePattern =
          (Objects.nonNull(excludeRegex) && !excludeRegex.isBlank())
              ? Pattern.compile(excludeRegex)
              : null;
    } catch (final Exception exception) {
      return applyInvertIfRequired(
          new LogRegexResult(
              false, extractedValues, "invalid excludeRegex: " + exception.getMessage()),
          invertResult);
    }
    try {
      try (final var lines = Files.lines(logFilePath, StandardCharsets.UTF_8)) {
        final var iterator = lines.iterator();
        while (iterator.hasNext()) {
          final String line = iterator.next();
          if (Objects.nonNull(includePattern) && !includePattern.matcher(line).find()) {
            continue;
          }
          if (Objects.nonNull(excludePattern) && excludePattern.matcher(line).find()) {
            continue;
          }
          final Matcher matcher = mainPattern.matcher(line);
          if (!matcher.find()) {
            continue;
          }
          if (groupIndex > matcher.groupCount()) {
            return applyInvertIfRequired(
                new LogRegexResult(
                    false,
                    extractedValues,
                    "groupIndex out of range. groupIndex: "
                        + groupIndex
                        + ", groupCount: "
                        + matcher.groupCount()),
                invertResult);
          }
          final String extracted = (groupIndex == 0) ? matcher.group(0) : matcher.group(groupIndex);
          extractedValues.add(extracted);
          if (Objects.nonNull(expectedValue)) {
            final boolean ok = expectedValue.equals(extracted);
            return applyInvertIfRequired(
                new LogRegexResult(
                    ok,
                    extractedValues,
                    ok
                        ? null
                        : ("extracted value did not match expected. expected: "
                            + expectedValue
                            + ", actual: "
                            + extracted)),
                invertResult);
          }
          return applyInvertIfRequired(
              new LogRegexResult(true, extractedValues, null), invertResult);
        }
      }
      if (Objects.nonNull(expectedValue)) {
        return applyInvertIfRequired(
            new LogRegexResult(
                false,
                extractedValues,
                "no matching line found for expected value: " + expectedValue),
            invertResult);
      }
      return applyInvertIfRequired(
          new LogRegexResult(
              !extractedValues.isEmpty(),
              extractedValues,
              extractedValues.isEmpty() ? "no matches found" : null),
          invertResult);
    } catch (final IOException ioException) {
      return applyInvertIfRequired(
          new LogRegexResult(
              false, extractedValues, "failed to read log file: " + ioException.getMessage()),
          invertResult);
    } catch (final Exception exception) {
      return applyInvertIfRequired(
          new LogRegexResult(false, extractedValues, "unexpected error: " + exception.getMessage()),
          invertResult);
    }
  }

  private static LogRegexResult applyInvertIfRequired(
      final LogRegexResult result, final boolean invertResult) {
    if (!invertResult) {
      return result;
    }
    return new LogRegexResult(!result.matched(), result.extractedValues(), result.reason());
  }

  public record LogRegexResult(boolean matched, List<String> extractedValues, String reason) {}
}
