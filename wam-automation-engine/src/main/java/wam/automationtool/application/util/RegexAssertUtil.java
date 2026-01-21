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

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexAssertUtil {

  private RegexAssertUtil() {}

  /**
   * @param regexGroupIndex group index to extract (0 = whole match, 1..n = capturing groups)
   * @param valueToProcess input string to run the regex against
   * @param expectedRegex regex pattern to apply (can include wildcards, groups, etc.)
   * @param invertResult if true, final boolean result is inverted
   * @return boolean result (false on any failure)
   */
  public static boolean assertByRegexGroup(
      final int regexGroupIndex,
      final String valueToProcess,
      final String expectedRegex,
      final boolean invertResult) {
    boolean result = false;
    try {
      // Basic validation (any failure => false)
      if (regexGroupIndex < 0 || Objects.isNull(valueToProcess) || Objects.isNull(expectedRegex)) {
        result = false;
      } else {
        // DOTALL helps when expectedRegex uses .* across line breaks too
        final Pattern pattern = Pattern.compile(expectedRegex, Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(valueToProcess);
        String lastExtracted = null;
        // We use the LAST match to be flexible when the pattern can occur multiple times.
        while (matcher.find()) {
          // group(0) is always valid if find() is true.
          // For group(1..n), it must exist.
          if (regexGroupIndex == 0) {
            lastExtracted = matcher.group(0);
          } else if (regexGroupIndex <= matcher.groupCount()) {
            lastExtracted = matcher.group(regexGroupIndex);
          }
          // If group index is higher than groupCount, we ignore this match and keep searching.
        }
        // success if we extracted something from at least one match
        result = (lastExtracted != null);
      }
    } catch (final Exception exception) {
      result = false;
    }
    return invertResult != result;
  }
}
