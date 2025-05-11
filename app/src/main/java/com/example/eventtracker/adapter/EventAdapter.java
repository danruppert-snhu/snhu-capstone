package com.example.eventtracker.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventtracker.R;

import java.util.ArrayList;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private ArrayList<String> dateList;
    private OnDaySelectedListener listener;

    private int selectedPosition = RecyclerView.NO_POSITION;


    /**
     @param dateList - list of dates to present in the calendar
     @param listener - listener function to invoke when the calendar item is clicked.
     */
    public EventAdapter(ArrayList<String> dateList, OnDaySelectedListener listener) {
        this.dateList = dateList;
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_grid_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        String day = dateList.get(position);
        holder.eventTextView.setText(day);

        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);

        }

        holder.itemView.setOnClickListener(v -> {
            setSelectedPosition(holder.getAdapterPosition());
            listener.onDaySelected(day);
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventTextView = itemView.findViewById(R.id.event_name);
        }
    }

    public interface OnDaySelectedListener {
        void onDaySelected(String day);
    }
}
