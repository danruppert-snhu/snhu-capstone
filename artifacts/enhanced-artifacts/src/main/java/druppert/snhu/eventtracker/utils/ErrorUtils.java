package druppert.snhu.eventtracker.utils;

import android.content.Context;
import android.widget.Toast;
import android.util.Log;

/**
 * ErrorUtils provides a centralized mechanism for error handling in EventSync.
 *
 * Standardized error logging and user-facing messaging
 * Reduces duplicated Toast/Log logic across the app
 * Promotes clean separation of concerns (UI vs. diagnostics)
 */
public class ErrorUtils {

    /**
     * CS-499
     * Displays a toast notification and logs the full error stack trace.
     *
     * @param context      The context in which to show the toast
     * @param errorReason  A tag or identifier for logcat filtering
     * @param message      The user-facing error message
     * @param e            The caught exception to log
     */
    public static void showAndLogError(Context context, String errorReason, String message, Exception e) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        Log.e(errorReason, message, e);
    }
}
