package com.example.eventtracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import com.example.eventtracker.adapter.EventListAdapter;
import com.example.eventtracker.entity.User;
import com.example.eventtracker.utils.Constants;
import com.example.eventtracker.utils.DatabaseConstants;
import com.example.eventtracker.utils.ErrorUtils;
import com.example.eventtracker.utils.SecurityUtils;

public class DatabaseHelper extends SQLiteOpenHelper {
    //CS-499 - Software engineering:
    private Context context;

    //CS-499 - Databases:
    //CS-499 - Software engineering:
    public enum RecurrenceType {
        DAYS(1, "Day"),
        WEEKS(2, "Week"),
        MONTHS(3, "Month"),
        YEARS(4, "Year");
        private final String dbValue;
        private final int recurrenceTypeId;
        RecurrenceType(int recurrenceTypeId, String dbValue) {
            this.recurrenceTypeId = recurrenceTypeId;
            this.dbValue = dbValue;

        }
        public int getRecurrenceTypeId() {
            return recurrenceTypeId;
        }
        public String getDbValue() {
            return dbValue;
        }

        /**
        * Returns a recurrence type for its string representation
         */
        public static RecurrenceType fromDbValue(String value) {
            for (RecurrenceType type : RecurrenceType.values()) {
                if (type.dbValue.equalsIgnoreCase(value)) {
                    return type;
                }
            }
            return null;
        }


        /**
         * Validates a recurrence type
         */
        public static boolean isValidRecurrenceType(String value) {
            return fromDbValue(value) != null;
        }
    }

    // Constructor to initialize the SQLiteOpenHelper with database name and version
    public DatabaseHelper(Context context) {
        super(context, DatabaseConstants.DATABASE_NAME, null, DatabaseConstants.DATABASE_VERSION);
        this.context = context;
    }

    /**
     * Called when the database is first created, or on upgrade.
     * Creates the database schema
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //CS-499 - Databases
        createTables(db);
        createIndexes(db);
        populateRecurrenceTypes(db);
    }

    private void createTables(SQLiteDatabase db) {
        for (String query : DatabaseConstants.CREATE_TABLE_STATEMENTS) {
            db.execSQL(query);
        }
    }
    private void createIndexes(SQLiteDatabase db) {
        for (String query : DatabaseConstants.CREATE_IDX_STATEMENTS) {
            db.execSQL(query);
        }
    }

    //CS-499 - Databases
    private void populateRecurrenceTypes(SQLiteDatabase db) {
        for (RecurrenceType type : RecurrenceType.values()) {
            ContentValues values = new ContentValues();
            values.put(DatabaseConstants.COLUMN_RECURRENCE_TYPE_KEY, type.getRecurrenceTypeId());
            values.put(DatabaseConstants.COLUMN_RECURRENCE_TYPE_DESCRIPTION, type.getDbValue());
            db.insert(DatabaseConstants.TABLE_RECURRENCE_TYPE, null, values);
        }
    }

    /**
     * Handles database upgrades by dropping and recreating tables.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //CS-499 - Databases
        dropTables(db);
        onCreate(db);
    }
    /**
    * Drop any tables in the TABLES array, invoked on upgrade.
     * CS-499 - Databases
     */
    private void dropTables(SQLiteDatabase db) {
        for (String table : DatabaseConstants.TABLES) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
    }

    /**
     * Inserts a new user record into the database.
     * @return true if insert was successful, false otherwise.
     */
    public boolean addUser(String username, String passwordHash, String salt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues userValues = new ContentValues();
        userValues.put(DatabaseConstants.COLUMN_USER_NAME, username);
        userValues.put(DatabaseConstants.COLUMN_USER_PASSWORD, passwordHash);
        userValues.put(DatabaseConstants.COLUMN_USER_PASSWORD_SALT, salt);
        long result = db.insert(DatabaseConstants.TABLE_USERS, null,userValues);
        db.close();
        return result != -1;
    }

    /**
     * Checks if a user with the given username already exists.
     */
    public boolean checkUserExists(String username) {
        boolean userExists = false;
        SQLiteDatabase db = this.getReadableDatabase();
        //CS-499: Using parameterized query to avoid SQL injection
        try (Cursor cursor = db.rawQuery(DatabaseConstants.USER_LOOKUP_ID_USERNAME_BY_USERNAME, new String[]{username})) {
            userExists = cursor.getCount() > 0;
        } finally {
            db.close();
        }
        return userExists;
    }

    /**
     * Validates password strength based on character types and minimum length.
     */
    public boolean checkPasswordStrength(String password) {
        if (password == null || password.length() < DatabaseConstants.PASSWORD_LENGTH_REQUIREMENT) {
            return false;
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUppercase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecialChar = true;
            }
        }

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar;
    }

    /**
     * CS-499: Authenticate user and return their user ID if valid.
     * Uses a parameterized query to protect against SQL injection.
     *
     * @param username The user's login username.
     * @param password The user's login password.
     * @return user_id if credentials are valid, otherwise -1.
     */
    public int authenticateUser(String username, String password) {
        int userId = -1; // Default to -1 (unauthenticated)
        SQLiteDatabase db = this.getReadableDatabase();

        try {

            // CS-499: Use parameterized query to prevent SQL injection
            try (Cursor cursor = db.rawQuery(DatabaseConstants.USER_AUTH_QUERY, new String[]{username})) {
                if (cursor.moveToFirst()) {
                    String storedHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_USER_PASSWORD));
                    String storedSalt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_USER_PASSWORD_SALT));
                    String providedHash = SecurityUtils.hashPassword(password, storedSalt);

                    if (providedHash.equals(storedHash)) {
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_USER_ID));
                    }
                }
            }
        } catch (Exception e) {
            ErrorUtils.showAndLogError(context, "AuthFailure", "Authentication failed. Please try again later.", e);
        } finally {
            db.close();
        }

        return userId;
    }


    /**
     * Adds an event to the database.
     * //CS-499 - Databases
     */
    public long addEvent(User user, String eventName, LocalDate eventDate, LocalTime eventTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // CS-499: Add the user ID to associate the event with its owner
        values.put(DatabaseConstants.COLUMN_USER_ID, user.getUserId());
        values.put(DatabaseConstants.COLUMN_EVENT_NAME, eventName);

        // Combine date and time, convert to epoch seconds using local time zone offset
        LocalDateTime eventDateTime = LocalDateTime.of(eventDate, eventTime);
        long eventDateEpoch = eventDateTime.toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(eventDateTime));

        values.put(DatabaseConstants.COLUMN_EVENT_DATE, eventDateEpoch);

        long result = db.insert(DatabaseConstants.TABLE_EVENTS, null, values);
        db.close();
        return result;
    }

    /**
     * CS-499 - Databases: Adds recurring events to the database in bulk using a transaction.
     * This method avoids redundant inserts by leveraging recurrence rules to calculate
     * each event's date based on the selected recurrence type and interval.
     *
     * @param user The user who owns the events.
     * @param recurrenceType The recurrence pattern (e.g., DAYS, WEEKS, MONTHS, YEARS).
     * @param interval The number of units (days/weeks/etc.) between each recurrence.
     * @param occurrences The total number of events to create.
     * @param eventName The name or description of the recurring event.
     * @param eventStart The start date of the first occurrence.
     * @param eventTime The time of day the event occurs.
     * @return The number of successfully inserted event rows.
     */
    public long addRecurringEvent(User user, RecurrenceType recurrenceType, int interval, int occurrences, String eventName, LocalDate eventStart, LocalTime eventTime) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        int rowsInserted = 0;
        long parentKey = -1;
        if (occurrences == 0 || recurrenceType == null || interval == 0) {
            //Only add a single event if the required fields for recurrence are not set.
            addEvent(user, eventName, eventStart, eventTime);
        } else {
            try {
                for (int occurenceCount = 0; occurenceCount < occurrences; occurenceCount++) {
                    // Calculate the date for this occurrence
                    LocalDate eventDate = advanceDate(eventStart, recurrenceType, occurenceCount, interval);
                    LocalDateTime dateTime = LocalDateTime.of(eventDate, eventTime);
                    long epoch = dateTime.toEpochSecond(
                            ZoneOffset.systemDefault().getRules().getOffset(dateTime)
                    );

                    ContentValues values = new ContentValues();
                    values.put(DatabaseConstants.COLUMN_USER_ID, user.getUserId());
                    values.put(DatabaseConstants.COLUMN_EVENT_NAME, eventName);
                    values.put(DatabaseConstants.COLUMN_EVENT_DATE, epoch);
                    values.put(DatabaseConstants.COLUMN_RECURRENCE_TYPE_KEY, recurrenceType.getRecurrenceTypeId());
                    if (parentKey > -1) {
                        values.put(DatabaseConstants.COLUMN_EVENT_PARENT, parentKey);
                    } else {
                        values.put(DatabaseConstants.COLUMN_IS_EVENT_PARENT, 1);
                    }
                    if (occurenceCount == 0) {
                        parentKey = db.insert(DatabaseConstants.TABLE_EVENTS, null, values);
                        rowsInserted += (parentKey > 0 ? 1 : 0);
                    } else if (db.insert(DatabaseConstants.TABLE_EVENTS, null, values) >= 0) {
                        rowsInserted++;
                    }
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                ErrorUtils.showAndLogError(context, "EventError", "Error creating event. Please try again.", e);
            } finally {
                db.endTransaction();
            }
            db.close();
        }
        return rowsInserted;
    }

    /**
     * CS-499 - Databases: Adds recurring events to the database in bulk using a transaction.
     * This method avoids redundant inserts by leveraging recurrence rules to calculate
     * each event's date based on the selected recurrence type and interval.
     *
     * @param user The user who owns the events.
     * @param recurrenceType The recurrence pattern (e.g., DAYS, WEEKS, MONTHS, YEARS).
     * @param interval The number of units (days/weeks/etc.) between each recurrence.
     * @param eventName The name or description of the recurring event.
     * @param eventStart The start date of the first occurrence.
     * @param eventTime The time of day the event occurs.
     * @param endDate The date for this schedule to end.
     * @return The number of successfully inserted event rows.
     */
    public long addRecurringEvent(User user, RecurrenceType recurrenceType, int interval, String eventName, LocalDate eventStart, LocalTime eventTime, LocalDate endDate) {
        LocalDateTime startDateTime = LocalDateTime.of(eventStart, eventTime);
        LocalDateTime endDateTime;
        if (endDate != null) {
            endDateTime = LocalDateTime.of(endDate, eventTime);
        } else {
            endDateTime = LocalDateTime.of(LocalDate.now().plusYears(Constants.YEAR_OFFSET), eventTime);
        }
        if (endDateTime.isBefore(startDateTime)) {
            Toast.makeText(context, "Start date can not be after end date.", Toast.LENGTH_SHORT).show();
            return -1;
        }
        int occurrences = calculateOccurrences(startDateTime, endDateTime, recurrenceType, interval); // Calculate # of occurrences between endDateTime and startDateTime
        return addRecurringEvent(user, recurrenceType, interval, occurrences, eventName, eventStart, eventTime);
    }

    /**
     * Advances the given date based on recurrence type and interval.
     * CS-499 - Databases
     *
     * @param start The original start date.
     * @param type The type of recurrence (DAYS, WEEKS, etc.)
     * @param steps The total number of units to advance.
     * @return A LocalDate offset by the specified recurrence.
     */
    private LocalDate advanceDate(LocalDate start, RecurrenceType type, int steps, int interval) {
        switch (type) {
            case DAYS:
                return start.plusDays(steps * interval);
            case WEEKS:
                return start.plusWeeks(steps * interval);
            case MONTHS:
                return start.plusMonths(steps * interval);
            case YEARS:
                return start.plusYears(steps * interval);
            default:
                return start;
        }
    }

    //CS-499 - Databases
    private int calculateOccurrences(LocalDateTime start, LocalDateTime end, RecurrenceType type, int interval) {
        if (start.isAfter(end)) return 0;

        switch (type) {
            case DAYS:
                return (int) ChronoUnit.DAYS.between(start, end) / interval + 1;
            case WEEKS:
                return (int) ChronoUnit.WEEKS.between(start, end) / interval + 1;
            case MONTHS:
                return (int) ChronoUnit.MONTHS.between(start, end) / interval + 1;
            case YEARS:
                return (int) ChronoUnit.YEARS.between(start, end) / interval + 1;
            default:
                return 0;
        }
    }


    /**
     * Converts an epoch timestamp to a user-friendly date-time string.
     */
    public String formatEventTimestamp(long epochSeconds) {
        LocalDateTime dateTime = Instant.ofEpochSecond(epochSeconds)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        int currentYear = LocalDateTime.now().getYear();
        int eventYear = dateTime.getYear();
        DateTimeFormatter formatter;
        if (eventYear == currentYear) {
            formatter = DateTimeFormatter.ofPattern(Constants.EVENT_TIMESTAMP_PATTERN_CURRENT_YEAR);
        } else {
            formatter = DateTimeFormatter.ofPattern(Constants.EVENT_TIMESTAMP_PATTERN);
        }
        return dateTime.format(formatter);
    }

    /**
     * Retrieves a list of events for a given date.
     * CS-499 - Databases
     */
    public ArrayList<EventListAdapter.EventData> getEventsForDate(User user, LocalDate date) {
        ArrayList<EventListAdapter.EventData> events = new ArrayList<>();

        long startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        long endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        SQLiteDatabase db = this.getReadableDatabase();
        String[] args = {String.valueOf(user.getUserId()), String.valueOf(startOfDay), String.valueOf(endOfDay)};

        try (Cursor cursor = db.rawQuery(DatabaseConstants.LOOKUP_EVENTS_FOR_DATE_QUERY, args)) {
            while (cursor.moveToNext()) {
                //CS-499 - Databases
                String eventName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_EVENT_NAME));
                long eventEpoch = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_EVENT_DATE));
                String formattedDate = formatEventTimestamp(eventEpoch);
                long eventId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_EVENT_ID));
                int parentIndex = cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_EVENT_PARENT);
                long eventParentId = cursor.isNull(parentIndex) ? -1 : cursor.getLong(parentIndex);
                boolean isParent = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_IS_EVENT_PARENT)) == 1);

                events.add(new EventListAdapter.EventData(user.getUserId(), eventName, formattedDate, eventId, eventEpoch, eventParentId, isParent));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        // Sort by event time (epoch)
        //CS-499 - Algorithms
        events.sort(Comparator.comparingLong(EventListAdapter.EventData::getEventEpoch));

        return events;
    }

    /**
     * Deletes an event from the database by its ID.
     * //CS-499 - Databases
     * //CS-499 - software engineering
     */
    public boolean deleteEvent(User user, long eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        //CS-499: Using parameterized query to avoid SQL injection
        int rowsDeleted = db.delete(DatabaseConstants.TABLE_EVENTS, DatabaseConstants.COLUMN_USER_ID + " = ? AND " + DatabaseConstants.COLUMN_EVENT_ID + " = ?", new String[]{String.valueOf(user.getUserId()), String.valueOf(eventId)});
        db.close();
        return rowsDeleted > 0;
    }

    //CS-499 - Databases
    //CS-499 - Software engineering
    public String getPhoneNumber(User user) {
        SQLiteDatabase db = this.getReadableDatabase();
        int userId = user.getUserId();
        String phoneNumber = "";

        try (Cursor cursor = db.rawQuery(DatabaseConstants.LOOKUP_PHONE_NUMBER_QUERY, new String[]{String.valueOf(userId)})) {
            if (cursor.moveToFirst()) {
                phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_USER_PHONE));
                if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                    user.setPhoneNumber(phoneNumber);
                }
            }
        } catch (Exception e) {
            ErrorUtils.showAndLogError(context, "PhoneNumberLookupErr", "Error fetching phone number. Please try again.", e);
        } finally {
            db.close();
        }
        return phoneNumber;
    }

    /**
     * CS-499: Support phone number persistence
     * Updates the user's phone number only if it is currently null or empty.
     * @param user The person object representing the user whose phone number will be updated.
     * @param newPhoneNumber The phone number to update.
     * @return true if updated, false if no update needed or user not found.
     */
    public boolean updatePhoneNumber(User user, String newPhoneNumber) {
        boolean updated = false;
        int userId = user.getUserId();
        String currentPhoneNumber = user.getPhoneNumber();

        //Don't update if phone number does not change
        if (currentPhoneNumber != null && currentPhoneNumber.equals(newPhoneNumber)) {
            return false;
        }

        String currentPhone = getPhoneNumber(user);
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            if (currentPhone == null || currentPhone.trim().isEmpty() || !currentPhone.equals(newPhoneNumber)) {
                ContentValues values = new ContentValues();
                values.put(DatabaseConstants.COLUMN_USER_PHONE, newPhoneNumber);
                int rows = db.update(DatabaseConstants.TABLE_USERS, values, DatabaseConstants.COLUMN_USER_ID + " = ?", new String[]{String.valueOf(userId)});
                updated = rows > 0;
                user.setPhoneNumber(newPhoneNumber);
            }
        } catch (Exception e) {
            ErrorUtils.showAndLogError(context, "UpdatePhoneError", "Error updating phone number. Please try again.", e);
        } finally {
            db.close();
        }

        return updated;
    }

    //CS-499 - Databases
    //CS-499 - software engineering
    //CS-499 - algorithms
    public boolean deleteEventSeries(long parentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int deleted = db.delete(DatabaseConstants.TABLE_EVENTS, DatabaseConstants.COLUMN_EVENT_PARENT + " = ? OR " + DatabaseConstants.COLUMN_EVENT_ID + " = ?", new String[]{
                String.valueOf(parentId), String.valueOf(parentId)
        });
        db.close();
        return deleted > 0;
    }

    public ArrayList<EventListAdapter.EventData> getUpcomingEvents(User user, int limit) {
        ArrayList<EventListAdapter.EventData> upcomingEvents = new ArrayList<>();

        long now = Instant.now().getEpochSecond();

        SQLiteDatabase db = this.getReadableDatabase();

        String[] args = {
                String.valueOf(user.getUserId()),
                String.valueOf(now),
                String.valueOf(limit)
        };

        try (Cursor cursor = db.rawQuery(DatabaseConstants.LOOKUP_UPCOMING_EVENTS_QUERY, args)) {
            while (cursor.moveToNext()) {
                String eventName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_EVENT_NAME));
                long eventEpoch = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_EVENT_DATE));
                String formattedDate = formatEventTimestamp(eventEpoch);
                long eventId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_EVENT_ID));
                int parentIndex = cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_EVENT_PARENT);
                long eventParentId = cursor.isNull(parentIndex) ? -1 : cursor.getLong(parentIndex);
                boolean isParent = (cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseConstants.COLUMN_IS_EVENT_PARENT)) == 1);

                upcomingEvents.add(new EventListAdapter.EventData(user.getUserId(), eventName, formattedDate, eventId, eventEpoch, eventParentId, isParent));
            }
        } catch (Exception e) {
            ErrorUtils.showAndLogError(context, "EventLookupErr", "Error looking up events. Please try again.", e);
        } finally {
            db.close();
        }

        return upcomingEvents;
    }





}
