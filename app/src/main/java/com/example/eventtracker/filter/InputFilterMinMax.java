package com.example.eventtracker.filter;

import android.text.InputFilter;
import android.text.Spanned;

public class InputFilterMinMax implements InputFilter {
    private final int min, max;

    public InputFilterMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }
    //CS-499 - algorithms
    @Override
    public CharSequence filter(CharSequence source, int start, int end,
                               Spanned dest, int dstart, int dend) {
        try {
            String input = dest.toString().substring(0, dstart) + source + dest.toString().substring(dend);
            if (input.isEmpty()) return null; // Allow deletion
            int value = Integer.parseInt(input);
            if (value >= min && value <= max) return null;
        } catch (NumberFormatException ignored) {}
        return "";
    }
}