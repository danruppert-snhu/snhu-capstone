package com.example.eventtracker.utils;


public class Constants {

    //CS-499 - Software engineering: eliminate magic numbers/strings
    /**
     * User session attributes
     */
    public static final String USER_INTENT_KEY = "user";

    /**
     * Phone number normalization constants
     */
    public static final int US_PHONE_LENGTH_W_AREA = 11;
    public static final int INTERNATIONAL_PHONE_LENGTH_W_AREA = 15;
    public static final int US_PHONE_LENGTH = 10;
    public static final String US_AREA_CODE = "+1";

    /**
     * Date/Time Formatting
     */
    public static final String TIME_FORMAT = "%02d:%02d %s";
    public static final String EVENT_DATE_PATTERN = "MMMM d, yyyy";
    public static final String EVENT_TIMESTAMP_PATTERN_CURRENT_YEAR = "MMM dd, hh:mm a";
    public static final String EVENT_TIMESTAMP_PATTERN = "MMM dd yyyy, hh:mm a";


    /**
     * Password Encryption and Hashing constants
     */

    public static final int SALT_LENGTH = 16;

    //CS-499 - software engineering: Increase PPBKDF2 iteration count from 65k to 100k
    public static final int HASH_ITERATIONS = 100000;
    public static final int KEY_LENGTH = 128;
    public static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";

    /**
     * Event List Constants
     *
     */
    //CS-499 - Algorithms: Least-Recently-Used (LRU) cache size, limit to a maximum of 50 events.
    public static final int MAX_CACHE_SIZE = 50;
    //CS-499 - Algorithms: Maximum number of events to show on the event list (both upcoming and selected date lists)
    public static final int MAX_EVENTS_TO_SHOW = 50;

    //CS-499 - Algorithms: How full the HashMap can become before resizing.
    public static final float EVENT_CACHE_LOAD_FACTOR = 0.75f;

    public static final int DAYS_IN_WEEK = 7;
    public static final int YEAR_OFFSET = 50;
    public static final String[] MONTHS_IN_YEAR = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    /**
     * SMS Permissions
     */

    public static final int SMS_PERMISSION_CODE = 100;

    /**
     * Recurring event constants
     */
    public static final int RECURRING_EVENT_MIN_DAYS = 1;
    public static final int RECURRING_EVENT_MAX_DAYS = 999;


}
