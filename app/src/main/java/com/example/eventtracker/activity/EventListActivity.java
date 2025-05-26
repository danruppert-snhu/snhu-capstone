package com.example.eventtracker.activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventtracker.adapter.EventAdapter;
import com.example.eventtracker.adapter.EventListAdapter;
import com.example.eventtracker.R;
import com.example.eventtracker.db.DatabaseHelper;
import com.example.eventtracker.entity.User;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.eventtracker.utils.Constants;

public class EventListActivity extends AppCompatActivity {

    //CS-499 - Algorithms: LRU cache implementation. Reduces database queries and consequently reduces slow disk I/O requests
    //CS-499 - Algorithms: Oldest events will be pushed out of the cache.
    private final LinkedHashMap<Integer, ArrayList<EventListAdapter.EventData>> eventCache =
            new LinkedHashMap<Integer, ArrayList<EventListAdapter.EventData>>(Constants.MAX_CACHE_SIZE, Constants.EVENT_CACHE_LOAD_FACTOR, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<Integer, ArrayList<EventListAdapter.EventData>> oldest) {
                    return size() > Constants.MAX_CACHE_SIZE;
                }
            };

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
    private ToggleButton toggleEventViewButton;

    private DatabaseHelper dbHelper;

    private User user;

    /**
     * Initializes the Event List screen, including the calendar view, event list,
     * month navigation, and add event functionality.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);
        // Initialize database helper to query event data
        dbHelper = new DatabaseHelper(this);
        //CS-499 - Software Engineering: Persist user data across intents
        user = getIntent().getSerializableExtra(Constants.USER_INTENT_KEY, User.class);


        //Get the calendar header text field (non-editable)
        monthYearText = findViewById(R.id.month_year_text);
        monthYearText.setOnClickListener(v -> showMonthYearPickerDialog());

        // Configure navigation buttons for month scrolling
        previousMonthButton = findViewById(R.id.button_previous_month);
        previousMonthButton.setOnClickListener(v -> navigateToPreviousMonth());

        nextMonthButton = findViewById(R.id.button_next_month);
        nextMonthButton.setOnClickListener(v -> navigateToNextMonth());


        // Set up the calendar view as a grid (7 columns for days of the week)
        calendarRecyclerView = findViewById(R.id.calendar_recycler_view);
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, Constants.DAYS_IN_WEEK));

        // Set up the event list view as a vertical list (one item per event)
        eventListRecyclerView = findViewById(R.id.event_list_recycler);
        eventListRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //The label at the top of the Event List screen. For example, "October 2024"
        // This will ultimately have navigation capabilities to choose other months / years
        // It defaults to the current month.
        selectedMonthYearDate = LocalDate.now();
        selectedDate = LocalDate.now();
        setMonthView();

        //CS-499 - Algorithms: Implement a priority queue to sort events by date.
        toggleEventViewButton = findViewById(R.id.toggle_event_view);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.EVENT_DATE_PATTERN);
        toggleEventViewButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked) {
                toggleEventViewButton.setText("Upcoming Events");
                toggleEventViewButton.setTextOff("Upcoming Events");
                loadUpcomingEvents();
                user.setViewingUpcomingEvents(true);
            } else {
                String dateText = "Events for " + selectedDate.format(formatter);
                toggleEventViewButton.setTextOn(dateText);
                toggleEventViewButton.setText(dateText);
                loadSelectedDateEvents(selectedDate);
                user.setViewingUpcomingEvents(false);
            }
        });

        // Load and bind events for today by default
        ArrayList<EventListAdapter.EventData> eventList = (user.getViewingUpcomingEvents() ? dbHelper.getUpcomingEvents(user, Constants.MAX_EVENTS_TO_SHOW) : dbHelper.getEventsForDate(user, selectedDate));


        //CS-499 - Software Engineering: persist user session across intents / method invocations
        eventListAdapter = new EventListAdapter(user, eventList, dbHelper);
        eventListRecyclerView.setAdapter(eventListAdapter);

        // Set up the Add Event button to open the AddEventActivity screen
        addEventButton = findViewById(R.id.add_event_button);
        //CS-499 - Software engineering: Modularize lambda definitions for improved readability
        addEventButton.setOnClickListener(v -> launchAddEventIntent());
    }

    //CS-499: Algorithms: Implement a priority queue to sort events by date.
    private void loadSelectedDateEvents(LocalDate selectedDate) {
        ArrayList<EventListAdapter.EventData> events = dbHelper.getEventsForDate(user, selectedDate);
        eventListAdapter.setEventList(events);
        //Reset scroll position to top of list.
        eventListRecyclerView.scrollToPosition(0);
    }

    //CS-499: Algorithms: Implement a priority queue to sort events by date.
    private void loadUpcomingEvents() {
        ArrayList<EventListAdapter.EventData> upcoming = dbHelper.getUpcomingEvents(user, Constants.MAX_EVENTS_TO_SHOW);
        eventListAdapter.setEventList(upcoming);
        //Reset scroll position to top of list.
        eventListRecyclerView.scrollToPosition(0);
    }



    /**
     * Show the app bar menu (containing the wrench/settings icon).
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.event_list_menu, menu);
        return true;
    }

    /**
     * Handle menu item selection (currently only opens the SMS notification settings screen).
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {


            Intent intent = new Intent(EventListActivity.this, SMSNotificationActivity.class);
            intent.putExtra(Constants.USER_INTENT_KEY, user);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //CS-499 - Software engineering: Modularize lambda invocations for human readability.
    private void launchAddEventIntent() {
        Intent intent = new Intent(EventListActivity.this, AddEventActivity.class);
        intent.putExtra(Constants.USER_INTENT_KEY, user);
        startActivity(intent);
    }

    /**
     * Navigate to the previous month and update the calendar view.
     */
    private void navigateToPreviousMonth() {
        selectedMonthYearDate = selectedMonthYearDate.minusMonths(1);
        setMonthView();
        eventCache.clear();
    }


    /**
     * Navigate to the next month and update the calendar view.
     */
    private void navigateToNextMonth() {
        selectedMonthYearDate = selectedMonthYearDate.plusMonths(1);
        setMonthView();
        eventCache.clear();
    }

    /**
     * Rebuild the calendar view based on the currently selected month/year.
     */
    private void setMonthView() {
        // Display the selected month and year
        monthYearText.setText(formatMonthYear(selectedMonthYearDate));
        // Generate day labels for the calendar grid
        dateList = daysInMonthArray(selectedMonthYearDate);
        // Bind calendar data to the adapter and highlight the selected date
        eventAdapter = new EventAdapter(user, dateList, this::onDaySelected);
        calendarRecyclerView.setAdapter(eventAdapter);
        if (!isFirstLoad) {
            // If a date was previously selected, re-select it if it belongs to the current month
            if (selectedDate.getMonth() == selectedMonthYearDate.getMonth() &&
                    selectedDate.getYear() == selectedMonthYearDate.getYear()) {
                int selectedPosition = selectedDate.getDayOfMonth() - 1;
                eventAdapter.setSelectedPosition(selectedPosition);
            }
        } else {
            // On first load, highlight today's date if it's within the selected month
            LocalDate today = LocalDate.now();
            int todayPosition = -1;
            if (today.getMonth() == selectedMonthYearDate.getMonth() && today.getYear() == selectedMonthYearDate.getYear()) {
                todayPosition = today.getDayOfMonth() - 1;
            }
            if (todayPosition >= 0) {
                eventAdapter.setSelectedPosition(todayPosition);
            }
            isFirstLoad = false;
        }
    }

    /**
     * Format a LocalDate as "MONTH YEAR" (e.g., "OCTOBER 2024").
     */
    private String formatMonthYear(LocalDate date) {
        return date.getMonth().name() + " " + date.getYear();
    }

    /**
     * Generate a list of all days in the selected month.
     */
    private ArrayList<String> daysInMonthArray(LocalDate date) {
        ArrayList<String> daysInMonth = new ArrayList<>();
        YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonthValue());
        int daysInMonthValue = yearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonthValue; day++) {
            daysInMonth.add(String.valueOf(day));
        }

        return daysInMonth;
    }

    /**
     * Display a dialog for selecting a custom month and year.
     */
    private void showMonthYearPickerDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_month_year_picker, null);

        NumberPicker monthPicker = dialogView.findViewById(R.id.month_picker);
        NumberPicker yearPicker = dialogView.findViewById(R.id.year_picker);

        // Set up month picker (1–12, Jan–Dec)
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setDisplayedValues(Constants.MONTHS_IN_YEAR);
        monthPicker.setValue(selectedMonthYearDate.getMonthValue());

        // Set up year picker (+/-50 years from current year)
        int currentYear = LocalDate.now().getYear();
        yearPicker.setMinValue(currentYear - Constants.YEAR_OFFSET); // Adjust range as needed
        yearPicker.setMaxValue(currentYear + Constants.YEAR_OFFSET);
        yearPicker.setValue(selectedMonthYearDate.getYear());

        // Show the dialog and apply selected month/year on confirmation
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

    /**
     * Callback for when a calendar day is selected.
     * Updates the event list to show events for the selected date.
     */
    private void onDaySelected(String day) {
        selectedDate = LocalDate.of(selectedMonthYearDate.getYear(), selectedMonthYearDate.getMonth(), Integer.parseInt(day));
        //CS-499 - Algorithms: implement a priority queue
        if (toggleEventViewButton.isChecked()) {
            int epochDay = getEpochDay(selectedDate);
            ArrayList<EventListAdapter.EventData> eventsForSelectedDate = eventCache.get(epochDay);

            if (eventsForSelectedDate == null) {
                // Cache miss: Load from DB
                eventsForSelectedDate = dbHelper.getEventsForDate(user, selectedDate);
                eventCache.put(epochDay, eventsForSelectedDate);
            }
            eventListAdapter.setEventList(eventsForSelectedDate);
        }
    }

    //CS-499 - Algorithms: implement a priority queue
    private int getEpochDay(LocalDate date) {
        return (int) date.toEpochDay();  // Suitable for SparseArray keys
    }


}

