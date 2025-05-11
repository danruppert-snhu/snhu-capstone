package com.example.eventtracker.activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventtracker.adapter.EventAdapter;
import com.example.eventtracker.adapter.EventListAdapter;
import com.example.eventtracker.R;
import com.example.eventtracker.db.DatabaseHelper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class EventListActivity extends AppCompatActivity {

    private boolean isFirstLoad = true;
    private RecyclerView calendarRecyclerView;
    private RecyclerView eventListRecyclerView;
    private EventAdapter eventAdapter;
    private EventListAdapter eventListAdapter;
    private TextView monthYearText;
    private ArrayList<String> dateList;
    private LocalDate selectedMonthYearDate;
    private LocalDate selectedDate;

    private Button addEventButton;
    private ImageButton previousMonthButton;
    private ImageButton nextMonthButton;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        dbHelper = new DatabaseHelper(this);

        //Get the calendar header text field (non-editable)
        monthYearText = findViewById(R.id.month_year_text);
        monthYearText.setOnClickListener(v -> showMonthYearPickerDialog());

        previousMonthButton = findViewById(R.id.button_previous_month);
        previousMonthButton.setOnClickListener(v -> navigateToPreviousMonth());

        nextMonthButton = findViewById(R.id.button_next_month);
        nextMonthButton.setOnClickListener(v -> navigateToNextMonth());


        //Get the recycler view for the calendar, and format it as a grid.
        calendarRecyclerView = findViewById(R.id.calendar_recycler_view);
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));

        //Get the recycler view for the event list (ultimately when dates are selected on the calendar) and format it as a linear list.
        eventListRecyclerView = findViewById(R.id.event_list_recycler);
        eventListRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        //The label at the top of the Event List screen. For example, "October 2024"
        // This will ultimately have navigation capabilities to choose other months / years
        // It defaults to the current month.
        selectedMonthYearDate = LocalDate.now();
        selectedDate = LocalDate.now();
        setMonthView();

        ArrayList<EventListAdapter.EventData> eventList = dbHelper.getEventsForDate(selectedDate);

        //Create an adapter to allow us to dynamically define, display, and interact with objects in the linear event list recycler view
        //For now, the objects are created using mock "Events" defined by the loop above. Ultimately, this will be revised to fetch events from the database for a selected date.
        //Dates will need an indicator added if there is actually data for a given date, but that can't be implemented until this code is connected to a data source.
        eventListAdapter = new EventListAdapter(eventList, dbHelper);
        eventListRecyclerView.setAdapter(eventListAdapter);
        addEventButton = findViewById(R.id.add_event_button);

        //Open the "Add Event" screen when the add event button is clicked.
        addEventButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventListActivity.this, AddEventActivity.class);
            startActivity(intent);
        });
    }

    // Show the settings wrench on the app bar, this currently redirects to the SMS activity screen.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.event_list_menu, menu);
        return true;
    }

    //Load the SMS activity screen when the settings button is pressed.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(EventListActivity.this, SMSNotificationActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToPreviousMonth() {
        selectedMonthYearDate = selectedMonthYearDate.minusMonths(1);
        setMonthView();
    }

    private void navigateToNextMonth() {
        selectedMonthYearDate = selectedMonthYearDate.plusMonths(1);
        setMonthView();
    }

    private void setMonthView() {
        monthYearText.setText(formatMonthYear(selectedMonthYearDate));
        dateList = daysInMonthArray(selectedMonthYearDate);
        eventAdapter = new EventAdapter(dateList, this::onDaySelected);
        calendarRecyclerView.setAdapter(eventAdapter);
        if (isFirstLoad) {
            LocalDate today = LocalDate.now();
            int todayPosition = -1;
            if (today.getMonth() == selectedMonthYearDate.getMonth() && today.getYear() == selectedMonthYearDate.getYear()) {
                todayPosition = today.getDayOfMonth() - 1;
            }
            if (todayPosition >= 0) {
                eventAdapter.setSelectedPosition(todayPosition);
            }
            isFirstLoad = false;
        } else {
            if (selectedDate.getMonth() == selectedMonthYearDate.getMonth() &&
                    selectedDate.getYear() == selectedMonthYearDate.getYear()) {
                int selectedPosition = selectedDate.getDayOfMonth() - 1;
                eventAdapter.setSelectedPosition(selectedPosition);
            }

        }
    }

    // Formats a date in the desired "<Month> <Year>" format
    private String formatMonthYear(LocalDate date) {
        return date.getMonth().name() + " " + date.getYear();
    }

    //Generates an array list of strings containing the days in the selected month
    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonth = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonthValue());
        int daysInMonthValue = yearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonthValue; day++) {
            daysInMonth.add(String.valueOf(day));
        }

        return daysInMonth;
    }

    private void showMonthYearPickerDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_month_year_picker, null);

        NumberPicker monthPicker = dialogView.findViewById(R.id.month_picker);
        NumberPicker yearPicker = dialogView.findViewById(R.id.year_picker);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setDisplayedValues(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"});
        monthPicker.setValue(selectedMonthYearDate.getMonthValue());

        int currentYear = LocalDate.now().getYear();
        yearPicker.setMinValue(currentYear - 50); // Adjust range as needed
        yearPicker.setMaxValue(currentYear + 50);
        yearPicker.setValue(selectedMonthYearDate.getYear());

        new AlertDialog.Builder(this)
                .setTitle("Select Month and Year")
                .setView(dialogView)
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedMonth = monthPicker.getValue();
                    int selectedYear = yearPicker.getValue();
                    selectedMonthYearDate = LocalDate.of(selectedYear, selectedMonth, 1);
                    setMonthView();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onDaySelected(String day) {
        selectedDate = LocalDate.of(selectedMonthYearDate.getYear(), selectedMonthYearDate.getMonth(), Integer.parseInt(day));
        ArrayList<EventListAdapter.EventData> eventsForSelectedDate = dbHelper.getEventsForDate(selectedDate);
        eventListAdapter.setEventList(eventsForSelectedDate);
    }

}

