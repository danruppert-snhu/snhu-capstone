package com.example.eventtracker.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventtracker.R;
import com.example.eventtracker.db.DatabaseHelper;
import com.example.eventtracker.entity.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventListViewHolder> {

    /**
     * Represents the structure of a single event row.
     */
    public static class EventData {
        String eventName;
        String eventDate;
        int userId;
        long eventId;
        long eventEpoch;
        long eventParentId;
        boolean isParent;

        //Intentionally restrict access to this constructor
        private EventData() {}
        // CS-499 - Software engineering: Support user-specific features, recurring events
        // CS-499 - Databases: Support recurring events
        // CS-499 - Data Structures and Algorithms: Design algorithms to sort maps effectively and efficiently.
        public EventData(int userId, String eventName, String eventDate, long eventId, long eventEpoch, long eventParentId, boolean isParent) {
            this.userId = userId;
            this.eventName = eventName;
            this.eventDate = eventDate;
            this.eventId = eventId;
            this.eventEpoch = eventEpoch;
            this.eventParentId = eventParentId;
            this.isParent = isParent;
        }
        // CS-499 - Data Structures and Algorithms: Design algorithms to sort maps effectively and efficiently.
        public long getEventEpoch() {
            return this.eventEpoch;
        }
    }

    private List<EventData> eventList;

    private DatabaseHelper dbHelper;

    private int focusedEventPosition = RecyclerView.NO_POSITION;

    private User user;

    /**
     * Constructor for initializing the adapter with data and DB reference.
     * CS-499 - Software engineering: Support user-specific features, recurring events
     */
    public EventListAdapter(User user, ArrayList<EventData> eventList, DatabaseHelper dbHelper) {
        this.user = user;
        this.eventList = eventList;
        this.dbHelper = dbHelper;
    }

    /**
     * Updates the event list and refreshes the RecyclerView.
     */
    public void setEventList(ArrayList<EventData> eventList) {
        this.eventList = eventList;
        notifyDataSetChanged(); // Refresh the adapter
    }

    /**
     * Inflates the layout for each list item.
     */
    @NonNull
    @Override
    public EventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);
        return new EventListViewHolder(view);
    }

    /**
     * Binds data to the list item and manages event interactions.
     */
    @Override
    public void onBindViewHolder(@NonNull EventListViewHolder holder, int position) {
        EventData event = eventList.get(position);
        holder.eventNameTextView.setText(event.eventName);
        holder.eventDateTextView.setText(event.eventDate);

        // Show/hide delete button based on selection
        if (position == focusedEventPosition) {
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.INVISIBLE);
        }

        // Handle row selection to show/hide delete button
        // CS-499 - Software engineering: modularize lambdas for readability
        holder.itemView.setOnClickListener(v -> handleRowSelection(holder));
        // Handle delete button click
        holder.deleteButton.setOnClickListener(v -> deleteEvent(v, position));
    }

    // CS-499 - Software engineering: modularize lambdas for readability
    private void handleRowSelection(EventListViewHolder holder) {
        int previousFocusedPosition = focusedEventPosition;
        focusedEventPosition = holder.getAdapterPosition();

        if (previousFocusedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousFocusedPosition);
        }

        // Refresh the new selected item
        notifyItemChanged(focusedEventPosition);
        // Refresh previously focused item to hide its delete button
        if (previousFocusedPosition == focusedEventPosition) {
            focusedEventPosition = RecyclerView.NO_POSITION;
            holder.deleteButton.setVisibility(View.INVISIBLE);
        } else {
            holder.deleteButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Deletes an event from the database and updates the UI.
     * CS-499 - software engineering: Support recurring events
     */
    private void deleteEvent(View view, int eventPosition) {
        EventData eventData = eventList.get(eventPosition);
        boolean series = eventData.isParent || eventData.eventParentId > 0;
        if (series) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("Delete Recurring Event")
                    .setMessage("Do you want to delete this event only or the entire series?")
                    .setPositiveButton("Entire Series", (dialog, which) -> {
                        deleteSeries(eventData, view, eventPosition);
                    })
                    .setNegativeButton("This Event Only", (dialog, which) -> {
                        deleteSingleEvent(eventData, view, eventPosition);
                    })
                    .setNeutralButton("Cancel", null)
                    .show();
        } else {
            deleteSingleEvent(eventData, view, eventPosition);
        }
    }
    // CS-499 - software engineering: Support recurring events
    private void deleteSingleEvent(EventData eventData, View view, int eventPosition) {
        boolean deleted = dbHelper.deleteEvent(user, eventData.eventId);
        if (deleted) {
            eventList.remove(eventPosition);
            notifyItemRemoved(eventPosition);
            notifyItemRangeChanged(eventPosition, eventList.size());
            Toast.makeText(view.getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(view.getContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
        }
    }

    // CS-499 - software engineering: Support recurring events
    // CS-499 - Algorithms: support recurring events
    private void deleteSeries(EventData eventData, View view, int eventPosition) {
        long parentId = eventData.isParent ? eventData.eventId : eventData.eventParentId;
        boolean deleted = dbHelper.deleteEventSeries(parentId);
        if (deleted) {
            // Remove all items in the adapter that were part of this series
            Iterator<EventData> iterator = eventList.iterator();
            int index = 0;
            while (iterator.hasNext()) {
                EventData e = iterator.next();
                if (e.eventId == parentId || e.eventParentId == parentId) {
                    iterator.remove();
                    notifyItemRemoved(index);
                    notifyItemRangeChanged(index, eventList.size());
                } else {
                    index++;
                }
            }
            Toast.makeText(view.getContext(), "Events deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(view.getContext(), "Failed to delete event series", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Returns the number of events in the list.
     */
    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * ViewHolder class to cache references to views for each event row.
     */
    public static class EventListViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;
        TextView eventDateTextView;
        Button deleteButton;

        public EventListViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.event_name);
            eventDateTextView = itemView.findViewById(R.id.event_date);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }
}
