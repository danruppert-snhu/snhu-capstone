package com.example.eventtracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

import com.example.eventtracker.adapter.EventListAdapter;
import com.example.eventtracker.utils.SecurityUtils;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "event_sync_database";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_EVENTS = "events";

    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "username";
    private static final String COLUMN_USER_PASSWORD = "password";

    private static final String COLUMN_USER_PASSWORD_SALT = "salt";

    private static final String COLUMN_EVENT_ID = "event_id";
    private static final String COLUMN_EVENT_NAME = "event_name";
    //Epoch date of event
    private static final String COLUMN_EVENT_DATE = "event_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_NAME + " TEXT, "
                + COLUMN_USER_PASSWORD + " TEXT, "
                + COLUMN_USER_PASSWORD_SALT + " TEXT, "
                + "UNIQUE("+COLUMN_USER_NAME+")"
                + ");";

        String createDataTable = "CREATE TABLE " + TABLE_EVENTS + "("
                + COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EVENT_NAME + " TEXT,"
                + COLUMN_EVENT_DATE + " INTEGER"
                + ")";

        db.execSQL(createUserTable);
        db.execSQL(createDataTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }

    public boolean addUser(String username, String passwordHash, String salt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues userValues = new ContentValues();
        userValues.put(COLUMN_USER_NAME, username);
        userValues.put(COLUMN_USER_PASSWORD, passwordHash);
        userValues.put(COLUMN_USER_PASSWORD_SALT, salt);
        long result = db.insert(TABLE_USERS, null,userValues);
        db.close();
        return result != -1;
    }

    public boolean checkUserExists(String username) {
        boolean userExists = false;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT "
                + COLUMN_USER_ID + ", "
                + COLUMN_USER_NAME + " "
                + "FROM users "
                + "WHERE username = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{username})) {
            userExists = cursor.getCount() > 0;
        } finally {
            db.close();
        }
        return userExists;
    }

    public boolean checkPasswordStrength(String password) {
        if (password == null || password.length() < 8) {
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

    public boolean authenticateUser(String username, String password) {
        boolean validCredentials = false;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String storedSalt, storedHash, providedHash;
            String query = "SELECT "
                    + COLUMN_USER_ID + ", "
                    + COLUMN_USER_NAME + ", "
                    + COLUMN_USER_PASSWORD + ", "
                    + COLUMN_USER_PASSWORD_SALT + " "
                    + "FROM users "
                    + "WHERE username = ?";
            try (Cursor cursor = db.rawQuery(query, new String[]{username})) {
                boolean userExists = cursor.getCount() > 0;
                if (userExists && cursor.moveToFirst()) {
                    storedHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD));
                    storedSalt = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD_SALT));
                    providedHash = SecurityUtils.hashPassword(password, storedSalt);
                    validCredentials = providedHash.equals(storedHash);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return validCredentials;
    }


    public long addEvent(String eventName, LocalDate eventDate, LocalTime eventTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_NAME, eventName);

        LocalDateTime eventDateTime = LocalDateTime.of(eventDate, eventTime);
        long eventDateEpoch = eventDateTime.toEpochSecond(ZoneOffset.systemDefault().getRules().getOffset(eventDateTime));

        values.put(COLUMN_EVENT_DATE, eventDateEpoch);

        long result = db.insert(TABLE_EVENTS, null, values);
        db.close();
        return result;
    }

    public String formatEventTimestamp(long epochSeconds) {
        LocalDateTime dateTime = Instant.ofEpochSecond(epochSeconds)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, hh:mm a");
        return dateTime.format(formatter);
    }

    public ArrayList<EventListAdapter.EventData> getEventsForDate(LocalDate date) {
        ArrayList<EventListAdapter.EventData> events = new ArrayList<>();

        long startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        long endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_EVENT_ID + ", " + COLUMN_EVENT_NAME + ", " + COLUMN_EVENT_DATE + " FROM " + TABLE_EVENTS +
                " WHERE " + COLUMN_EVENT_DATE + " >= ? AND " + COLUMN_EVENT_DATE + " < ?";
        String[] args = {String.valueOf(startOfDay), String.valueOf(endOfDay)};

        try (Cursor cursor = db.rawQuery(query, args)) {
            while (cursor.moveToNext()) {
                String eventName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EVENT_NAME));
                long eventEpoch = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_DATE));
                String formattedDate = formatEventTimestamp(eventEpoch);
                long eventId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EVENT_ID));

                events.add(new EventListAdapter.EventData(eventName, formattedDate, eventId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }

        return events;
    }

    public boolean deleteEvent(long eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete(TABLE_EVENTS, COLUMN_EVENT_ID + " = ?", new String[]{String.valueOf(eventId)});
        db.close();
        return rowsDeleted > 0;
    }


}
