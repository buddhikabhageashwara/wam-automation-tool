
# Execution Cache Removal Actions

## A_REMOVE_ALL_EXECUTION_CACHE

This action **should not be called on the Manager**, since the Manager-side cache is required to complete **report generation**.  
On the Manager, the execution cache is automatically cleared **after every execution**.

However, on **Agents**, this automatic cleanup does not occur.  
Therefore, this action can be used to manually clear **all execution caches** on Agents.

This is useful when:
- Running a **fresh execution**, or
- At the **end of a test plan**, to clear all Agent-side caches.

---

## A_REMOVE_EXECUTION_CACHE

This action **should not be called on the Manager**, as the Manager-side cache is required for **report generation**.  
On the Manager, execution caches are automatically cleared after each execution.

On **Agents**, this cleanup does not happen automatically.  
This action can be used to remove cache data for **a single, known execution**.

The required **execution ID** can be obtained from the **execution report**.

---

## A_REMOVE_CACHE_ITEM

This action can be executed on **either the Manager or an Agent**, since it targets only a **specific cache item** within an execution and does not affect the entire execution cache.
