package com.example.eventtracker.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventtracker.R;
import com.example.eventtracker.entity.User;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private ArrayList<String> dateList;
    private OnDaySelectedListener listener;

    private int selectedPosition = RecyclerView.NO_POSITION;
    private User user;


    private EventAdapter() {}

    /**
     * Constructs an EventAdapter with a list of days and a listener for selection.
     *
     * @param dateList - list of dates to present in the calendar
     * @param listener - listener function to invoke when the calendar item is clicked.
     */
    public EventAdapter(User user, ArrayList<String> dateList, OnDaySelectedListener listener) {
        this.user = user;
        this.dateList = dateList;
        this.listener = listener;
    }

    /**
     * Updates the currently selected calendar day and refreshes the visual state.
     */
    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

    /**
     * Inflates the layout for a calendar day cell.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_grid_item, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds the day value to the view and applies selection highlighting.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        String day = dateList.get(position);
        // Set the text to the day number
        holder.eventTextView.setText(day);

        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);

        }

        // Set up the click listener to update selection and notify parent
        holder.itemView.setOnClickListener(v -> handleDaySelection(holder, day));
    }

    private void handleDaySelection(EventViewHolder holder, String day) {
        setSelectedPosition(holder.getAdapterPosition());
        listener.onDaySelected(day);
    }

    /**
     * Returns the number of days in the list.
     */
    @Override
    public int getItemCount() {
        return dateList.size();
    }

    /**
     * ViewHolder class for a single calendar day cell.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTextView = itemView.findViewById(R.id.event_name);
        }
    }

    /**
     * Interface for notifying when a calendar day is selected.
     */
    public interface OnDaySelectedListener {
        void onDaySelected(String day);
    }
}
