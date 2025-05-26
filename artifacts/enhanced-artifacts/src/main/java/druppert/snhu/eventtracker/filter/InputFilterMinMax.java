package druppert.snhu.eventtracker.filter;

import android.text.InputFilter;
import android.text.Spanned;

/**
 * InputFilterMinMax constrains EditText input to a specified numeric range.
 * Implements an algorithm to enforce numeric constraints in real time.
 * Commonly used to restrict integer input fields such as recurrence intervals.
 * CS-499
 */
public class InputFilterMinMax implements InputFilter {
    private final int min, max;

    /**
     * Initializes the filter with a minimum and maximum allowed integer value.
     *
     * @param min Minimum allowable value (inclusive).
     * @param max Maximum allowable value (inclusive).
     */
    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }
    /**
     * Filters user input in real time to ensure the resulting value remains within [min, max].
     *
     * CS-499 - Algorithms: Validates bounds using a conditional check strategy.
     *
     * @param source New text being inserted by the user.
     * @param start Start index in the source text.
     * @param end End index in the source text.
     * @param dest Current content of the EditText.
     * @param dstart Start index in the destination where new text will be applied.
     * @param dend End index in the destination where new text will be applied.
     * @return Null to accept the input; an empty string to reject it.
     */
    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        try {
            String input = dest.toString().substring(0, dstart) + source + dest.toString().substring(dend);
            if (input.isEmpty()) return null;
            int value = Integer.parseInt(input);
            if (value >= min && value <= max) return null;
        } catch (NumberFormatException ignored) {}
        return "";
    }
}