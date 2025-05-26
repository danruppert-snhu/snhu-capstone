package druppert.snhu.eventtracker.utils;

/**
 * PhoneUtils provides utility methods for phone number formatting and validation.
 *
 * CS-499 Enhancements:
 * - Introduced algorithmic normalization for user-entered phone numbers
 * - Ensures compatibility with E.164 format for global SMS support
 * - Reduces input errors and improves user experience with consistent formatting
 */
public class PhoneUtils {


    // Private constructor to prevent instantiation
    private PhoneUtils() {}

    /**
     * CS-499 - Algorithms:
     * Normalizes a user-entered phone number into E.164-compatible format.
     * Strips all non-digit characters.
     * Assumes US (+1) if the number has 10 digits.
     * Prepends '+' for international format if digits are 11–15 in length.
     * @param input Raw phone number from user input.
     * @return Normalized phone number or null if invalid.
     */
    public static String normalizePhoneNumber(String input) {
        if (input == null) return null;

        // Remove all non-digit characters
        String digits = input.replaceAll("\\D", "");

        // US fallback: If 10 digits, assume it's a US number, prepend +1
        if (digits.length() == Constants.US_PHONE_LENGTH) {
            return Constants.US_AREA_CODE + digits;
        }

        // If 11–15 digits, assume it's already international (maybe with leading country code)
        if (digits.length() >= Constants.US_PHONE_LENGTH_W_AREA && digits.length() <= Constants.INTERNATIONAL_PHONE_LENGTH_W_AREA) {
            return "+" + digits;
        }

        // Invalid length
        return null;
    }

}
