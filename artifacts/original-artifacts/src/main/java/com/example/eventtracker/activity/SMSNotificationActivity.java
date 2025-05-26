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

public class SMSNotificationActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 100;
    private TextView smsPermissionStatus;
    private Button smsRequestButton;
    private EditText phoneNumberInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_notification);

        smsPermissionStatus = findViewById(R.id.sms_permission_status);
        smsRequestButton = findViewById(R.id.sms_request_button);

        checkAndDisplaySMSPermissionStatus();

        smsRequestButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestSMSPermission();
            } else {
                sendSMS();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sms_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, EventListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

    private void requestSMSPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
            Toast.makeText(this, "SMS permission is required to send notifications.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                smsPermissionStatus.setText("SMS Permission Status: Granted");
                smsRequestButton.setText("Send SMS");
            } else {
                smsPermissionStatus.setText("SMS Permission Status: Denied");
                Toast.makeText(this, "Please enable SMS permission in settings.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void sendSMS() {
        String phoneNumber = phoneNumberInput.getText().toString().trim();
        String message = "This is an SMS notification from EventSync.";

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            smsPermissionStatus.setText("SMS sent successfully.");
        } catch (Exception e) {
            smsPermissionStatus.setText("Failed to send SMS: " + e.getMessage());
        }
    }
}
