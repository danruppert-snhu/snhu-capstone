package com.example.eventtracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventtracker.R;
import com.example.eventtracker.db.DatabaseHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Calendar;

public class AddEventActivity extends AppCompatActivity {

    private TextView eventDateInput;
    private TextView eventTimeInput;
    private TextView eventNameInput;
    private Button saveEventButton;
    private DatabaseHelper dbHelper;

    private int selectedYear, selectedMonth, selectedDay;
    private int selectedHour, selectedMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        dbHelper = new DatabaseHelper(this);

        // Initialize UI components
        eventDateInput = findViewById(R.id.event_date_input);
        eventTimeInput = findViewById(R.id.event_time_input);
        saveEventButton = findViewById(R.id.save_event_button);
        eventNameInput = findViewById(R.id.event_name_input);

        // Handle date input
        eventDateInput.setOnClickListener(v -> showDatePickerDialog());

        // Handle time input
        eventTimeInput.setOnClickListener(v -> showTimePickerDialog());

        saveEventButton.setOnClickListener(v -> saveEvent());
    }

    private void saveEvent() {
        if (selectedYear == 0 || selectedHour == -1 || eventNameInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter an event name and select both date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        String eventName = eventNameInput.getText().toString();
        LocalDate eventDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay);
        LocalTime eventTime = LocalTime.of(selectedHour, selectedMinute);

        long result = dbHelper.addEvent(eventName, eventDate, eventTime);
        if (result != -1) {
            Intent intent = new Intent(AddEventActivity.this, EventListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Error saving event", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDatePickerDialog() {
        //When the "Date" text field is clicked, this displays a calendar for the user to select a day of the year.
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, yearSelected, monthSelected, daySelected) -> {
                    selectedYear = yearSelected;
                    selectedMonth = monthSelected;
                    selectedDay = daySelected;

                    String dateString = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                    eventDateInput.setText(dateString);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteSelected) -> {
                    selectedHour = hourOfDay; // Use hourOfDay directly
                    selectedMinute = minuteSelected;

                    // Format and display the time in 12-hour format with AM/PM
                    String amPm = (hourOfDay < 12) ? "AM" : "PM";
                    int displayHour = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;

                    String timeString = String.format("%02d:%02d %s", displayHour, selectedMinute, amPm);
                    eventTimeInput.setText(timeString);
                }, hour, minute, false); // 'false' for 24-hour format dialog

        timePickerDialog.show();
    }
}
