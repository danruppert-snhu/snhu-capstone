package com.example.eventtracker.adapter;

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

import java.util.ArrayList;
import java.util.List;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventListViewHolder> {

    // Create a data class to store event details
    public static class EventData {
        String eventName;
        String eventDate;

        long eventId;

        public EventData(String eventName, String eventDate, long eventId) {
            this.eventName = eventName;
            this.eventDate = eventDate;
            this.eventId = eventId;
        }
    }

    private List<EventData> eventList;

    private DatabaseHelper dbHelper;

    private int focusedEventPosition = RecyclerView.NO_POSITION;

    public EventListAdapter(ArrayList<EventData> eventList, DatabaseHelper dbHelper) {
        this.eventList = eventList;
        this.dbHelper = dbHelper;
    }

    public void setEventList(ArrayList<EventData> eventList) {
        this.eventList = eventList;
        notifyDataSetChanged(); // Refresh the adapter
    }

    @NonNull
    @Override
    public EventListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_list_item, parent, false);
        return new EventListViewHolder(view);
    }

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

        // Handle event selection
        holder.itemView.setOnClickListener(v -> {
            int previousFocusedPosition = focusedEventPosition;
            focusedEventPosition = holder.getAdapterPosition();
            if (previousFocusedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousFocusedPosition);
            }
            notifyItemChanged(focusedEventPosition);
            if (previousFocusedPosition == focusedEventPosition) {
                focusedEventPosition = RecyclerView.NO_POSITION;
                holder.deleteButton.setVisibility(View.INVISIBLE);
            } else {
                holder.deleteButton.setVisibility(View.VISIBLE);
            }
        });

        holder.deleteButton.setOnClickListener(v -> deleteEvent(v, position));
    }

    private void deleteEvent(View view, int eventPosition) {
        EventData eventData = eventList.get(eventPosition);
        long eventId = eventData.eventId;
        boolean deleted = dbHelper.deleteEvent(eventId);
        if (deleted) {
            eventList.remove(eventPosition);
            notifyItemRemoved(eventPosition);
            notifyItemRangeChanged(eventPosition, eventList.size());
            Toast.makeText(view.getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(view.getContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public int getItemCount() {
        return eventList.size();
    }

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
