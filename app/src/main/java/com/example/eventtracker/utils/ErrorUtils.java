package com.example.eventtracker.utils;

import android.content.Context;
import android.widget.Toast;
import android.util.Log;
public class ErrorUtils {

    //CS-499 - software engineering
    public static void showAndLogError(Context context, String errorReason, String message, Exception e) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        Log.e(errorReason, message, e);
    }
}
