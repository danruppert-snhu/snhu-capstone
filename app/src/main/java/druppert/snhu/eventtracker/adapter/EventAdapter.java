package druppert.snhu.eventtracker.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import druppert.snhu.eventtracker.R;
import druppert.snhu.eventtracker.entity.User;

import java.util.ArrayList;

/**
 * EventAdapter populates calendar grid cells with selectable day numbers.
 *
 * Implements modular view selection and highlighting
 * Supports interaction callbacks using listener interface
 * Modularized lambdas for readability and testability
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private ArrayList<String> dateList;
    private OnDaySelectedListener listener;

    private int selectedPosition = RecyclerView.NO_POSITION;
    private User user;

    // Private default constructor to enforce custom initialization
    private EventAdapter() {}

    /**
     * Constructs an EventAdapter with a list of days and a listener for selection.
     *
     * @param user     The active user
     * @param dateList List of day labels to display in the calendar
     * @param listener Listener to handle callbacks on date selection
     */
    public EventAdapter(User user, ArrayList<String> dateList, OnDaySelectedListener listener) {
        this.user = user;
        this.dateList = dateList;
        this.listener = listener;
    }

    /**
     * Updates the highlighted day in the grid and refreshes the visual state.
     *
     * @param position Index of the newly selected day
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
        // Disable interaction for padding cells
        if (day.isEmpty()) {
            holder.itemView.setEnabled(false);
            holder.itemView.setClickable(false);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Highlight selected day
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);

        }

        // Set up the click listener to update selection and notify parent
        // CS-499 - Software engineering: modularize lambdas for readability
        holder.itemView.setOnClickListener(v -> handleDaySelection(holder, day));
    }

    // CS-499 - Software engineering: modularize lambdas for readability
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
