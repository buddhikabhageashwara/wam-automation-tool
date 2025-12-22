# Developer Note  
## Alias Concept and Usage in Test Case Steps

---

## Purpose of This Note

This document explains **how aliases work**, **why they exist**, and **how they are used during Test Case Step (TCS) execution**.

This note is intended to:
- Help another developer understand the full design and relationships when continuing development
- Allow easy recall of the implementation after a long break
- Serve as long-term technical documentation

---

## 1. High-Level Concept

An **Alias** is a **named, reusable configuration group** that can be referenced inside **Test Case Steps** using `preferenceParameters`.

Instead of hardcoding values (URLs, DB credentials, cache names, etc.) directly into each test case step:
- We define them once as an **Alias**
- Reference them by **alias name**
- Resolve them dynamically **during execution**

### Benefits
- Reusability
- Cleaner test case definitions
- Environment independence
- Easier maintenance

---

## 2. Core Table Groupings

### 2.1 Alias-Side Tables (Configuration Source)

These tables define **what an alias is** and **what values it contains**.

| Table | Responsibility |
|------|---------------|
| alias | Stores the alias name and alias type |
| alias_parameter | Stores actual values for each parameter |
| alias_parameter_type | Defines what parameters an alias can have |

**Relationship**
```
alias
 └── alias_parameter
      └── alias_parameter_type
```

---

### 2.2 Test Case Step-Side Tables (Configuration Consumer)

These tables define **what a test case step expects**.

| Table | Responsibility |
|------|---------------|
| test_case_step | Test step definition |
| preference_parameter | Values passed when creating a TCS |
| preference_parameter_type | Defines configurable parameters a TCS can accept |

**Relationship**
```
test_case_step
 └── preference_parameter
      └── preference_parameter_type
```

---

## 3. How Alias Resolution Works (Execution Flow)

### Runtime Flow

1. Test Case Step execution starts
2. Execution engine reads `preferenceParameters`
3. For each entry:
   - **Key** = `preference_parameter_type.id`
   - **Value** = alias name (e.g., `TEST_ALIAS_NAME`)
4. Execution engine searches `alias` table using `alias_name`
5. Once alias is found:
   - Fetch all records from `alias_parameter` using `alias.id`
6. Each `alias_parameter` contains:
   - `parameter_value`
   - Reference to `alias_parameter_type.parameter_name`
7. Retrieved values are used in execution logic

**Important:**  
Test case steps never store real values — only alias names.

---

## 4. Alias Type Classification

Aliases are grouped using `AliasType` to define what the alias represents.

```java
public enum AliasType {
    EXECUTION_SERVER("EXECUTION_SERVER"),
    CACHED_DATA("CACHED_DATA"),
    MYSQL_DB_CONNECTION("MYSQL_DB_CONNECTION"),
    MONGO_DB_CONNECTION("MONGO_DB_CONNECTION");
}
```

---

## 5. Example 1: Execution Server Alias

### Use Case
Execute a test case step on a remote execution agent.

---

### Step 1: Create Preference Parameter Type

Insert record into `preference_parameter_type` table:

| Column | Value |
|------|------|
| parameter_name | agentURL |

Assume:
```
preference_parameter_type.id = 4
```

---

### Step 2: Create Test Case Step

```json
{
  "testCaseStepName": "test case step close browser",
  "description": "test case step close browser description",
  "testCaseId": 4,
  "executionOrder": 7,
  "testCaseStepType": "W_CLOSE_BROWSER",
  "preferenceParameters": {
    "4": "TEST_ALIAS_NAME",
    "5": "webDriverCacheName5"
  }
}
```

---

### Step 3: Create Alias Record

Insert into `alias` table:

| Column | Value |
|------|------|
| alias_name | TEST_ALIAS_NAME |
| alias_type | EXECUTION_SERVER |

---

### Step 4: Create Alias Parameter Type

Insert into `alias_parameter_type` table:

| Column | Value |
|------|------|
| parameter_name | agentURL |

Assume:
```
alias_parameter_type.id = 8
```

---

### Step 5: Create Alias Parameter

Insert into `alias_parameter` table:

| Column | Value |
|------|------|
| alias_id | ID of TEST_ALIAS_NAME |
| alias_parameter_type_id | 8 |
| parameter_value | http://localhost:8087/v1/wam/automation/executions/testcasesteps/ |

---

## 6. Example 2: MYSQL_DB_CONNECTION Alias

### Use Case
Allow test cases to connect to different MySQL databases dynamically.

---

### Step 1: Alias Type

```java
AliasType.MYSQL_DB_CONNECTION
```

---

### Step 2: Alias Parameter Types

| parameter_name |
|---------------|
| host |
| username |
| password |
| port |
| database |

---

### Step 3: Create Alias

| Column | Value |
|------|------|
| alias_name | MYSQL_LOCAL_DB |
| alias_type | MYSQL_DB_CONNECTION |

---

### Step 4: Create Alias Parameters

| alias_parameter_type_id | parameter_value |
|------------------------|----------------|
| host | localhost |
| username | root |
| password | root123 |
| port | 3306 |
| database | wam_test_db |

---

## 7. Key Design Benefits

- Single source of truth
- Reusable configuration
- Environment independence
- Clean test case payloads
- Easy future extension

---

## 8. Quick Recall Summary

**PreferenceParameter defines _what is required_**  
**Alias defines _how it is provided_**
