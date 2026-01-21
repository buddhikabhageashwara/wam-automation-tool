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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AutoLocatorDetector
 *
 * <p>Responsibilities:
 *
 * <ol>
 *   <li>Export DOM element metadata to CSV (attributes + validated XPath + optional validated CSS
 *       selector).
 *   <li>Self-heal an XPath by validating a previous XPath and, if broken, re-identifying the
 *       element using a provided HTML snippet string and generating a working XPath.
 * </ol>
 *
 * <p>Logging policy (less noisy):
 *
 * <ul>
 *   <li>Start + end logs for the two public methods.
 *   <li>Warn/error logs only for not-found or unexpected errors.
 * </ul>
 */
public final class AutoLocatorDetector {

  /**
   * Placeholder token to mark dynamic portions inside HTML snippet text. Example:
   * "Yes(IGNORE_ME_IGNORE_ME)" should match "Yes(1)" or "Yes(2)".
   */
  public static final String IGNORE_TOKEN = "IGNORE_ME_IGNORE_ME";

  private static final Logger log = LoggerFactory.getLogger(AutoLocatorDetector.class);
  private static final Pattern NON_LETTER_START = Pattern.compile("^[^A-Za-z]+");
  private static final Pattern ATTR_PATTERN =
      Pattern.compile("([a-zA-Z_:][a-zA-Z0-9_:\\-\\.]*)\\s*=\\s*([\"'])(.*?)\\2");

  private static final String ABSOLUTE_XPATH_JS =
      "function absoluteXPath(el) {"
          + "  if (!el || el.nodeType !== 1) return '';"
          + "  if (el.id) return '//*[@id=\"' + el.id + '\"]';"
          + "  const parts = [];"
          + "  while (el && el.nodeType === 1) {"
          + "    let ix = 1;"
          + "    let sib = el.previousSibling;"
          + "    while (sib) {"
          + "      if (sib.nodeType === 1 && sib.nodeName === el.nodeName) ix++;"
          + "      sib = sib.previousSibling;"
          + "    }"
          + "    const tagName = el.nodeName.toLowerCase();"
          + "    parts.unshift(tagName + '[' + ix + ']');"
          + "    el = el.parentNode;"
          + "  }"
          + "  return '/' + parts.join('/');"
          + "}"
          + "return absoluteXPath(arguments[0]);";

  private static final String ABSOLUTE_CSS_JS =
      "function cssPath(el) {"
          + "  if (!el || el.nodeType !== 1) return '';"
          + "  if (el.id) return '#' + el.id;"
          + "  const path = [];"
          + "  while (el && el.nodeType === 1) {"
          + "    let sel = el.nodeName.toLowerCase();"
          + "    if (el.parentElement) {"
          + "      const siblings = Array.from(el.parentElement.children).filter(e => e.nodeName === el.nodeName);"
          + "      if (siblings.length > 1) {"
          + "        const idx = siblings.indexOf(el) + 1;"
          + "        sel += ':nth-of-type(' + idx + ')';"
          + "      }"
          + "    }"
          + "    path.unshift(sel);"
          + "    el = el.parentElement;"
          + "  }"
          + "  return path.join(' > ');"
          + "}"
          + "return cssPath(arguments[0]);";

  private AutoLocatorDetector() {}

  // ============================================================
  // Public API - Export
  // ============================================================

  /**
   * Export current DOM elements to a CSV file.
   *
   * <p>CSV columns:
   *
   * <pre>
   * recordId, groupId, incrementalNumber, isConsider, expectedValue, elementText, elementType, elementHtml,
   * xpath, xpathIsValid, xpathMatchCount,
   * cssSelector, cssIsValid, cssMatchCount,
   * attributeName, attributeValue
   * </pre>
   *
   * <p>Defaults per row:
   *
   * <ul>
   *   <li>{@code isConsider} = "true"
   *   <li>{@code expectedValue} = "" (empty)
   * </ul>
   */
  public static void exportDomToCsv(
      final WebDriver driver,
      final Path csvPath,
      final long startNumber,
      final boolean onlyInteractive,
      final boolean includeCssSelector) {

    Objects.requireNonNull(driver, "driver");
    Objects.requireNonNull(csvPath, "csvPath");

    log.info(
        "AutoLocatorDetector.exportDomToCsv START | csvPath={} | startNumber={} | onlyInteractive={} | includeCssSelector={}",
        csvPath,
        startNumber,
        onlyInteractive,
        includeCssSelector);

    final long start = System.currentTimeMillis();

    waitForDomReady(driver);

    final List<WebElement> elements = driver.findElements(By.cssSelector("*"));
    final JavascriptExecutor js = (JavascriptExecutor) driver;

    ensureParentDirectoryExists(csvPath);

    int exportedElements = 0;
    int skippedElements = 0;

    try (final BufferedWriter writer =
        Files.newBufferedWriter(
            csvPath,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)) {
      writeExportHeader(writer);

      long counter = startNumber;

      for (final WebElement el : elements) {
        try {
          if (!isUsable(el)) {
            skippedElements++;
            continue;
          }

          final String tag = safeTag(el);
          if (tag.isEmpty()) {
            skippedElements++;
            continue;
          }

          if (onlyInteractive && !isInteractive(el, tag)) {
            skippedElements++;
            continue;
          }

          exportOneElement(writer, driver, js, el, tag, counter, includeCssSelector);
          counter++;
          exportedElements++;

        } catch (final StaleElementReferenceException ignored) {
          skippedElements++;
        } catch (final JavascriptException ignored) {
          skippedElements++;
        }
      }

    } catch (final IOException e) {
      log.error("AutoLocatorDetector.exportDomToCsv ERROR | csvPath={}", csvPath, e);
      throw new RuntimeException("Failed to write CSV to: " + csvPath, e);
    }

    final long ms = System.currentTimeMillis() - start;
    log.info(
        "AutoLocatorDetector.exportDomToCsv END | exportedElements={} | skippedElements={} | totalDomElements={} | tookMs={} | output={}",
        exportedElements,
        skippedElements,
        elements.size(),
        ms,
        csvPath.toAbsolutePath());
  }

  // ============================================================
  // Public API - Self Heal
  // ============================================================

  /**
   * Self-heal an XPath using an HTML snippet string (live search).
   *
   * <p>Dynamic text support:
   *
   * <ul>
   *   <li>If the provided HTML snippet visible text contains {@link #IGNORE_TOKEN}, it is treated
   *       as a wildcard.
   *   <li>Example snippet text: {@code "Yes(IGNORE_ME_IGNORE_ME)"} will match DOM texts like {@code
   *       "Yes(1)"} or {@code "Yes(2)"}.
   * </ul>
   *
   * @param driver WebDriver already on the correct page
   * @param elementHtml HTML snippet string (quotes may be escaped in JSON)
   * @param indexToPick 0-based selection if multiple matches exist
   * @return Working XPath found via live DOM search
   * @throws NoSuchElementException if no element matches or index out of range
   */
  public static String selfHealXPathByHtml(
      final WebDriver driver, final String elementHtml, final int indexToPick) {

    Objects.requireNonNull(driver, "driver");
    Objects.requireNonNull(elementHtml, "elementHtml");

    final String htmlTrim = elementHtml.trim();
    log.info(
        "START | indexToPick={} | elementHtmlLength={} | elementHtmlPreview={}",
        indexToPick,
        htmlTrim.length(),
        htmlTrim.length() <= 180 ? htmlTrim : htmlTrim.substring(0, 180) + "...");

    final long start = System.currentTimeMillis();

    waitForDomReady(driver);

    final JavascriptExecutor js = (JavascriptExecutor) driver;

    // 1) Parse signature from HTML snippet
    final HtmlSignature sig;
    try {
      sig = HtmlSignature.parse(elementHtml);
    } catch (final RuntimeException ex) {
      log.error("ERROR | invalid elementHtml snippet", ex);
      throw ex;
    }

    // 2) Find matches in DOM (live search)
    final List<WebElement> matches = findElementsByHtmlSignature(driver, sig);

    if (matches.isEmpty()) {
      log.warn(
          "NOT_FOUND | tag={} | attrs={} | stableClasses={} | textPattern=[{}]",
          sig.tag,
          sig.attrs,
          sig.stableClassTokens,
          sig.visibleTextPattern);
      throw new NoSuchElementException(
          "No element matched HTML signature. tag="
              + sig.tag
              + ", attrs="
              + sig.attrs
              + ", stableClasses="
              + sig.stableClassTokens
              + ", textPattern=["
              + sig.visibleTextPattern
              + "]");
    }

    if (indexToPick < 0 || indexToPick >= matches.size()) {
      log.warn("NOT_FOUND | indexToPick={} out of range | matches={}", indexToPick, matches.size());
      throw new NoSuchElementException(
          "indexToPick=" + indexToPick + " out of range. Matches found: " + matches.size());
    }

    final WebElement picked = matches.get(indexToPick);

    // 3) Generate healed XPath + validate
    final Map<String, String> attrs = getAllAttributes(js, picked);
    final String tag = safeTag(picked);

    final SelectorValidationResult healed =
        buildPreferredXPathValidated(driver, js, picked, tag, attrs);

    final long ms = System.currentTimeMillis() - start;
    log.info(
        "END | matches={} | pickedIndex={} | healedMatchCount={} | healedIsValid={} | tookMs={}",
        matches.size(),
        indexToPick,
        healed.matchCount,
        healed.isValid,
        ms);

    return healed.selector;
  }

  // ============================================================
  // Export internals
  // ============================================================

  private static void exportOneElement(
      final BufferedWriter writer,
      final WebDriver driver,
      final JavascriptExecutor js,
      final WebElement el,
      final String tag,
      final long incrementalNumber,
      final boolean includeCssSelector)
      throws IOException {

    final String groupId = UUID.randomUUID().toString();

    // New columns
    final String isConsider = "true";
    final String expectedValue = "";

    final String elementType = toElementType(tag, el);
    final String elementText = safeText(el);

    // New column: elementHtml
    final String elementHtml = safeOuterHtml(js, el);

    final Map<String, String> attrs = getAllAttributes(js, el);

    final SelectorValidationResult xpRes = buildPreferredXPathValidated(driver, js, el, tag, attrs);

    final SelectorValidationResult cssRes =
        includeCssSelector
            ? buildPreferredCssSelectorValidated(driver, js, el, tag, attrs)
            : new SelectorValidationResult("", false, 0);

    if (attrs.isEmpty()) {
      writeExportRow(
          writer,
          groupId,
          incrementalNumber,
          isConsider,
          expectedValue,
          elementText,
          elementType,
          elementHtml,
          xpRes,
          cssRes,
          "",
          "");
      return;
    }

    for (final Map.Entry<String, String> entry : attrs.entrySet()) {
      writeExportRow(
          writer,
          groupId,
          incrementalNumber,
          isConsider,
          expectedValue,
          elementText,
          elementType,
          elementHtml,
          xpRes,
          cssRes,
          entry.getKey(),
          entry.getValue());
    }
  }

  private static void writeExportHeader(final BufferedWriter writer) throws IOException {
    writeCsvRow(
        writer,
        "recordId",
        "groupId",
        "incrementalNumber",
        "isConsider",
        "expectedValue",
        "elementText",
        "elementType",
        "elementHtml",
        "xpath",
        "xpathIsValid",
        "xpathMatchCount",
        "cssSelector",
        "cssIsValid",
        "cssMatchCount",
        "attributeName",
        "attributeValue");
  }

  private static void writeExportRow(
      final BufferedWriter writer,
      final String groupId,
      final long incrementalNumber,
      final String isConsider,
      final String expectedValue,
      final String elementText,
      final String elementType,
      final String elementHtml,
      final SelectorValidationResult xpRes,
      final SelectorValidationResult cssRes,
      final String attributeName,
      final String attributeValue)
      throws IOException {

    writeCsvRow(
        writer,
        UUID.randomUUID().toString(), // recordId
        groupId,
        String.valueOf(incrementalNumber),
        isConsider,
        expectedValue,
        elementText,
        elementType,
        elementHtml,
        xpRes.selector,
        String.valueOf(xpRes.isValid),
        String.valueOf(xpRes.matchCount),
        cssRes.selector,
        String.valueOf(cssRes.isValid),
        String.valueOf(cssRes.matchCount),
        attributeName,
        attributeValue);
  }

  // ============================================================
  // HTML Signature (Self Heal)
  // ============================================================

  @SuppressWarnings("unchecked")
  private static List<WebElement> findElementsByHtmlSignature(
      final WebDriver driver, final HtmlSignature sig) {

    final JavascriptExecutor js = (JavascriptExecutor) driver;

    // We do wildcard matching in JS for speed and accuracy against DOM text.
    final Object res =
        js.executeScript(
            "var tag = arguments[0];"
                + "var attrs = arguments[1] || {};"
                + "var classTokens = arguments[2] || [];"
                + "var pattern = arguments[3] || '';"
                + "var IGNORE = arguments[4] || '';"
                + "var nodeList = document.querySelectorAll(tag);"
                + "var els = [];"
                + "for (var i = 0; i < nodeList.length; i++) els.push(nodeList[i]);"
                + "function splitWs(s){"
                + "  return (s || '').split(/\\s+/);"
                + "}"
                + "function hasAllClasses(el, tokens){"
                + "  if (!tokens || tokens.length === 0) return true;"
                + "  var cls = splitWs(el.getAttribute('class'));"
                + "  for (var i = 0; i < tokens.length; i++){"
                + "    if (cls.indexOf(tokens[i]) === -1) return false;"
                + "  }"
                + "  return true;"
                + "}"
                + "function visibleText(el){"
                + "  var t = (el.innerText || el.textContent || '');"
                + "  return ('' + t).replace(/\\s+/g, ' ').replace(/^\\s+|\\s+$/g, '');"
                + "}"
                + "function matchText(actual, pattern){"
                + "  if (!pattern || pattern.length === 0) return true;"
                + "  if (pattern.indexOf(IGNORE) === -1) return actual === pattern;"
                + "  var parts = pattern.split(IGNORE);"
                + "  var hasNonEmpty = false;"
                + "  for (var i = 0; i < parts.length; i++){"
                + "    if (parts[i] && parts[i].length > 0){ hasNonEmpty = true; break; }"
                + "  }"
                + "  if (!hasNonEmpty) return true;"
                + "  if (parts[0] && parts[0].length > 0){"
                + "    if (actual.indexOf(parts[0]) !== 0) return false;"
                + "  }"
                + "  var pos = (parts[0] && parts[0].length > 0) ? parts[0].length : 0;"
                + "  for (var j = 1; j < parts.length; j++){"
                + "    var seg = parts[j] || '';"
                + "    if (seg.length === 0) continue;"
                + "    var idx = actual.indexOf(seg, pos);"
                + "    if (idx === -1) return false;"
                + "    pos = idx + seg.length;"
                + "  }"
                + "  var last = parts[parts.length - 1] || '';"
                + "  if (last.length > 0){"
                + "    var endPos = actual.length - last.length;"
                + "    if (endPos < 0) return false;"
                + "    if (actual.lastIndexOf(last) !== endPos) return false;"
                + "  }"
                + "  return true;"
                + "}"
                + "var out = [];"
                + "for (var e = 0; e < els.length; e++){"
                + "  var el = els[e];"
                + "  for (var k in attrs){"
                + "    if (!Object.prototype.hasOwnProperty.call(attrs, k)) continue;"
                + "    var expected = attrs[k];"
                + "    var actual = el.getAttribute(k) || '';"
                + "    if (actual !== expected){ el = null; break; }"
                + "  }"
                + "  if (!el) continue;"
                + "  if (!hasAllClasses(el, classTokens)) continue;"
                + "  var text = visibleText(el);"
                + "  if (!matchText(text, pattern)) continue;"
                + "  out.push(el);"
                + "}"
                + "return out;",
            sig.tag,
            sig.attrs,
            sig.stableClassTokens,
            sig.visibleTextPattern,
            IGNORE_TOKEN);

    if (res instanceof List<?>) {
      return (List<WebElement>) res;
    }
    return Collections.emptyList();
  }

  // ============================================================
  // Signature matching (supports IGNORE_TOKEN wildcard)
  // ============================================================

  private static SelectorValidationResult buildPreferredXPathValidated(
      final WebDriver driver,
      final JavascriptExecutor js,
      final WebElement el,
      final String tag,
      final Map<String, String> attrs) {

    final List<String> candidates = new ArrayList<>();

    addIfNotBlank(candidates, byAttrXPath(tag, "id", attrs.get("id")));
    addIfNotBlank(candidates, byAttrXPath(tag, "data-testid", attrs.get("data-testid")));
    addIfNotBlank(candidates, byAttrXPath(tag, "name", attrs.get("name")));
    addIfNotBlank(candidates, byAttrXPath(tag, "aria-label", attrs.get("aria-label")));

    final String token = pickStableTextToken(safeText(el));
    if (!token.isEmpty()) {
      candidates.add(
          "//" + tag + "[.//*[contains(normalize-space(), " + xpathLiteral(token) + ")]]");
    }

    final String abs = safeJsString(js, ABSOLUTE_XPATH_JS, el);
    addIfNotBlank(candidates, abs);

    for (final String xp : candidates) {
      final SelectorValidationResult vr = validateXpath(driver, js, el, xp);
      if (vr.isValid && vr.matchCount == 1) return vr;
    }

    if (!candidates.isEmpty()) {
      final String last = candidates.get(candidates.size() - 1);
      return validateXpath(driver, js, el, last);
    }

    return new SelectorValidationResult("", false, 0);
  }

  // ============================================================
  // Selector validation result
  // ============================================================

  private static SelectorValidationResult validateXpath(
      final WebDriver driver,
      final JavascriptExecutor js,
      final WebElement target,
      final String xpath) {
    try {
      final List<WebElement> found = driver.findElements(By.xpath(xpath));
      final int count = found.size();
      if (count != 1) return new SelectorValidationResult(xpath, false, count);

      final boolean same = domFingerprint(js, target).equals(domFingerprint(js, found.get(0)));
      return new SelectorValidationResult(xpath, same, count);
    } catch (final Exception e) {
      return new SelectorValidationResult(xpath, false, 0);
    }
  }

  // ============================================================
  // XPath generation + validation
  // ============================================================

  private static String byAttrXPath(final String tag, final String attr, final String value) {
    if (value == null || value.isBlank()) return "";
    return "//" + tag + "[@" + attr + "=" + xpathLiteral(value) + "]";
  }

  private static SelectorValidationResult buildPreferredCssSelectorValidated(
      final WebDriver driver,
      final JavascriptExecutor js,
      final WebElement el,
      final String tag,
      final Map<String, String> attrs) {

    final List<String> candidates = new ArrayList<>();

    final String id = attrs.getOrDefault("id", "");
    if (!id.isBlank()) candidates.add("#" + cssEscape(id));

    final String testId = attrs.getOrDefault("data-testid", "");
    if (!testId.isBlank()) candidates.add(tag + "[data-testid=" + cssLiteral(testId) + "]");

    final String name = attrs.getOrDefault("name", "");
    if (!name.isBlank()) candidates.add(tag + "[name=" + cssLiteral(name) + "]");

    final String aria = attrs.getOrDefault("aria-label", "");
    if (!aria.isBlank()) candidates.add(tag + "[aria-label=" + cssLiteral(aria) + "]");

    final String stableClass = pickStableClass(attrs.getOrDefault("class", ""));
    if (!stableClass.isBlank()) candidates.add(tag + "." + cssEscape(stableClass));

    final String cssPath = safeJsString(js, ABSOLUTE_CSS_JS, el);
    addIfNotBlank(candidates, cssPath);

    for (final String css : candidates) {
      final SelectorValidationResult vr = validateCss(driver, js, el, css);
      if (vr.isValid && vr.matchCount == 1) return vr;
    }

    if (!candidates.isEmpty()) {
      final String last = candidates.get(candidates.size() - 1);
      return validateCss(driver, js, el, last);
    }

    return new SelectorValidationResult("", false, 0);
  }

  private static SelectorValidationResult validateCss(
      final WebDriver driver,
      final JavascriptExecutor js,
      final WebElement target,
      final String css) {
    try {
      final List<WebElement> found = driver.findElements(By.cssSelector(css));
      final int count = found.size();
      if (count != 1) return new SelectorValidationResult(css, false, count);

      final boolean same = domFingerprint(js, target).equals(domFingerprint(js, found.get(0)));
      return new SelectorValidationResult(css, same, count);
    } catch (final Exception e) {
      return new SelectorValidationResult(css, false, 0);
    }
  }

  // ============================================================
  // CSS generation + validation
  // ============================================================

  private static String pickStableClass(final String classAttr) {
    if (classAttr == null || classAttr.isBlank()) return "";
    final String[] parts = classAttr.trim().split("\\s+");
    for (final String c : parts) {
      if (c.isBlank()) continue;
      if (c.startsWith("css-")) continue;
      return c;
    }
    return "";
  }

  @SuppressWarnings("unchecked")
  private static Map<String, String> getAllAttributes(
      final JavascriptExecutor js, final WebElement el) {
    final Object res =
        js.executeScript(
            "const el = arguments[0];"
                + "const out = {};"
                + "if (!el || !el.attributes) return out;"
                + "for (const a of el.attributes) out[a.name] = a.value;"
                + "return out;",
            el);

    final Map<String, String> map = new LinkedHashMap<>();
    if (res instanceof Map<?, ?> raw) {
      for (final Map.Entry<?, ?> e : raw.entrySet()) {
        final String k = String.valueOf(e.getKey());
        final String v = e.getValue() == null ? "" : String.valueOf(e.getValue());
        map.put(k, v);
      }
    }
    return map;
  }

  private static void waitForDomReady(final WebDriver driver) {
    new WebDriverWait(driver, Duration.ofSeconds(20))
        .until(
            d ->
                "complete"
                    .equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    new WebDriverWait(driver, Duration.ofSeconds(20))
        .until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
  }

  // ============================================================
  // Attributes extraction
  // ============================================================

  private static String domFingerprint(final JavascriptExecutor js, final WebElement el) {
    try {
      final Object res =
          js.executeScript(
              "const e = arguments[0];"
                  + "if (!e) return '';"
                  + "const tag = e.tagName ? e.tagName.toLowerCase() : '';"
                  + "const id = e.id || '';"
                  + "const name = e.getAttribute('name') || '';"
                  + "const cls = e.getAttribute('class') || '';"
                  + "const text = (e.innerText || e.textContent || '').trim().slice(0, 120);"
                  + "let idx = 1;"
                  + "if (e.parentElement) {"
                  + "  const kids = Array.from(e.parentElement.children);"
                  + "  idx = kids.indexOf(e) + 1;"
                  + "}"
                  + "return [tag,id,name,cls,text,idx].join('|');",
              el);
      return res == null ? "" : String.valueOf(res);
    } catch (final Exception ex) {
      return "";
    }
  }

  // ============================================================
  // DOM ready / fingerprint / outerHTML
  // ============================================================

  private static String safeOuterHtml(final JavascriptExecutor js, final WebElement el) {
    try {
      final Object res = js.executeScript("return arguments[0] ? arguments[0].outerHTML : '';", el);
      return res == null ? "" : String.valueOf(res);
    } catch (final Exception e) {
      return "";
    }
  }

  private static String safeJsString(
      final JavascriptExecutor js, final String script, final WebElement el) {
    try {
      final Object res = js.executeScript(script, el);
      return res == null ? "" : String.valueOf(res);
    } catch (final Exception e) {
      return "";
    }
  }

  private static String toElementType(final String tag, final WebElement el) {
    return switch (tag) {
      case "a" -> "anchortag";
      case "button" -> "button";
      case "input" -> {
        final String t = safeAttr(el, "type").toLowerCase(Locale.ROOT);
        yield t.isEmpty() ? "input" : "input:" + t;
      }
      case "select" -> "select";
      case "textarea" -> "textarea";
      case "img" -> "image";
      default -> tag;
    };
  }

  private static boolean isUsable(final WebElement el) {
    try {
      el.isEnabled();
      return true;
    } catch (final Exception e) {
      return false;
    }
  }

  // ============================================================
  // Element typing / filters
  // ============================================================

  private static boolean isInteractive(final WebElement el, final String tag) {
    if (tag.equals("a")
        || tag.equals("button")
        || tag.equals("input")
        || tag.equals("select")
        || tag.equals("textarea")) {
      return true;
    }
    final String role = safeAttr(el, "role").toLowerCase(Locale.ROOT);
    if (role.equals("button") || role.equals("link")) return true;

    final String onclick = safeAttr(el, "onclick");
    if (!onclick.isEmpty()) return true;

    final String tabindex = safeAttr(el, "tabindex");
    return !tabindex.isEmpty() && !tabindex.equals("-1");
  }

  private static String safeTag(final WebElement el) {
    try {
      final String t = el.getTagName();
      return t == null ? "" : t.toLowerCase(Locale.ROOT);
    } catch (final Exception e) {
      return "";
    }
  }

  private static String safeText(final WebElement el) {
    try {
      final String t = el.getText();
      return t == null ? "" : t.trim();
    } catch (final Exception e) {
      return "";
    }
  }

  private static String safeAttr(final WebElement el, final String attr) {
    try {
      final String v = el.getAttribute(attr);
      return v == null ? "" : v.trim();
    } catch (final Exception e) {
      return "";
    }
  }

  private static String pickStableTextToken(final String text) {
    if (text == null) return "";
    String t = text.trim();
    if (t.isEmpty()) return "";

    t = NON_LETTER_START.matcher(t).replaceAll("").trim();
    if (t.isEmpty()) return "";

    final String[] parts = t.split("\\s+");
    final List<String> keep = new ArrayList<>();
    for (final String p : parts) {
      if (p.matches(".*\\d.*") || p.contains("$") || p.contains("€") || p.contains("£")) break;
      if (p.matches(".*[A-Za-z].*")) keep.add(p);
      if (keep.size() >= 3) break;
    }

    if (!keep.isEmpty()) return String.join(" ", keep);
    return parts.length > 0 ? parts[0] : "";
  }

  private static String xpathLiteral(final String s) {
    if (s == null) return "''";
    if (!s.contains("'")) return "'" + s + "'";
    if (!s.contains("\"")) return "\"" + s + "\"";

    final StringBuilder sb = new StringBuilder("concat(");
    final char[] arr = s.toCharArray();
    boolean first = true;

    for (final char c : arr) {
      if (!first) sb.append(",");
      first = false;

      if (c == '\'') sb.append("\"").append("'").append("\"");
      else if (c == '"') sb.append("'").append("\"").append("'");
      else sb.append("'").append(c).append("'");
    }

    sb.append(")");
    return sb.toString();
  }

  // ============================================================
  // Text token selection (for preferred XPath style)
  // ============================================================

  private static String cssLiteral(final String value) {
    final String v = value.replace("\"", "\\\"");
    return "\"" + v + "\"";
  }

  // ============================================================
  // XPath / CSS escaping
  // ============================================================

  private static String cssEscape(final String value) {
    return value.replace(" ", "\\ ").replace("#", "\\#").replace(".", "\\.");
  }

  private static void ensureParentDirectoryExists(final Path csvPath) {
    try {
      final Path parent = csvPath.toAbsolutePath().getParent();
      if (parent != null) Files.createDirectories(parent);
    } catch (final IOException e) {
      throw new RuntimeException("Failed to create directories for path: " + csvPath, e);
    }
  }

  private static void writeCsvRow(final BufferedWriter w, final String... cols) throws IOException {
    final StringBuilder sb = new StringBuilder();
    for (int i = 0; i < cols.length; i++) {
      if (i > 0) sb.append(',');
      sb.append(escapeCsv(cols[i]));
    }
    sb.append("\n");
    w.write(sb.toString());
  }

  // ============================================================
  // File + CSV helpers
  // ============================================================

  private static String escapeCsv(String s) {
    if (s == null) s = "";
    final boolean needQuotes =
        s.contains(",") || s.contains("\"") || s.contains("\n") || s.contains("\r");
    final String v = s.replace("\"", "\"\"");
    return needQuotes ? "\"" + v + "\"" : v;
  }

  private static void addIfNotBlank(final List<String> list, final String value) {
    if (value != null && !value.isBlank()) list.add(value);
  }

  /**
   * Signature extracted from a user-provided HTML snippet.
   *
   * <p>visibleTextPattern: - if no IGNORE_TOKEN -> exact match required (case-sensitive) - if
   * IGNORE_TOKEN present -> wildcard match (prefix/suffix must match; middle is dynamic)
   *
   * @param visibleTextPattern may contain IGNORE_TOKEN
   */
  private record HtmlSignature(
      String tag,
      Map<String, String> attrs,
      List<String> stableClassTokens,
      String visibleTextPattern) {

    static HtmlSignature parse(final String html) {
      final String h = html == null ? "" : html.trim();
      if (h.isEmpty()) throw new IllegalArgumentException("elementHtml is empty");

      final String tag = extractTagName(h);
      final Map<String, String> allAttrs = extractAttributesFromStartTag(h);

      final List<String> stableTokens =
          extractStableClassTokens(allAttrs.getOrDefault("class", ""));
      final String textPattern = extractVisibleText(h); // may include IGNORE_TOKEN

      final Map<String, String> useful = new LinkedHashMap<>();

      // Common
      copyIfPresent(allAttrs, useful, "id");
      copyIfPresent(allAttrs, useful, "name");
      copyIfPresent(allAttrs, useful, "type");
      copyIfPresent(allAttrs, useful, "role");
      copyIfPresent(allAttrs, useful, "title");

      // Accessibility/testing
      copyIfPresent(allAttrs, useful, "aria-label");
      copyIfPresent(allAttrs, useful, "data-testid");

      // Links/images/inputs
      copyIfPresent(allAttrs, useful, "href");
      copyIfPresent(allAttrs, useful, "src");
      copyIfPresent(allAttrs, useful, "alt");
      copyIfPresent(allAttrs, useful, "value");
      copyIfPresent(allAttrs, useful, "placeholder");

      // Include all data-* attributes
      for (final Map.Entry<String, String> e : allAttrs.entrySet()) {
        final String k = e.getKey();
        if (k != null && k.startsWith("data-")) {
          copyIfPresent(allAttrs, useful, k);
        }
      }

      return new HtmlSignature(tag, useful, stableTokens, textPattern);
    }

    private static void copyIfPresent(
        final Map<String, String> src, final Map<String, String> dst, final String key) {
      final String v = src.get(key);
      if (v != null && !v.isBlank()) dst.put(key, v.trim());
    }

    private static String extractTagName(final String html) {
      final int lt = html.indexOf('<');
      final int space = html.indexOf(' ', lt + 1);
      final int gt = html.indexOf('>', lt + 1);
      final int end = (space == -1) ? gt : Math.min(space, gt);
      if (lt == -1 || end == -1)
        throw new IllegalArgumentException("Invalid HTML snippet (no tag found)");
      return html.substring(lt + 1, end).replace("/", "").trim().toLowerCase(Locale.ROOT);
    }

    private static Map<String, String> extractAttributesFromStartTag(final String html) {
      final int lt = html.indexOf('<');
      final int gt = html.indexOf('>', lt + 1);
      if (lt == -1 || gt == -1) return Map.of();

      final String startTag = html.substring(lt + 1, gt);
      final String[] parts = startTag.trim().split("\\s+", 2);
      final String attrPart = parts.length > 1 ? parts[1] : "";

      final Map<String, String> attrs = new LinkedHashMap<>();
      final Matcher m = ATTR_PATTERN.matcher(attrPart);

      while (m.find()) {
        final String key = m.group(1).toLowerCase(Locale.ROOT);
        final String val = m.group(3);
        attrs.put(key, val);
      }
      return attrs;
    }

    private static List<String> extractStableClassTokens(final String classAttr) {
      if (classAttr == null) return List.of();
      final String[] tokens = classAttr.trim().split("\\s+");
      final List<String> out = new ArrayList<>();
      for (final String t : tokens) {
        if (t.isBlank()) continue;
        if (t.startsWith("css-")) continue;
        out.add(t);
      }
      return out;
    }

    private static String extractVisibleText(final String html) {
      final String text = html.replaceAll("<[^>]+>", " ");
      return text.replaceAll("\\s+", " ").trim();
    }
  }

  private record SelectorValidationResult(String selector, boolean isValid, int matchCount) {
    private SelectorValidationResult(
        final String selector, final boolean isValid, final int matchCount) {
      this.selector = selector == null ? "" : selector;
      this.isValid = isValid;
      this.matchCount = matchCount;
    }
  }
}
