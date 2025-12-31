# Log reading validation – QA parameter cookbook (with `invertResult`)

This guide is written for **QA** to validate logs by entering **only input values** (no code changes needed), using these fields:

- `logFilePath`
- `includeRegex`
- `excludeRegex`
- `regex`
- `groupIndex`
- `expectedValue`
- `invertResult` *(new)*

✅ Rule used by the system:  
- If **matched = true** → step becomes **PASS**  
- If **matched = false** → step becomes **FAIL**

---

## What each input means (QA-friendly)

### 1) logFilePath
The log file path to scan (example: the temp log file path you created).

### 2) includeRegex (optional)
Only lines that **match this pattern** are considered.

- Leave **empty / null** if you do not want filtering.

### 3) excludeRegex (optional)
Lines that **match this pattern** are ignored.

- Leave **empty / null** if you do not want to ignore anything.

### 4) regex (required)
The **main** pattern to search inside each allowed line.

### 5) groupIndex
What value to extract from the main `regex`.

- `0` → extract the **full matched text**
- `1` → extract the **1st captured part** in `( ... )`
- `2` → extract the **2nd captured part** in `( ... )`
- etc.

> If your `regex` does not contain `( ... )`, always use `groupIndex = 0`.

### 6) expectedValue
- If provided: PASS only if the extracted value equals `expectedValue`
- If left **null**: PASS if *any match exists* (no equality check)

### 7) invertResult  ✅ (new)
Controls how PASS/FAIL is decided after matching.

- `false` → normal behavior (match = PASS)
- `true` → inverted behavior (NO match = PASS)

This is useful for **“PASS when no error exists”**.

---

# Scenario 1: Exact match (no include/exclude)

### Goal
PASS only if an exact message appears in the log.

### Example log line
```
2025-12-28 ... DEBUG ... Agent URL preference parameter is null. test case step id: 16
```

### Inputs
- `includeRegex`: `null`
- `excludeRegex`: `null`
- `regex`: `Agent URL preference parameter is null\. test case step id: 16\b`
- `groupIndex`: `0`
- `expectedValue`: `Agent URL preference parameter is null. test case step id: 16`
- `invertResult`: `false`

### Why this works
- `groupIndex = 0` returns the full matched text, so it can equal the expected value.

---

# Scenario 2: Extract ONE part and compare with expected

### Goal
Find the line and extract only the step id (`16`), then compare with expected.

### Example log line
```
2025-12-28 ... DEBUG ... Agent URL preference parameter is null. test case step id: 16
```

### Inputs
- `includeRegex`: `Agent URL preference parameter is`
- `excludeRegex`: `IGNORE ME`
- `regex`: `test case step id:\s*(\d+)\b`
- `groupIndex`: `1`
- `expectedValue`: `16`
- `invertResult`: `false`

### Why this works
- `(\d+)` captures only digits, so `groupIndex = 1` returns `16`.

---

# Scenario 3: Use REGEX for include, exclude, and action regex

### Goal
Use proper regex patterns for filtering and extracting.

### Example log lines
```
2025-12-28 ... DEBUG ... Agent URL preference parameter is null. test case step id: 16
2025-12-28 ... DEBUG ... Agent URL preference parameter is null. test case step id: 16 IGNORE ME
```

### Inputs
- `includeRegex`: `^\d{4}-\d{2}-\d{2}.*\bDEBUG\b.*Agent URL preference parameter is`
- `excludeRegex`: `\bIGNORE ME\b`
- `regex`: `test case step id:\s*(\d+)\b`
- `groupIndex`: `1`
- `expectedValue`: `16`
- `invertResult`: `false`

### Result
- First line is considered (include matches).
- Second line is ignored (exclude matches).
- Step id is extracted and validated.

---

# Scenario 4: include = null, exclude = null (scan whole file)

### Goal
Search the entire log file without filtering.

### Example log line
```
... test case step id: 16
```

### Inputs
- `includeRegex`: `null`
- `excludeRegex`: `null`
- `regex`: `\btest case step id:\s*(\d+)\b`
- `groupIndex`: `1`
- `expectedValue`: `16`
- `invertResult`: `false`

---

# Scenario 5: include and exclude BOTH used (simple patterns)

### Goal
Only consider Agent URL lines, ignore IGNORE ME lines, validate the extracted id.

### Example log lines
```
... Agent URL preference parameter is null. test case step id: 16 IGNORE ME
... Agent URL preference parameter is null. test case step id: 16
```

### Inputs
- `includeRegex`: `Agent URL preference parameter is`
- `excludeRegex`: `IGNORE ME`
- `regex`: `test case step id:\s*(\d+)\b`
- `groupIndex`: `1`
- `expectedValue`: `16`
- `invertResult`: `false`

---

# Scenario 6: Group 2 example (two captured values)

### Goal
Extract the **second** captured value using `groupIndex = 2`.

### Example log line
```
... userId=ABC123, stepId=16, status=PASSED
```

### Inputs (extract userId)
- `includeRegex`: `userId=`
- `excludeRegex`: `null`
- `regex`: `userId=(\w+),\s*stepId=(\d+)`
- `groupIndex`: `1`
- `expectedValue`: `ABC123`
- `invertResult`: `false`

### Inputs (extract stepId) ✅ group 2
- `includeRegex`: `userId=`
- `excludeRegex`: `null`
- `regex`: `userId=(\w+),\s*stepId=(\d+)`
- `groupIndex`: `2`
- `expectedValue`: `16`
- `invertResult`: `false`

---

# Scenario 7: PASS if ANY match exists (no expectedValue)

### Goal
PASS if the line appears at least once (don’t compare extracted value).

### Example log line
```
... Agent URL preference parameter is null. test case step id: 16
```

### Inputs
- `includeRegex`: `Agent URL preference parameter is`
- `excludeRegex`: `null`
- `regex`: `test case step id:\s*(\d+)\b`
- `groupIndex`: `1`
- `expectedValue`: `null`
- `invertResult`: `false`

---

# Scenario 8: API validation – FAIL if an error is printed, PASS if no error printed ✅ (uses invertResult)

### Goal
After triggering an API, determine success/failure from logs:
- If **any error** printed → FAIL
- If **no error** printed → PASS

### Example error log lines
```
... ERROR ... Something failed ...
... Exception: NullPointerException ...
... StackTrace ...
```

### Inputs (PASS when NO error exists)
- `includeRegex`: `null`
- `excludeRegex`: `null`
- `regex`: `\b(ERROR|Exception|StackTrace)\b`
- `groupIndex`: `0`
- `expectedValue`: `null`
- `invertResult`: `true`

### How to read the result
- If the log contains ERROR/Exception/StackTrace → match found → inverted → **FAIL**
- If none exist → no match → inverted → **PASS**

---

## Quick escaping cheat-sheet (for QA)

If you type regex in a JSON/Java-like field, common escapes:

- Digits: `\d+`
- Word boundary: `\b`
- Space(s): `\s+`
- Literal dot: `\.`

---

## Common mistakes

1) **groupIndex out of range**
- Your `regex` does not have enough `( ... )` groups.
- Fix: add groups or reduce groupIndex (or use 0).

2) **Expected value mismatch**
- Matching works, but extracted value doesn’t equal expectedValue.
- Fix: check groupIndex or expectedValue text.

3) **Over-filtering**
- includeRegex too strict → no lines considered.
- Fix: loosen includeRegex or set it to null.

