package druppert.snhu.eventtracker.utils;


/**
 * Constants holds global configuration and magic value replacements for EventSync.
 *
 * Eliminates magic numbers and strings
 * Handles password hashing security
 * Centralizes all constants for maintainability and clarity
 */
public class Constants {

    /** CS-499 - Software Engineering: User session key for Intent bundling */
    public static final String USER_INTENT_KEY = "user";

    // ------------------------
    // Phone Number Normalization
    // ------------------------

    /** Length of a U.S. phone number including country code (e.g., +1xxxxxxxxxx) */
    public static final int US_PHONE_LENGTH_W_AREA = 11;

    /** Max length of an international phone number including area code */
    public static final int INTERNATIONAL_PHONE_LENGTH_W_AREA = 15;

    /** Standard U.S. phone number length without country code */
    public static final int US_PHONE_LENGTH = 10;

    /** Default U.S. area code prefix */
    public static final String US_AREA_CODE = "+1";

    // ------------------------
    // Date & Time Formatting
    // ------------------------

    /** Format for displaying time in 12-hour format with AM/PM */
    public static final String TIME_FORMAT = "%02d:%02d %s";

    /** Format for displaying full date (e.g., "October 21, 2025") */
    public static final String EVENT_DATE_PATTERN = "MMMM d, yyyy";

    /** Format for displaying date/time for current year events */
    public static final String EVENT_TIMESTAMP_PATTERN_CURRENT_YEAR = "MMM dd, hh:mm a";

    /** Format for displaying date/time including year for non-current year events */
    public static final String EVENT_TIMESTAMP_PATTERN = "MMM dd yyyy, hh:mm a";

    // ------------------------
    // Password Encryption & Hashing
    // ------------------------

    /** Number of bytes for randomly generated salt */
    public static final int SALT_LENGTH = 16;

    /** CS-499 - Security: Increased PBKDF2 iteration count for stronger hashes */
    public static final int HASH_ITERATIONS = 100000;

    /** Desired key length in bits for derived password hash */
    public static final int KEY_LENGTH = 128;

    /** Algorithm used for key derivation in password hashing */
    public static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA1";

    // ------------------------
    // Event List & Caching
    // ------------------------

    /** CS-499 - Algorithms: Max number of items to keep in LRU cache for selected-day event lookups */
    public static final int MAX_CACHE_SIZE = 50;

    /** CS-499 - Algorithms: Max number of events shown at once for any query */
    public static final int MAX_EVENTS_TO_SHOW = 50;

    /** CS-499 - Algorithms: Load factor for backing HashMap in LRU cache */
    public static final float EVENT_CACHE_LOAD_FACTOR = 0.75f;

    // ------------------------
    // Calendar UI
    // ------------------------

    /** Number of days in a week for calendar grid layout */
    public static final int DAYS_IN_WEEK = 7;

    /** Number of years before and after the current year to display in date pickers */
    public static final int YEAR_OFFSET = 50;

    /** Display labels for each month in abbreviated form */
    public static final String[] MONTHS_IN_YEAR = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    // ------------------------
    // SMS & Permissions
    // ------------------------

    /** Request code for runtime permission to send SMS */
    public static final int SMS_PERMISSION_CODE = 100;

    // ------------------------
    // Recurring Events
    // ------------------------

    /** Minimum allowed interval between recurring events (in days) */
    public static final int RECURRING_EVENT_MIN_DAYS = 1;

    /** Maximum allowed interval between recurring events (in days) */
    public static final int RECURRING_EVENT_MAX_DAYS = 999;


}
