
# Test Case Step Log Management Design

## Overview

In `TestCaseStepExecuteResponseDto`, a new field should be introduced to return the log file content.  
This field should be of type `String`, where the log file is converted into **Base64 format** and returned for **report generation**.

---

## Supported Test Case Step (TCS) Types for Log Handling

The following **exact TCS types** should be introduced for log reading and management:

### 1. A_LOG_FILE_READING_START
- **Purpose:** Start log capturing for a test case step.
- **Inputs:**
    - Log file path
    - File name

- **File Name Format:**
  ```
  <execution_id>_<tcs_id>_<execution_order_number>_<given_file_name>
  ```

- When this TCS is triggered, log lines should start writing to a temporary location using the **Apache Tailer library**.

---

### 2. A_LOG_FILE_READING_END
- **Purpose:** Stop log capturing.
- When this TCS is triggered:
    - Log writing is stopped.
    - The log file is finalized and ready for reading or extraction.

---

### 3. A_LOG_FILE_READ
- **Purpose:** Read the finalized log file.
- This step should read the log content from the temporary location and prepare it for further processing.

---

### 4. A_LOG_FILE_EXTRACT
- **Purpose:** Retrieve the log content.
- The log file should be:
    - Read from disk
    - Converted into **Base64 format**
    - Returned as a `String` in `TestCaseStepExecuteResponseDto` for **report generation**

---

### 5. A_LOG_FILE_DELETE
- **Purpose:** Delete the generated log file.
- A flag should be introduced to control deletion behavior:
    - If the flag is **true**:
        - The step returns **success**, regardless of whether the log file exists.
    - If the flag is **false**:
        - The step **fails** if the log file does not exist when deletion is requested.

---

## Alias-Based Connection Between Log Steps

To maintain a connection between the above five TCS types, the **same alias** must be passed as a preference parameter.

This alias uniquely links all log-related steps within a single execution flow.

---

## Log Execution Flow

When performing log reading and extraction, the following execution sequence **must** be followed:

1. Trigger **A_LOG_FILE_READING_START**
2. Trigger **A_LOG_FILE_READING_END**
3. Trigger **A_LOG_FILE_READ**
4. Trigger **A_LOG_FILE_EXTRACT**
5. Optionally trigger **A_LOG_FILE_DELETE**

---

## Log Writing Mechanism

- Log writing starts when **A_LOG_FILE_READING_START** is triggered.
- Log lines are written to a **temporary location** using the **Apache Tailer library**.
- Log writing continues until **A_LOG_FILE_READING_END** is triggered.
- After the end step:
    - The log file is finalized.
    - No further log entries are written.

---

## Log Retention and Cleanup

- If **A_LOG_FILE_DELETE** is not triggered:
    - The log file will **remain permanently** in the specified location.
- Log file cleanup is the responsibility of the **test case author**.

---

## Notes

- Log extraction (`A_LOG_FILE_EXTRACT`) should only be executed **after** log reading has completed.
- The alias parameter must remain consistent across all log-related TCS types to ensure proper linkage.
