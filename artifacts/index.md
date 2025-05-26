---
layout: default
title: Artifacts
nav_order: 2
---
<div style="text-align: center; white-space: nowrap; font-size: 16px; margin-bottom: 10px;">
  <a href="/snhu-capstone/index.md">Home</a> |
  <a href="/snhu-capstone/code-review/index.md">Code Review</a> |
  <a href="/snhu-capstone/enhancements/software-engineering/index.md">Software Engineering</a> |
  <a href="/snhu-capstone/enhancements/data-structures-algorithms/index.md">Algorithms</a> |
  <a href="/snhu-capstone/enhancements/databases/index.md">Databases</a> |
  <a href="/snhu-capstone/artifacts/index.md">Artifacts</a> |
  <a href="/snhu-capstone/self-assessment/index.md">Self-Assessment</a>
</div>
<hr>


# Artifacts

This page includes summaries of the three enhanced artifacts developed during the CS-499 Capstone Project. Each artifact represents growth across the major domains of computer science: Software Design and Engineering, Algorithms and Data Structures, and Databases. Code snippets are provided to illustrate meaningful enhancements made to the application, but are not comprehensive of the code to support these enhancements.

---

## Software Design and Engineering

The primary artifact is the `EventSync` Android application, originally created in CS-360. Enhancements made during the capstone focused on modular design, error handling, and session management.

### Utility Class Refactor (Example: `ErrorUtils.java`)
```java
public class ErrorUtils {
    public static void showAndLogError(Context context, String errorReason, String message, Exception e) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        Log.e(errorReason, message, e);
    }
}
```

### Centralized Session Handling
```java
public class User implements Serializable {
    private int userId;
    private String username;
    private String phoneNumber;
    private boolean viewingUpcomingEvents = true;
}
```

### Modular Lambda Usage
```java
savePhoneNumberButton.setOnClickListener(v -> savePhoneNumber(););
```

### User session persistence
```java
user = getIntent().getSerializableExtra(Constants.USER_INTENT_KEY, User.class);
```

---

## Algorithms and Data Structures

This artifact was enhanced to demonstrate improved algorithmic logic and data structure selection for recurring events and runtime efficiency.

### LRU Caching Example
```java
private LinkedHashMap<String, List<EventData>> cache = new LinkedHashMap<>(MAX_CACHE_SIZE, 0.75f, true) {
    protected boolean removeEldestEntry(Map.Entry<String, List<EventData>> eldest) {
        return size() > MAX_CACHE_SIZE;
    }
};
```

### Recurrence Calculation
```java
private LocalDate advanceDate(LocalDate start, RecurrenceType type, int steps, int interval) {
    switch (type) {
        case DAYS: return start.plusDays(steps * interval);
        case WEEKS: return start.plusWeeks(steps * interval);
        case MONTHS: return start.plusMonths(steps * interval);
        case YEARS: return start.plusYears(steps * interval);
        default: return start;
    }
}
```

### Enum Use for Recurrence Type
```java
public enum RecurrenceType {
    DAYS(1, "Day"),
    WEEKS(2, "Week"),
    MONTHS(3, "Month"),
    YEARS(4, "Year");
    
    private final int recurrenceTypeId;
    private final String dbValue;
    
    // Getter methods omitted for brevity
}
```

---

## Databases

Significant improvements were made to the SQLite schema to support referential integrity, recurring event structures, and secure credential storage.

### SQLite Table Creation with Constraints
```sql
CREATE TABLE events (
    event_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,
    event_name TEXT,
    event_date INTEGER,
    recurrence_type_key INTEGER,
    FOREIGN KEY(user_id) REFERENCES users(user_id),
    FOREIGN KEY(recurrence_type_key) REFERENCES recurrence_types(recurrence_type_key)
);
```

### Parameterized Queries to Prevent SQL Injection
```java
Cursor cursor = db.rawQuery("SELECT * FROM users WHERE username = ?", new String[]{username});
```

### Secure Password Storage
```java
KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 100000, 128);
SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
```

These code snippets reflect enhancements to application security, performance, and maintainability and collectively demonstrate my ability to work across the full stack of Android development.