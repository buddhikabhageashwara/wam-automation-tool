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

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RenderedCssChecker {

  private static final Logger log = LoggerFactory.getLogger(RenderedCssChecker.class);

  private RenderedCssChecker() {}

  /**
   * Rendered CSS Attribute Checker (Theme Verification)
   *
   * <h2>Rendered Value Strategy</h2>
   *
   * <ul>
   *   <li><b>Default (most attributes):</b> Reads the <i>rendered/computed</i> value via {@code
   *       window.getComputedStyle(element).getPropertyValue(cssAttribute)}.
   *   <li><b>Geometry-based rendered values:</b> For {@code width} and {@code height}, reads the
   *       real on-screen size via {@code element.getBoundingClientRect()} and returns values in
   *       {@code px}.
   * </ul>
   *
   * <h2>Supported CSS Attribute List</h2>
   *
   * <p>The checker supports any valid CSS property name accepted by {@code
   * getComputedStyle().getPropertyValue(...)}. Below is the recommended list of attributes commonly
   * used for UI/theme verification.
   *
   * <h3>Colors (normalized comparison: HEX or RGB/RGBA expected is allowed)</h3>
   *
   * <ul>
   *   <li>{@code color}
   *   <li>{@code background-color}
   *   <li>{@code border-top-color}
   *   <li>{@code border-right-color}
   *   <li>{@code border-bottom-color}
   *   <li>{@code border-left-color}
   *   <li>{@code border-color} (shorthand output depends on browser; prefer side-specific)
   *   <li>{@code outline-color}
   *   <li>{@code caret-color}
   *   <li>{@code accent-color} (checkbox/radio in modern browsers)
   *   <li>{@code box-shadow} (contains color; compare carefully, usually string match)
   *   <li>{@code text-shadow} (contains color; compare carefully, usually string match)
   * </ul>
   *
   * <h3>Borders</h3>
   *
   * <ul>
   *   <li>{@code border-top-width}, {@code border-right-width}, {@code border-bottom-width}, {@code
   *       border-left-width}
   *   <li>{@code border-top-style}, {@code border-right-style}, {@code border-bottom-style}, {@code
   *       border-left-style}
   *   <li>{@code border-top-left-radius}, {@code border-top-right-radius}, {@code
   *       border-bottom-left-radius}, {@code border-bottom-right-radius}
   *   <li>{@code border-radius} (shorthand; prefer corner-specific for stable checks)
   * </ul>
   *
   * <h3>Typography (Rendered font handling)</h3>
   *
   * <ul>
   *   <li>{@code font-family} (checker compares <b>rendered font only</b>: first font name from
   *       computed value)
   *   <li>{@code font-size}
   *   <li>{@code font-weight}.3
   *   <li>{@code font-style}
   *   <li>{@code font-variant}
   *   <li>{@code line-height}
   *   <li>{@code letter-spacing}
   *   <li>{@code word-spacing}
   *   <li>{@code text-align}
   *   <li>{@code text-transform}
   *   <li>{@code text-decoration-line}
   *   <li>{@code text-decoration-style}
   *   <li>{@code text-decoration-color}
   * </ul>
   *
   * <h3>Spacing</h3>
   *
   * <ul>
   *   <li>{@code padding-top}, {@code padding-right}, {@code padding-bottom}, {@code padding-left}
   *   <li>{@code margin-top}, {@code margin-right}, {@code margin-bottom}, {@code margin-left}
   *   <li>{@code gap}, {@code row-gap}, {@code column-gap} (flex/grid spacing)
   * </ul>
   *
   * <h3>Size (Geometry / Computed)</h3>
   *
   * <ul>
   *   <li>{@code width} (geometry-based: {@code getBoundingClientRect().width})
   *   <li>{@code height} (geometry-based: {@code getBoundingClientRect().height})
   *   <li>{@code min-width}, {@code max-width}, {@code min-height}, {@code max-height} (computed
   *       style)
   *   <li>{@code box-sizing}
   * </ul>
   *
   * <h3>Layout & Display</h3>
   *
   * <ul>
   *   <li>{@code display}
   *   <li>{@code position}
   *   <li>{@code top}, {@code right}, {@code bottom}, {@code left} (computed; may be {@code auto}
   *       depending on layout)
   *   <li>{@code z-index}
   *   <li>{@code overflow}, {@code overflow-x}, {@code overflow-y}
   *   <li>{@code visibility}
   *   <li>{@code opacity}
   * </ul>
   *
   * <h3>Flexbox (when verifying layout rules)</h3>
   *
   * <ul>
   *   <li>{@code display} (should be {@code flex} or {@code inline-flex})
   *   <li>{@code flex-direction}
   *   <li>{@code justify-content}
   *   <li>{@code align-items}
   *   <li>{@code flex-wrap}
   *   <li>{@code align-content}
   * </ul>
   *
   * <h3>Grid (when verifying layout rules)</h3>
   *
   * <ul>
   *   <li>{@code display} (should be {@code grid} or {@code inline-grid})
   *   <li>{@code grid-template-columns}
   *   <li>{@code grid-template-rows}
   *   <li>{@code grid-column-gap} / {@code column-gap}
   *   <li>{@code grid-row-gap} / {@code row-gap}
   * </ul>
   *
   * <h3>Interaction / UX</h3>
   *
   * <ul>
   *   <li>{@code cursor}
   *   <li>{@code pointer-events}
   *   <li>{@code user-select}
   *   <li>{@code transition} (string match; may vary by browser)
   * </ul>
   *
   * <h3>Notes for Stable Assertions</h3>
   *
   * <ul>
   *   <li>Prefer <b>side-specific border properties</b> over shorthands to avoid browser formatting
   *       differences.
   *   <li>Colors are compared after normalization to <b>{@code #RRGGBB}</b> (alpha is ignored in
   *       normalization).
   *   <li>{@code box-shadow} / {@code text-shadow} and {@code transition} are often best checked
   *       using exact strings only if your UI is consistent across browsers.
   *   <li>{@code top/right/bottom/left} may return {@code auto} depending on positioning; use
   *       geometry checks if you need physical placement verification.
   * </ul>
   */
  public static Result checkRenderedCss(
      final WebDriver driver,
      final WebElement element,
      final String cssAttributeName,
      final String expectedValue) {
    log.info(
        "Rendered CSS check start | attr='{}' | expected='{}' | tag='{}' | id='{}' | name='{}' | class='{}'",
        safe(cssAttributeName),
        safe(expectedValue),
        safe(getSafeTag(element)),
        safe(getSafeAttr(element, "id")),
        safe(getSafeAttr(element, "name")),
        safe(getSafeAttr(element, "class")));

    try {
      Objects.requireNonNull(driver, "driver");
      Objects.requireNonNull(element, "element");
      Objects.requireNonNull(cssAttributeName, "cssAttributeName");

      final String attr = cssAttributeName.trim();
      final String expectedRaw = safe(expectedValue);

      // 1) Always read rendered value (computed style OR geometry when needed)
      final String actualRendered = readRenderedValue(driver, element, attr);

      // 2) Font: compare rendered font only (first font)
      if (isFontFamily(attr)) {
        final String actualFont = firstFontOnly(actualRendered);
        final String expectedFont = normalizeFontName(expectedRaw);

        final boolean pass = expectedFont.equalsIgnoreCase(actualFont);
        final Result result =
            new Result(
                attr,
                expectedFont,
                actualFont,
                pass,
                pass ? "" : ("Rendered font mismatch. Computed=" + actualRendered));

        log.info(
            "Rendered CSS check end | attr='{}' | passed={} | actual='{}' | note='{}'",
            result.cssAttribute,
            result.passed,
            result.actualRendered,
            safe(result.note));
        return result;
      }

      // 3) Colors: normalize both sides to HEX and compare
      if (isColorAttribute(attr) || looksLikeColor(expectedRaw)) {
        final String actualHex = normalizeToHex(actualRendered);
        final String expectedHex = normalizeToHex(expectedRaw);

        if (expectedHex.isBlank()) {
          final Result result =
              new Result(
                  attr,
                  expectedRaw,
                  actualRendered,
                  false,
                  "Expected color format not recognized (use HEX like #FF6600 or RGB like rgb(255,102,0))");

          log.info(
              "Rendered CSS check end | attr='{}' | passed={} | actual='{}' | note='{}'",
              result.cssAttribute,
              result.passed,
              result.actualRendered,
              safe(result.note));
          return result;
        }

        if (actualHex.isBlank()) {
          final Result result =
              new Result(
                  attr,
                  expectedHex,
                  actualRendered,
                  false,
                  "Actual rendered color could not be normalized");

          log.info(
              "Rendered CSS check end | attr='{}' | passed={} | actual='{}' | note='{}'",
              result.cssAttribute,
              result.passed,
              result.actualRendered,
              safe(result.note));
          return result;
        }

        final boolean pass = expectedHex.equalsIgnoreCase(actualHex);
        final Result result =
            new Result(
                attr,
                expectedHex,
                actualRendered + " (normalized=" + actualHex + ")",
                pass,
                pass ? "" : "Color mismatch");

        log.info(
            "Rendered CSS check end | attr='{}' | passed={} | actual='{}' | note='{}'",
            result.cssAttribute,
            result.passed,
            result.actualRendered,
            safe(result.note));
        return result;
      }

      // 4) Default: exact match on rendered value string
      final boolean pass = expectedRaw.equals(actualRendered);
      final Result result =
          new Result(attr, expectedRaw, actualRendered, pass, pass ? "" : "Mismatch");

      log.info(
          "Rendered CSS check end | attr='{}' | passed={} | actual='{}' | note='{}'",
          result.cssAttribute,
          result.passed,
          result.actualRendered,
          safe(result.note));
      return result;

    } catch (final Exception ex) {
      log.error(
          "Rendered CSS check failed | attr='{}' | expected='{}' | tag='{}' | id='{}' | name='{}' | class='{}' | error='{}'",
          safe(cssAttributeName),
          safe(expectedValue),
          safe(getSafeTag(element)),
          safe(getSafeAttr(element, "id")),
          safe(getSafeAttr(element, "name")),
          safe(getSafeAttr(element, "class")),
          ex.getMessage(),
          ex);
      throw ex;
    }
  }

  /**
   * Always returns rendered value: - width/height -> bounding client rect (real pixels user sees) -
   * everything else -> computed style property value
   */
  private static String readRenderedValue(
      final WebDriver driver, final WebElement element, final String cssName) {
    final String prop = cssName.toLowerCase(Locale.ROOT).trim();

    // Real rendered geometry values (avoid "auto" and layout ambiguity)
    if ("width".equals(prop) || "height".equals(prop)) {
      return readBoundingRect(driver, element, prop);
    }

    // Most CSS props are best from computed style (rendered/computed value)
    return readComputedStyle(driver, element, cssName);
  }

  // ----------------- rendered value reader -----------------

  private static String readBoundingRect(
      final WebDriver driver, final WebElement element, final String dim) {
    final JavascriptExecutor js = (JavascriptExecutor) driver;
    final Object val =
        js.executeScript(
            "const r = arguments[0].getBoundingClientRect(); "
                + "return arguments[1] === 'width' ? r.width : r.height;",
            element,
            dim);

    // Keep it stable: return like "160px" (rounded to 2 decimals)
    final double d = toDouble(val);
    final double rounded = Math.round(d * 100.0) / 100.0;
    // Many teams prefer integer pixels; if you want that, change to Math.round(d)
    return stripTrailingZeros(rounded) + "px";
  }

  private static String readComputedStyle(
      final WebDriver driver, final WebElement element, final String cssName) {
    final JavascriptExecutor js = (JavascriptExecutor) driver;
    final Object val =
        js.executeScript(
            "return window.getComputedStyle(arguments[0]).getPropertyValue(arguments[1]);",
            element,
            cssName);
    return safe(val == null ? "" : val.toString());
  }

  private static boolean isFontFamily(final String attr) {
    final String a = attr.toLowerCase(Locale.ROOT);
    return a.equals("font-family") || a.equals("fontfamily");
  }

  // ----------------- compare helpers -----------------

  private static boolean isColorAttribute(final String attr) {
    final String a = attr.toLowerCase(Locale.ROOT);
    return a.equals("color")
        || a.equals("background-color")
        || a.endsWith("-color")
        || a.contains("shadow");
  }

  private static boolean looksLikeColor(final String v) {
    final String s = safe(v).toLowerCase(Locale.ROOT);
    return s.startsWith("#") || s.startsWith("rgb(") || s.startsWith("rgba(");
  }

  // Normalize HEX or RGB(A) -> "#RRGGBB" (alpha ignored)
  private static String normalizeToHex(final String value) {
    final String v = safe(value).toLowerCase(Locale.ROOT);
    if (v.isBlank()) return "";

    if (v.startsWith("#") || v.matches("^[0-9a-f]{3}$") || v.matches("^[0-9a-f]{6}$")) {
      return normalizeHex(v);
    }

    final Pattern p =
        Pattern.compile(
            "rgba?\\s*\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)(?:\\s*,\\s*([0-9.]+))?\\s*\\)");
    final Matcher m = p.matcher(v);
    if (m.find()) {
      final int r = clamp255(parseInt(m.group(1)));
      final int g = clamp255(parseInt(m.group(2)));
      final int b = clamp255(parseInt(m.group(3)));
      return String.format("#%02X%02X%02X", r, g, b);
    }

    return "";
  }

  private static String normalizeHex(final String hex) {
    String h = safe(hex).toUpperCase(Locale.ROOT);
    if (h.isBlank()) return "";

    if (!h.startsWith("#")) h = "#" + h;
    h = h.replaceAll("\\s+", "");

    if (h.matches("^#[0-9A-F]{3}$")) {
      final char r = h.charAt(1);
      final char g = h.charAt(2);
      final char b = h.charAt(3);
      h = "#" + r + r + g + g + b + b;
    }

    if (!h.matches("^#[0-9A-F]{6}$")) return "";
    return h;
  }

  private static String firstFontOnly(final String computedFontFamily) {
    final String s = safe(computedFontFamily);
    if (s.isBlank()) return "";

    final String first = s.split(",")[0].trim();
    return normalizeFontName(first);
  }

  private static String normalizeFontName(final String s) {
    String v = safe(s).trim();
    v = v.replaceAll("^['\"]|['\"]$", "");
    return v.trim();
  }

  private static int parseInt(final String s) {
    try {
      return Integer.parseInt(s.trim());
    } catch (final Exception e) {
      return 0;
    }
  }

  // ----------------- misc helpers -----------------

  private static int clamp255(final int v) {
    if (v < 0) return 0;
    return Math.min(v, 255);
  }

  private static String safe(final String s) {
    return s == null ? "" : s.trim();
  }

  private static String getSafeTag(final WebElement element) {
    try {
      return element == null ? "" : element.getTagName();
    } catch (final Exception e) {
      return "";
    }
  }

  private static String getSafeAttr(final WebElement element, final String attrName) {
    try {
      return element == null ? "" : element.getAttribute(attrName);
    } catch (final Exception e) {
      return "";
    }
  }

  private static double toDouble(final Object val) {
    if (val == null) return 0.0;
    try {
      return Double.parseDouble(val.toString());
    } catch (final Exception e) {
      return 0.0;
    }
  }

  private static String stripTrailingZeros(final double value) {
    final String s = String.valueOf(value);
    if (s.endsWith(".0")) return s.substring(0, s.length() - 2);
    return s;
  }

  public record Result(
      String cssAttribute, String expected, String actualRendered, boolean passed, String note) {}
}
