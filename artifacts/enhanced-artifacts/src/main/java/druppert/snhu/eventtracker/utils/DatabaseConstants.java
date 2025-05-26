package druppert.snhu.eventtracker.utils;

/**
 * DatabaseConstants centralizes all schema-related constants for EventSync.
 *
 * CS-499 Enhancements:
 * Eliminates hardcoded strings and magic numbers in SQL
 * Adds support for recurring events and indexing
 * Enables database versioning and schema management
 * Ensures maintainable, secure, and efficient database interactions
 */
public class DatabaseConstants {


    // -------------------------
    // Schema Version & Naming
    // -------------------------

    /** Database version for migrations */
    public static final int DATABASE_VERSION = 3;

    /** Name of the local SQLite database file */
    public static final String DATABASE_NAME = "event_sync_database";

    /** Minimum allowed password length */
    public static final int PASSWORD_LENGTH_REQUIREMENT = 8;

    // -------------------------
    // Table Names
    // -------------------------
    public static final String TABLE_USERS = "users";
    public static final String TABLE_EVENTS = "events";
    /** CS-499 - Databases: Table for recurrence metadata (e.g., DAILY, WEEKLY) */
    public static final String TABLE_RECURRENCE_TYPE = "recurrence_types";

    /** All defined table names (used for drop/create logic) */
    public static final String[] TABLES = {TABLE_USERS, TABLE_EVENTS,  TABLE_RECURRENCE_TYPE};

    // -------------------------
    // Column Names
    // -------------------------
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USER_NAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_PASSWORD_SALT = "salt";
    public static final String COLUMN_USER_PHONE = "phone_number";
    public static final String COLUMN_EVENT_ID = "event_id";
    public static final String COLUMN_EVENT_NAME = "event_name";
    //Epoch date of event
    public static final String COLUMN_EVENT_DATE = "event_date";
    public static final String COLUMN_EVENT_END_DATE = "end_date";
    public static final String COLUMN_EVENT_PARENT = "event_parent_key";
    public static final String COLUMN_IS_EVENT_PARENT = "is_recurring_parent";
    //CS-499 - Databases
    public static final String COLUMN_RECURRENCE_TYPE_KEY = "recurrence_type_key";
    //CS-499 - Databases
    public static final String COLUMN_RECURRENCE_TYPE_DESCRIPTION = "description";

    // -------------------------
    // Predefined Queries
    // -------------------------

    /** Query to authenticate a user by username and fetch their credentials */
    public static final String USER_AUTH_QUERY = "SELECT "
            + DatabaseConstants.COLUMN_USER_ID + ", "
            + DatabaseConstants.COLUMN_USER_PASSWORD + ", "
            + DatabaseConstants.COLUMN_USER_PASSWORD_SALT +
            " FROM " + DatabaseConstants.TABLE_USERS +
            " WHERE " + DatabaseConstants.COLUMN_USER_NAME + " = ?";

    /** Query to check for an existing username */
    public static final String USER_LOOKUP_ID_USERNAME_BY_USERNAME = "SELECT "
            + COLUMN_USER_ID + ", "
            + COLUMN_USER_NAME + " "
            + "FROM users "
            + "WHERE username = ?";

    /** Query to get a user's stored phone number */
    public static final String LOOKUP_PHONE_NUMBER_QUERY = "SELECT "
            + DatabaseConstants.COLUMN_USER_PHONE+ " FROM "
            + DatabaseConstants.TABLE_USERS + " WHERE "
            + DatabaseConstants.COLUMN_USER_ID + " = ?";

    /** Query to retrieve events on a specific day */
    public static final String LOOKUP_EVENTS_FOR_DATE_QUERY = "SELECT "
            + DatabaseConstants.COLUMN_EVENT_ID + ", "
            + DatabaseConstants.COLUMN_EVENT_NAME + ", "
            + DatabaseConstants.COLUMN_EVENT_DATE + ", "
            + DatabaseConstants.COLUMN_EVENT_PARENT + ", "
            + DatabaseConstants.COLUMN_IS_EVENT_PARENT + " FROM "
            + DatabaseConstants.TABLE_EVENTS + " WHERE "
            + DatabaseConstants.COLUMN_USER_ID  + " = ? AND "
            + DatabaseConstants.COLUMN_EVENT_DATE + " >= ? AND "
            + DatabaseConstants.COLUMN_EVENT_DATE + " < ?";


    /** Query to retrieve a list of upcoming events */
    public static final String LOOKUP_UPCOMING_EVENTS_QUERY = "SELECT "
            + DatabaseConstants.COLUMN_EVENT_ID + ", "
            + DatabaseConstants.COLUMN_EVENT_NAME + ", "
            + DatabaseConstants.COLUMN_EVENT_DATE + ", "
            + DatabaseConstants.COLUMN_EVENT_PARENT + ", "
            + DatabaseConstants.COLUMN_IS_EVENT_PARENT + " FROM "
            + DatabaseConstants.TABLE_EVENTS + " WHERE "
            + DatabaseConstants.COLUMN_USER_ID + " = ? AND "
            + DatabaseConstants.COLUMN_EVENT_DATE + " >= ? ORDER BY "
            + DatabaseConstants.COLUMN_EVENT_DATE + " ASC LIMIT ?";

    // -------------------------
    // Table DDL Definitions
    // -------------------------

    /** CS-499 - Databases: Create table for users with secure fields and constraints */
    public static final String CREATE_USER_TABLE_DDL = "CREATE TABLE " + DatabaseConstants.TABLE_USERS + "("
            + DatabaseConstants.COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DatabaseConstants.COLUMN_USER_NAME + " TEXT, "
            + DatabaseConstants.COLUMN_USER_PASSWORD + " TEXT, "
            + DatabaseConstants.COLUMN_USER_PASSWORD_SALT + " TEXT, "
            + DatabaseConstants.COLUMN_USER_PHONE + " TEXT, "
            + "UNIQUE("+DatabaseConstants.COLUMN_USER_NAME+")"
            + ");";


    /** CS-499 - Databases: Create table for events with recurrence support */
    public static final String CREATE_DATA_TABLE_DDL = "CREATE TABLE " + TABLE_EVENTS + " ("
            + DatabaseConstants.COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DatabaseConstants.COLUMN_USER_ID + " INTEGER, "
            + DatabaseConstants.COLUMN_EVENT_NAME + " TEXT, "
            + DatabaseConstants.COLUMN_EVENT_DATE + " INTEGER, "
            + DatabaseConstants.COLUMN_RECURRENCE_TYPE_KEY + " INTEGER, "
            + DatabaseConstants.COLUMN_EVENT_END_DATE + " INTEGER, "
            + DatabaseConstants.COLUMN_EVENT_PARENT + " INTEGER, "
            + DatabaseConstants.COLUMN_IS_EVENT_PARENT + " INTEGER DEFAULT 0, "
            + "FOREIGN KEY(" + DatabaseConstants.COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
            + "FOREIGN KEY (" + DatabaseConstants.COLUMN_EVENT_PARENT + ") REFERENCES " + TABLE_EVENTS + "(" + COLUMN_EVENT_ID + ")"
            + ")";

    /** CS-499 - Databases: Create lookup table for recurrence types */
    public static final String CREATE_RECURRENCE_TYPE_TABLE_DDL = "CREATE TABLE " + TABLE_RECURRENCE_TYPE + " ("
            + COLUMN_RECURRENCE_TYPE_KEY + " INTEGER PRIMARY KEY, "
            + COLUMN_RECURRENCE_TYPE_DESCRIPTION + " TEXT"
            + ")";

    // -------------------------
    // CS-499 Index DDL Definitions
    // -------------------------
    public static final String CREATE_USERNAME_IDX_USER_TABLE = "CREATE INDEX idx_username ON " + TABLE_USERS + " (" + COLUMN_USER_NAME + ");";
    public static final  String CREATE_USER_ID_IDX_EVENT_TABLE = "CREATE INDEX idx_event_user_id ON " + TABLE_EVENTS + " (" + COLUMN_USER_ID + ");";
    public static final String CREATE_EVENT_NAME_IDX_EVENT_TABLE = "CREATE INDEX idx_event_name ON " + TABLE_EVENTS + " (" + COLUMN_EVENT_NAME + ");";
    public static final String CREATE_EVENT_DATE_IDX_EVENT_TABLE = "CREATE INDEX idx_event_date ON " + TABLE_EVENTS + " (" + COLUMN_EVENT_DATE + ");";

    // -------------------------
    // Utility Arrays
    // -------------------------

    /** All table creation statements for initial schema setup */
    public static final String[] CREATE_TABLE_STATEMENTS = {
            CREATE_USER_TABLE_DDL,
            CREATE_DATA_TABLE_DDL,
            CREATE_RECURRENCE_TYPE_TABLE_DDL
                                                        };

    /** All index creation statements for performance tuning */
    public static final String[] CREATE_IDX_STATEMENTS = {
                                                            CREATE_USERNAME_IDX_USER_TABLE,
                                                            CREATE_EVENT_NAME_IDX_EVENT_TABLE,
                                                            CREATE_EVENT_DATE_IDX_EVENT_TABLE,
                                                            CREATE_USER_ID_IDX_EVENT_TABLE
                                                        };


}
