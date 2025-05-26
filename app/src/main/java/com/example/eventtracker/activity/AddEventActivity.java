package com.example.eventtracker.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eventtracker.R;
import com.example.eventtracker.db.DatabaseHelper;
import com.example.eventtracker.entity.User;
import com.example.eventtracker.filter.InputFilterMinMax;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.function.Consumer;

public class AddEventActivity extends AppCompatActivity {

    private TextView eventDateInput;
    private TextView eventTimeInput;
    private TextView eventNameInput;
    private Button saveEventButton;
    private CheckBox repeatCheckbox;
    private LinearLayout recurrenceOptions;
    private LinearLayout repeatUntil;

    private EditText intervalText;
    private TextView endsOnText;
    private EditText endsOnDatePicker;
    private Spinner recurrenceTypeSpinner;
    private ArrayAdapter<String> recurrenceAdapter;


    private DatabaseHelper dbHelper;

    private User user;

    private LocalDate eventStartDate, eventEndDate;

    private LocalTime eventTime;

    /**
     * Activity for creating a new event by allowing the user to select a date, time, and name.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        user = getIntent().getSerializableExtra("user", User.class);

        // Initialize database helper
        dbHelper = new DatabaseHelper(this);

        // Initialize UI components
        eventDateInput = findViewById(R.id.event_date_input);
        eventTimeInput = findViewById(R.id.event_time_input);
        saveEventButton = findViewById(R.id.save_event_button);
        eventNameInput = findViewById(R.id.event_name_input);
        repeatCheckbox = findViewById(R.id.recurrence_checkbox);
        recurrenceOptions = findViewById(R.id.recurrence_options);
        recurrenceTypeSpinner = findViewById(R.id.recurrence_type_spinner);
        intervalText = findViewById(R.id.recurrence_interval_input);
        repeatUntil = findViewById(R.id.repeat_until);
        endsOnText = findViewById(R.id.ends_on_textview);
        endsOnDatePicker = findViewById(R.id.recurrence_end_date_input);

        // Show a calendar dialog when user clicks the event date field
        eventDateInput.setOnClickListener(v -> showDatePickerDialog(eventDateInput, date -> eventStartDate = date));

        // Show a time picker dialog when user clicks the event time field
        eventTimeInput.setOnClickListener(v -> showTimePickerDialog(eventTimeInput, time -> eventTime = time));
        endsOnDatePicker.setOnClickListener(v -> showDatePickerDialog(endsOnDatePicker, date -> eventEndDate = date));

        // Trigger event saving when Save button is clicked
        saveEventButton.setOnClickListener(v -> saveEvent());
        repeatCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            recurrenceOptions.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            repeatUntil.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        intervalText.setFilters(new InputFilter[]{
                new InputFilterMinMax(1, 999)
        });


        // Load singular by default
        recurrenceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.recurrence_types));
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recurrenceTypeSpinner.setAdapter(recurrenceAdapter);

        // Watch interval for changes
        intervalText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                int interval = 1;
                try {
                    interval = Integer.parseInt(s.toString());
                } catch (NumberFormatException ignored) {}

                boolean usePlural = interval > 1;
                updateRecurrenceSpinnerOptions(usePlural);
            }
        });

    }

    private void updateRecurrenceSpinnerOptions(boolean usePlural) {
        int currentPosition = recurrenceTypeSpinner.getSelectedItemPosition();

        int arrayRes = usePlural
                ? R.array.recurrence_types_plural
                : R.array.recurrence_types;

        recurrenceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(arrayRes));
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recurrenceTypeSpinner.setAdapter(recurrenceAdapter);
        recurrenceTypeSpinner.setSelection(currentPosition);
    }

    /**
     * Saves the event to the database after validating user input.
     */
    private void saveEvent() {
        // Validate that all required inputs are provided
        if (eventStartDate == null || eventTime == null || eventNameInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter an event name and select both date and time", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gather user input
        String eventName = eventNameInput.getText().toString();
        long result = -1;
        if (!repeatCheckbox.isChecked()) {
            //Fetch the user who this event is being created / updated for.
            // Persist the event to the database
            result = dbHelper.addEvent(user, eventName, eventStartDate, eventTime);
        } else {
            int interval = Integer.parseInt(intervalText.getText().toString());
            String recurrenceTypeSelected = recurrenceTypeSpinner.getSelectedItem().toString();
            if (recurrenceTypeSelected.endsWith("s") && recurrenceTypeSelected.length() > 3) {
                recurrenceTypeSelected = recurrenceTypeSelected.substring(0, recurrenceTypeSelected.length() - 1);
            }
            DatabaseHelper.RecurrenceType recurrenceType = DatabaseHelper.RecurrenceType.fromDbValue(recurrenceTypeSelected);
            result = dbHelper.addRecurringEvent(user, recurrenceType,  interval,  eventName,  eventStartDate,  eventTime,  eventEndDate);
        }
        if (result != -1) {
            // If save successful, return to the Event List screen
            Intent intent = new Intent(AddEventActivity.this, EventListActivity.class);
            intent.putExtra("user", user);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // Handle failure
            Toast.makeText(this, "Error saving event", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays a calendar picker dialog for selecting the event date.
     */
    private void showDatePickerDialog(TextView textView, Consumer<LocalDate> date) {
        //When the "Date" text field is clicked, this displays a calendar for the user to select a day of the year.
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create and show date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, yearSelected, monthSelected, daySelected) -> {
                    // Format date as MM/DD/YYYY and set it in the input field
                    String dateString = (monthSelected + 1) + "/" + daySelected + "/" + yearSelected;
                    LocalDate selectedDate = LocalDate.of(yearSelected, monthSelected + 1, daySelected);
                    date.accept(selectedDate);
                    textView.setText(dateString);
                }, year, month, day);

        datePickerDialog.show();
    }

    /**
     * Displays a time picker dialog for selecting the event time.
     */
    private void showTimePickerDialog(TextView textView, Consumer<LocalTime> time) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create and show time picker dialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteSelected) -> {
                    LocalTime selectedTime = LocalTime.of(hourOfDay, minuteSelected);
                    time.accept(selectedTime);

                    // Convert to 12-hour format and format string with AM/PM
                    String amPm = (hourOfDay < 12) ? "AM" : "PM";
                    int displayHour = (hourOfDay % 12 == 0) ? 12 : hourOfDay % 12;

                    String timeString = String.format("%02d:%02d %s", displayHour, minuteSelected, amPm);
                    textView.setText(timeString);
                }, hour, minute, false); // 'false' means to use the 12-hour format

        timePickerDialog.show();
    }
}
