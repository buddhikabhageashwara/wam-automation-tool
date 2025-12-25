
# Test Case Step Log Management Design

## Overview

In `TestCaseStepExecuteResponseDto`, a new field should be introduced to return the log file content.  
This field should be of type `String`, where the log file is converted into **Base64 format** and returned for **report generation**.

---

## Supported Test Case Step (TCS) Types for Log Handling

The following TCS types should be introduced for log reading and management:

### 1. Start Logging
- **Inputs:**
  - Log file path
  - File name

- **File Name Format:**
  ```
  <execution_id>_<tcs_id>_<execution_order_number>_<given_file_name>
  ```

### 2. End Logging

### 3. Log Reading

### 4. Retrieve Log

### 5. Delete Log
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

When performing **log reading**, the following execution sequence must be followed:

1. Trigger **Start Logging**
2. Trigger **End Logging**
3. Trigger **Log Reading**

---

## Log Writing Mechanism

- When the **Start Logging** TCS is triggered:
  - Log lines are written to a **temporary location** using the **Apache Tailer library**.
- Log writing continues until the **End Logging** TCS is triggered.
- Once **End Logging** is executed:
  - Log writing stops, and the log file is finalized.

---

## Log Retrieval and Cleanup

- After logging is completed, the user may:
  1. Trigger **Retrieve Log**
  2. Optionally trigger **Delete Log**

- If the **Delete Log** step is not triggered:
  - The log file will **remain permanently** in the specified location.

---

## Retention Responsibility (Optional)

Log file cleanup is the responsibility of the test case author.  
If the delete step is omitted, log files will not be automatically removed.
