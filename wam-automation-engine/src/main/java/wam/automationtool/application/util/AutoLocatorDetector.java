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
          + "    const tag = el.nodeName.toLowerCase();"
          + "    const isSvg = el.namespaceURI && el.namespaceURI.indexOf('svg') !== -1;"
          + "    const step = isSvg"
          + "      ? '*[local-name()=\"' + tag + '\"][' + ix + ']'"
          + "      : tag + '[' + ix + ']';"
          + "    parts.unshift(step);"
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

    final String normalizedHtml = normalizeCopiedHtml(elementHtml);
    final String htmlTrim = normalizedHtml.trim();

    log.info(
        "START | indexToPick={} | elementHtmlLength={} | elementHtmlPreview={}",
        indexToPick,
        htmlTrim.length(),
        htmlTrim.length() <= 180 ? htmlTrim : htmlTrim.substring(0, 180) + "...");

    final long start = System.currentTimeMillis();
    waitForDomReady(driver);

    final JavascriptExecutor js = (JavascriptExecutor) driver;

    final HtmlSignature sig = HtmlSignature.parse(normalizedHtml);

    // ==========================================================
    // 🔐 HARD LOCK: ID MUST WIN — NO FALLBACK ALLOWED
    // ==========================================================
    final String sigId =
        sig.attrs == null ? "" : String.valueOf(sig.attrs.getOrDefault("id", "")).trim();

    log.info(
        "SIGNATURE | tag={} | extractedId='{}' | attrsKeys={}",
        sig.tag,
        sigId,
        sig.attrs == null ? "null" : sig.attrs.keySet());

    if (!sigId.isBlank()) {
      try {
        // --- Debug: DOM count in current document ---
        try {
          final Object domCount =
              js.executeScript(
                  "return document.querySelectorAll('#' + CSS.escape(arguments[0])).length;",
                  sigId);
          log.info("DEBUG | domQueryCountById | id={} | count={}", sigId, domCount);
        } catch (Exception e) {
          log.warn("DEBUG | domQueryCountById failed | id={}", sigId, e);
        }

        // --- Debug: iframe count in current document ---
        try {
          final Object iframeCount =
              js.executeScript("return document.querySelectorAll('iframe,frame').length;");
          log.info("DEBUG | iframeCountInDoc | count={}", iframeCount);
        } catch (Exception e) {
          log.warn("DEBUG | iframeCountInDoc failed", e);
        }

        // --- Debug: Selenium id count in current context ---
        try {
          final int seleniumCount = driver.findElements(By.id(sigId)).size();
          log.info("DEBUG | seleniumFindElementsById | id={} | count={}", sigId, seleniumCount);
        } catch (Exception e) {
          log.warn("DEBUG | seleniumFindElementsById failed | id={}", sigId, e);
        }

        // ✅ Frame-aware ID search
        final WebElement elById = findByIdAcrossFrames(driver, sigId);

        if (elById == null) {
          log.warn("ID_PRESENT_BUT_NOT_FOUND_IN_ANY_FRAME | id={}", sigId);
        } else {
          log.info("HARD_MATCH | by=id(frame-scan) | id={}", sigId);

          // NOTE: after findByIdAcrossFrames, driver is already in the correct frame
          final Map<String, String> attrs = getAllAttributes(js, elById);
          final String tag = safeTag(elById);

          final SelectorValidationResult healed =
              buildPreferredXPathValidated(driver, js, elById, tag, attrs);

          logPickedElementDebug(js, elById, healed.selector);

          log.info(
              "END | resolvedBy=id | healedMatchCount={} | tookMs={}",
              healed.matchCount,
              System.currentTimeMillis() - start);

          // ✅ IMPORTANT: restore frame context for caller
          final String resultXpath = healed.selector;
          driver.switchTo().defaultContent();
          return resultXpath;
        }

      } catch (final Exception e) {
        // Always restore, even if something went wrong while switching frames
        try {
          driver.switchTo().defaultContent();
        } catch (Exception ignored) {
          // ignore
        }
        log.warn("ID_MATCH_FLOW_FAILED | id={}", sigId, e);
      }
    }

    // ==========================================================
    // 2️⃣ SECONDARY MATCHING (only if NO ID or ID not found)
    // ==========================================================
    List<WebElement> matches = findElementsByHtmlSignatureWithFallback(driver, sig);

    if (matches.isEmpty()) {
      // restore before throwing
      try {
        driver.switchTo().defaultContent();
      } catch (Exception ignored) {
        // ignore
      }
      throw new NoSuchElementException(
          "No element matched HTML signature. tag=" + sig.tag + ", attrs=" + sig.attrs);
    }

    if (indexToPick < 0 || indexToPick >= matches.size()) {
      // restore before throwing
      try {
        driver.switchTo().defaultContent();
      } catch (Exception ignored) {
        // ignore
      }
      throw new NoSuchElementException(
          "indexToPick=" + indexToPick + " out of range. Matches=" + matches.size());
    }

    final WebElement picked = matches.get(indexToPick);
    final Map<String, String> attrs = getAllAttributes(js, picked);
    final String tag = safeTag(picked);

    final SelectorValidationResult healed =
        buildPreferredXPathValidated(driver, js, picked, tag, attrs);

    logPickedElementDebug(js, picked, healed.selector);

    log.info(
        "END | matches={} | pickedIndex={} | healedMatchCount={} | tookMs={}",
        matches.size(),
        indexToPick,
        healed.matchCount,
        System.currentTimeMillis() - start);

    // ✅ IMPORTANT: restore frame context for caller
    final String resultXpath = healed.selector;
    driver.switchTo().defaultContent();
    return resultXpath;
  }

  private static WebElement findByIdAcrossFrames(final WebDriver driver, final String id) {
    Objects.requireNonNull(driver, "driver");
    Objects.requireNonNull(id, "id");

    driver.switchTo().defaultContent();

    // Try top document
    final List<WebElement> top = driver.findElements(By.id(id));
    if (!top.isEmpty()) return top.get(0);

    // Deep search frames (recursive)
    return findByIdInFramesRecursive(driver, id, 0, 5);
  }

  private static WebElement findByIdInFramesRecursive(
      final WebDriver driver, final String id, final int depth, final int maxDepth) {

    if (depth > maxDepth) return null;

    final List<WebElement> frames = driver.findElements(By.cssSelector("iframe,frame"));
    for (int i = 0; i < frames.size(); i++) {
      try {
        driver.switchTo().frame(frames.get(i));

        final List<WebElement> hit = driver.findElements(By.id(id));
        if (!hit.isEmpty()) {
          return hit.get(0); // ✅ driver is now in the correct frame
        }

        final WebElement nested = findByIdInFramesRecursive(driver, id, depth + 1, maxDepth);
        if (nested != null) return nested;

      } catch (final Exception ignored) {
        // ignore and continue
      } finally {
        // go back to parent for next sibling frame
        try {
          driver.switchTo().parentFrame();
        } catch (final Exception ignored2) {
          driver.switchTo().defaultContent();
        }
      }
    }
    return null;
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

    final Object res =
        js.executeScript(
            "var tag = arguments[0];"
                + "var attrs = arguments[1] || {};"
                + "var classTokens = arguments[2] || [];"
                + "var pattern = arguments[3] || '';"
                + "var IGNORE = arguments[4] || '';"
                + "var pathD = arguments[5] || '';"
                + "function splitWs(s){ return (s || '').split(/\\s+/).filter(Boolean); }"
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
                + "function isVisible(el){"
                + "  if (!el) return false;"
                + "  var r = el.getClientRects();"
                + "  return r && r.length > 0;"
                + "}"

                // --- SVG path 'd' matching helpers ---
                + "function normD(d){ return (d || '').replace(/\\s+/g,' ').trim(); }"
                + "function extractFirstPathD(svg){"
                + "  try {"
                + "    var p = svg.querySelector('path');"
                + "    if (!p) return '';"
                + "    return normD(p.getAttribute('d') || '');"
                + "  } catch(e) { return ''; }"
                + "}"
                + "function matchPathD(svg, expectedD){"
                + "  if (!expectedD || expectedD.length === 0) return true;"
                + "  var actualD = extractFirstPathD(svg);"
                + "  return actualD.length > 0 && actualD === normD(expectedD);"
                + "}"
                + "var nodeList = document.querySelectorAll(tag);"
                + "var els = [];"
                + "for (var i = 0; i < nodeList.length; i++) els.push(nodeList[i]);"
                + "var out = [];"
                + "for (var e = 0; e < els.length; e++){"
                + "  var el = els[e];"

                // attrs match
                + "  for (var k in attrs){"
                + "    if (!Object.prototype.hasOwnProperty.call(attrs, k)) continue;"
                + "    var expected = attrs[k];"
                + "    var actual = el.getAttribute(k) || '';"
                + "    if (actual !== expected){ el = null; break; }"
                + "  }"
                + "  if (!el) continue;"

                // class tokens
                + "  if (!hasAllClasses(el, classTokens)) continue;"

                // ✅ SVG path 'd' match (strong discriminator)
                + "  var isSvg = (String(tag).toLowerCase() === 'svg');"
                + "  if (isSvg && pathD && pathD.length > 0) {"
                + "    if (!matchPathD(el, pathD)) continue;"
                + "  }"

                // text
                + "  var text = visibleText(el);"
                + "  if (!matchText(text, pattern)) continue;"

                // visibility
                + "  if (!isVisible(el)) continue;"
                + "  out.push(el);"
                + "}"
                + "return out;",
            sig.tag,
            sig.attrs,
            sig.stableClassTokens,
            sig.visibleTextPattern,
            IGNORE_TOKEN,
            sig.svgPathD == null ? "" : sig.svgPathD);

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

    // ------------------------------------------------------------------
    // 1) Strong attributes FIRST (ID must always win)
    // ------------------------------------------------------------------
    addIfNotBlank(candidates, byAttrXPath(tag, "id", attrs.get("id")));
    addIfNotBlank(candidates, byAttrXPath(tag, "data-testid", attrs.get("data-testid")));
    addIfNotBlank(candidates, byAttrXPath(tag, "name", attrs.get("name")));
    addIfNotBlank(candidates, byAttrXPath(tag, "aria-label", attrs.get("aria-label")));

    // ------------------------------------------------------------------
    // 2) Stable class tokens (ignore css-* noise)
    // ------------------------------------------------------------------
    final String classAttr = attrs.getOrDefault("class", "");
    final List<String> stableTokens = new ArrayList<>();

    if (classAttr != null && !classAttr.isBlank()) {
      for (final String c : classAttr.trim().split("\\s+")) {
        if (c.isBlank()) continue;
        if (c.startsWith("css-")) continue;
        stableTokens.add(c);
      }
    }

    if (!stableTokens.isEmpty()) {
      final StringBuilder cond = new StringBuilder();
      for (int i = 0; i < stableTokens.size(); i++) {
        if (i > 0) cond.append(" and ");
        final String token = stableTokens.get(i);
        cond.append(
            "contains(concat(' ', normalize-space(@class), ' '), "
                + xpathLiteral(" " + token + " ")
                + ")");
      }

      if (isSvgTag(tag)) {
        candidates.add("//*[(local-name()=" + xpathLiteral(tag) + ") and " + cond + "]");
      } else {
        candidates.add("//" + tag + "[" + cond + "]");
      }
    }

    // ------------------------------------------------------------------
    // 3) Visible text token (only if meaningful)
    // ------------------------------------------------------------------
    final String textToken = pickStableTextToken(safeText(el));
    if (!textToken.isEmpty()) {
      if (isSvgTag(tag)) {
        candidates.add(
            "//*[(local-name()="
                + xpathLiteral(tag)
                + ") and .//*[contains(normalize-space(), "
                + xpathLiteral(textToken)
                + ")]]");
      } else {
        candidates.add(
            "//" + tag + "[.//*[contains(normalize-space(), " + xpathLiteral(textToken) + ")]]");
      }
    }

    // ------------------------------------------------------------------
    // 4) Absolute XPath (LAST RESORT ONLY)
    // ------------------------------------------------------------------
    final String abs = safeJsString(js, ABSOLUTE_XPATH_JS, el);
    addIfNotBlank(candidates, abs);

    // ------------------------------------------------------------------
    // 5) Validate candidates
    //    - Prefer UNIQUE valid match
    //    - Otherwise pick BEST valid (lowest matchCount)
    //    - NEVER return unrelated XPath
    // ------------------------------------------------------------------
    SelectorValidationResult bestValid = null;

    for (final String xp : candidates) {
      final SelectorValidationResult vr = validateXpath(driver, js, el, xp);

      if (vr.isValid && vr.matchCount == 1) {
        return vr; // 🎯 perfect match
      }

      if (vr.isValid) {
        if (bestValid == null || vr.matchCount < bestValid.matchCount) {
          bestValid = vr;
        }
      }
    }

    // ------------------------------------------------------------------
    // 6) Safe fallback
    // ------------------------------------------------------------------
    if (bestValid != null) {
      return bestValid;
    }

    // Absolutely nothing usable
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

    final String t = tag == null ? "" : tag.toLowerCase(Locale.ROOT).trim();
    if (t.isEmpty()) return "";

    // SVG-safe: use local-name()
    if (isSvgTag(t)) {
      return "//*[(local-name()="
          + xpathLiteral(t)
          + ") and @"
          + attr
          + "="
          + xpathLiteral(value)
          + "]";
    }

    return "//" + t + "[@" + attr + "=" + xpathLiteral(value) + "]";
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

  private static String normalizeCopiedHtml(final String html) {
    if (html == null) return "";
    String h = html.trim();

    // remove very noisy attributes from Chrome "Copy element"
    h = h.replaceAll("\\s+style\\s*=\\s*(['\"]).*?\\1", "");
    h = h.replaceAll("\\s+srcset\\s*=\\s*(['\"]).*?\\1", "");
    h = h.replaceAll("\\s+decoding\\s*=\\s*(['\"]).*?\\1", "");
    h = h.replaceAll("\\s+data-nimg\\s*=\\s*(['\"]).*?\\1", "");
    h = h.replaceAll("\\s+data-[a-zA-Z0-9_-]+\\s*=\\s*(['\"]).*?\\1", "");

    // normalize whitespace
    h = h.replaceAll("\\s+", " ").trim();
    return h;
  }

  private static List<WebElement> findElementsByHtmlSignatureWithFallback(
      final WebDriver driver, final HtmlSignature sig) {

    // 1) strict
    List<WebElement> res = findElementsByHtmlSignature(driver, sig);
    if (!res.isEmpty()) return res;

    // ✅ keep svgPathD in all fallbacks (if available)
    final String svgPathD = sig.svgPathD == null ? "" : sig.svgPathD;

    // 2) relax attrs (keep class tokens + text)
    res =
        findElementsByHtmlSignature(
            driver,
            new HtmlSignature(
                sig.tag, Map.of(), sig.stableClassTokens, sig.visibleTextPattern, svgPathD));
    if (!res.isEmpty()) return res;

    // 3) relax text (keep class tokens; sometimes innerText differs)
    res =
        findElementsByHtmlSignature(
            driver, new HtmlSignature(sig.tag, sig.attrs, sig.stableClassTokens, "", svgPathD));
    if (!res.isEmpty()) return res;

    // 4) relax attrs + text (still keep class tokens)
    res =
        findElementsByHtmlSignature(
            driver, new HtmlSignature(sig.tag, Map.of(), sig.stableClassTokens, "", svgPathD));
    if (!res.isEmpty()) return res;

    // 5) last resort: text-only (only if class tokens are empty)
    if (sig.stableClassTokens == null || sig.stableClassTokens.isEmpty()) {
      res =
          findElementsByHtmlSignature(
              driver,
              new HtmlSignature(sig.tag, Map.of(), List.of(), sig.visibleTextPattern, svgPathD));
      if (!res.isEmpty()) return res;
    }

    return Collections.emptyList();
  }

  private static boolean isSvgTag(final String tag) {
    if (tag == null) return false;
    return switch (tag.toLowerCase(Locale.ROOT)) {
      case "svg",
          "path",
          "g",
          "circle",
          "rect",
          "line",
          "polygon",
          "polyline",
          "ellipse",
          "defs",
          "use",
          "symbol",
          "clippath",
          "mask",
          "lineargradient",
          "radialgradient",
          "stop",
          "text",
          "tspan" ->
          true;
      default -> false;
    };
  }

  private static void logPickedElementDebug(
      final JavascriptExecutor js, final WebElement picked, final String extractedXPath) {

    try {
      final Object res =
          js.executeScript(
              "const el = arguments[0];"
                  + "const xp = arguments[1] || '';"
                  + "function safe(v){ return v ? String(v) : ''; }"
                  + "const tag = safe(el.tagName).toLowerCase();"
                  + "const id = safe(el.getAttribute('id'));"
                  + "const name = safe(el.getAttribute('name'));"
                  + "const aria = safe(el.getAttribute('aria-label'));"
                  + "const role = safe(el.getAttribute('role'));"
                  + "const cls = safe(el.getAttribute('class'));"
                  + "const txt = safe(el.innerText || el.textContent).replace(/\\s+/g,' ').trim().slice(0,120);"
                  + "const outer = safe(el.outerHTML).replace(/\\s+/g,' ').trim().slice(0,260);"
                  + "const color = window.getComputedStyle(el).getPropertyValue('color');"
                  + "const fill = window.getComputedStyle(el).getPropertyValue('fill');"
                  + "let pathFill = '';"
                  + "try {"
                  + "  const p = el.querySelector('path');"
                  + "  if (p) pathFill = window.getComputedStyle(p).getPropertyValue('fill');"
                  + "} catch(e) {}"
                  + "return { tag, id, name, aria, role, cls, txt, outer, color, fill, pathFill, xp };",
              picked,
              extractedXPath);

      if (res instanceof java.util.Map<?, ?> m) {
        log.info(
            "PICKED_ELEMENT | xp={} | tag={} | id={} | name={} | ariaLabel={} | role={} | class={} | textPreview={} | cssColor={} | cssFill={} | pathFill={} | outerPreview={}",
            m.get("xp"),
            m.get("tag"),
            m.get("id"),
            m.get("name"),
            m.get("aria"),
            m.get("role"),
            m.get("cls"),
            m.get("txt"),
            m.get("color"),
            m.get("fill"),
            m.get("pathFill"),
            m.get("outer"));
        return;
      }

      log.info(
          "PICKED_ELEMENT | xp={} | tag={} | class={}",
          extractedXPath,
          safeTag(picked),
          safeAttr(picked, "class"));

    } catch (final Exception ex) {
      log.warn("PICKED_ELEMENT debug log failed | xp={}", extractedXPath, ex);
    }
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
      String visibleTextPattern,
      String svgPathD) {

    static HtmlSignature parse(final String html) {
      final String h = html == null ? "" : html.trim();
      if (h.isEmpty()) throw new IllegalArgumentException("elementHtml is empty");

      final String tag = extractTagName(h);
      final Map<String, String> allAttrs = extractAttributesFromStartTag(h);

      final List<String> stableTokens =
          extractStableClassTokens(allAttrs.getOrDefault("class", ""));
      final String textPattern = extractVisibleText(h);

      final Map<String, String> useful = new LinkedHashMap<>();

      copyIfPresent(allAttrs, useful, "id");
      copyIfPresent(allAttrs, useful, "name");
      copyIfPresent(allAttrs, useful, "type");
      copyIfPresent(allAttrs, useful, "role");
      copyIfPresent(allAttrs, useful, "title");

      copyIfPresent(allAttrs, useful, "aria-label");
      copyIfPresent(allAttrs, useful, "data-testid");

      copyIfPresent(allAttrs, useful, "href");
      copyIfPresent(allAttrs, useful, "src");
      copyIfPresent(allAttrs, useful, "alt");
      copyIfPresent(allAttrs, useful, "value");
      copyIfPresent(allAttrs, useful, "placeholder");

      copyIfPresent(allAttrs, useful, "aria-hidden");
      copyIfPresent(allAttrs, useful, "focusable");
      copyIfPresent(allAttrs, useful, "viewbox");
      copyIfPresent(allAttrs, useful, "width");
      copyIfPresent(allAttrs, useful, "height");
      copyIfPresent(allAttrs, useful, "fill");

      for (final Map.Entry<String, String> e : allAttrs.entrySet()) {
        final String k = e.getKey();
        if (k != null && k.startsWith("data-")) {
          copyIfPresent(allAttrs, useful, k);
        }
      }

      // ✅ Extract SVG path d (first path in snippet)
      String pathD = "";
      if ("svg".equalsIgnoreCase(tag)) {
        pathD = extractFirstPathD(h);
      }

      return new HtmlSignature(tag, useful, stableTokens, textPattern, pathD);
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
      if (lt == -1 || end == -1) {
        throw new IllegalArgumentException("Invalid HTML snippet (no tag found)");
      }
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

    private static String extractFirstPathD(final String html) {
      try {
        final Pattern p =
            Pattern.compile("<path[^>]*\\sd\\s*=\\s*([\"'])(.*?)\\1", Pattern.CASE_INSENSITIVE);
        final Matcher m = p.matcher(html);
        if (m.find()) {
          return m.group(2) == null ? "" : m.group(2).replaceAll("\\s+", " ").trim();
        }
        return "";
      } catch (final Exception e) {
        return "";
      }
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

    // ============================================================
    // Public API - Get element HTML by XPath (safe)
    // ============================================================

    public static final String ELEMENT_NOT_FOUND = "ELEMENT_NOT_FOUND";

    /**
     * Returns the actual outerHTML of the first element matched by the given XPath.
     *
     * <p>Behavior:
     * <ul>
     *   <li>If found: returns {@code "xpath: <xp>, html: <outerHtml>"}.</li>
     *   <li>If not found OR any error occurs: returns {@link #ELEMENT_NOT_FOUND}.</li>
     * </ul>
     *
     * <p>This method is frame-aware and restores {@code defaultContent()} before returning.
     */
    public static String getOuterHtmlByXPath(final WebDriver driver, final String xpath) {
        Objects.requireNonNull(driver, "driver");
        Objects.requireNonNull(xpath, "xpath");

        final String xp = xpath.trim();
        if (xp.isEmpty()) return ELEMENT_NOT_FOUND;

        log.info("START | xpath={}", xp);

        final long start = System.currentTimeMillis();

        try {
            waitForDomReady(driver);

            // Always start from top document
            driver.switchTo().defaultContent();

            final WebElement found = findByXPathAcrossFrames(driver, xp);
            if (found == null) {
                log.warn("NOT_FOUND | xpath={}", xp);
                return ELEMENT_NOT_FOUND;
            }

            final JavascriptExecutor js = (JavascriptExecutor) driver;
            final String html = safeOuterHtml(js, found);

            final String result = "xpath: " + xp + ", html: " + (html == null ? "" : html);

            log.info(
                    "END | xpath={} | htmlLength={} | tookMs={}",
                    xp,
                    html == null ? 0 : html.length(),
                    System.currentTimeMillis() - start);

            return result;

        } catch (final Exception e) {
            log.error("ERROR | xpath={}", xp, e);
            return ELEMENT_NOT_FOUND;

        } finally {
            try {
                driver.switchTo().defaultContent();
            } catch (final Exception ignored) {
                // ignore
            }
        }
    }

    private static WebElement findByXPathAcrossFrames(final WebDriver driver, final String xpath) {
        Objects.requireNonNull(driver, "driver");
        Objects.requireNonNull(xpath, "xpath");

        // Try top document
        try {
            final List<WebElement> top = driver.findElements(By.xpath(xpath));
            if (!top.isEmpty()) return top.get(0);
        } catch (final Exception ignored) {
            // ignore
        }

        // Deep search frames (recursive)
        return findByXPathInFramesRecursive(driver, xpath, 0, 5);
    }

    private static WebElement findByXPathInFramesRecursive(
            final WebDriver driver, final String xpath, final int depth, final int maxDepth) {

        if (depth > maxDepth) return null;

        final List<WebElement> frames = driver.findElements(By.cssSelector("iframe,frame"));
        for (int i = 0; i < frames.size(); i++) {
            try {
                driver.switchTo().frame(frames.get(i));

                try {
                    final List<WebElement> hit = driver.findElements(By.xpath(xpath));
                    if (!hit.isEmpty()) {
                        return hit.get(0); // ✅ driver is now in the correct frame
                    }
                } catch (final Exception ignored) {
                    // ignore
                }

                final WebElement nested = findByXPathInFramesRecursive(driver, xpath, depth + 1, maxDepth);
                if (nested != null) return nested;

            } catch (final Exception ignored) {
                // ignore
            } finally {
                try {
                    driver.switchTo().parentFrame();
                } catch (final Exception ignored2) {
                    try {
                        driver.switchTo().defaultContent();
                    } catch (final Exception ignored3) {
                        // ignore
                    }
                }
            }
        }
        return null;
    }

}
