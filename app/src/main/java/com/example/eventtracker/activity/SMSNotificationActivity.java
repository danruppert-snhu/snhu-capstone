package com.example.eventtracker.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.eventtracker.R;
import com.example.eventtracker.db.DatabaseHelper;
import com.example.eventtracker.entity.User;
import com.example.eventtracker.utils.Constants;
import com.example.eventtracker.utils.ErrorUtils;
import com.example.eventtracker.utils.PhoneUtils;

public class SMSNotificationActivity extends AppCompatActivity {

    private TextView smsPermissionStatus;
    private Button smsRequestButton;
    private EditText phoneNumberInput;
    private Button savePhoneNumberButton;
    private DatabaseHelper dbHelper;

    private User user;

    /**
     * Activity for managing SMS notification permissions and sending a test SMS.
     * Includes runtime permission handling.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_notification);

        //CS-499 - Software Engineering: persist user
        user = getIntent().getSerializableExtra(Constants.USER_INTENT_KEY, User.class);

        // Initialize UI components
        smsPermissionStatus = findViewById(R.id.sms_permission_status);
        smsRequestButton = findViewById(R.id.sms_request_button);
        //CS-499 - Software Engineering: complete SMS implementation
        savePhoneNumberButton = findViewById(R.id.save_phone_number_button);

        //CS-499 - Software Engineering:  Finalize phone number input for SMS support
        phoneNumberInput = findViewById(R.id.phone_number_input);
        dbHelper = new DatabaseHelper(this);


        String phoneNumber = user.getPhoneNumber();
        //CS-499 - Databases and Software engineering: handle user-specific phone number persistence.
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            phoneNumber = dbHelper.getPhoneNumber(user);
        }
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            phoneNumberInput.setHint("");
            phoneNumberInput.setText(phoneNumber);
        }

        // Check and display current SMS permission status on load
        checkAndDisplaySMSPermissionStatus();

        // Handle button click: request permission or send SMS depending on current status
        //CS-499 - Software engineering: modularize lambdas for readability
        smsRequestButton.setOnClickListener(v -> handleSMSButton());
        //CS-499 - Software engineering: modularize lambdas for readability
        savePhoneNumberButton.setOnClickListener(v -> {
            savePhoneNumber();
            //CS-499 - Software engineering: modularize lambdas for readability
            //Only show the phone number saved notification when the save button is pressed, even though we save it when users send or press save
            Toast.makeText(this, "Phone number saved.", Toast.LENGTH_SHORT).show();
        });
    }

    //CS-499 - Software engineering: modularize lambdas for readability
    private void handleSMSButton() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Ask for permission if not granted
            requestSMSPermission();
        } else {
            // Send SMS if permission already granted
            sendSMS();
        }
    }

    /**
     * Inflate the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sms_menu, menu);
        return true;
    }

    /**
     * Handle menu item click (redirects back to the Event List screen).
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, EventListActivity.class);
            intent.putExtra(Constants.USER_INTENT_KEY, user);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks current SMS permission and updates UI with appropriate status and button text.
     */
    private void checkAndDisplaySMSPermissionStatus() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            smsPermissionStatus.setText("SMS Permission Status: Granted");
            smsRequestButton.setText("Send SMS");
        } else {
            smsPermissionStatus.setText("SMS Permission Status: Denied");
            smsRequestButton.setText("Request SMS Permission");
        }
    }

    /**
     * Requests the SEND_SMS permission from the user, showing rationale if needed.
     */
    private void requestSMSPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
            Toast.makeText(this, "SMS permission is required to send notifications.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, Constants.SMS_PERMISSION_CODE);
    }

    /**
     * Callback after the user responds to the permission request.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                smsPermissionStatus.setText("SMS Permission Status: Granted");
                smsRequestButton.setText("Send SMS");
            } else {
                smsPermissionStatus.setText("SMS Permission Status: Denied");
                Toast.makeText(this, "Please enable SMS permission in settings.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * CS-499 - Software engineering: modularize lambdas for readability
     */
    private boolean savePhoneNumber() {
        String rawPhone = phoneNumberInput.getText().toString().trim();
        String phoneNumber = PhoneUtils.normalizePhoneNumber(rawPhone);

        if (phoneNumber != null && phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // CS-499 Enhancement: Validate format of phone number before attempting to send
        if (phoneNumber == null) {
            Toast.makeText(this, "Invalid phone number format.", Toast.LENGTH_SHORT).show();
            return false;
        }

        try {
            //CS-499: Finalize phone number input for SMS support
            boolean success = dbHelper.updatePhoneNumber(user, phoneNumber);
            if (success)
                user.setPhoneNumber(phoneNumber);
            return success;
        } catch (Exception e) {
            ErrorUtils.showAndLogError(this, "PhoneNumberError", "Phone number change failed. Please try again.", e);
        }
        return false;
    }

    /**
     * Sends an SMS message to a phone number entered by the user.
     */
    private void sendSMS() {
        boolean phoneNumberValid = savePhoneNumber();

        if (!phoneNumberValid) {
            return;
        }

        String phoneNumber = user.getPhoneNumber();

        String message = "This is an SMS notification from EventSync.";

        try {

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            smsPermissionStatus.setText("SMS sent successfully.");
            Toast.makeText(this, "SMS sent successfully.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            smsPermissionStatus.setText("Failed to send SMS");
            ErrorUtils.showAndLogError(this, "SMSError", "Error sending SMS. Please try again.", e);
        }
    }
}
