package com.example.project_seekdeep;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * Original code from https://www.youtube.com/watch?v=Ba0Q-cK1fJo,
 * taken on 3/31/2025
 * modified by Nancy
 */
public class CalendarAdapter extends RecyclerView.Adapter<CalendarCellHolder> {

    private ArrayList<String> daysOfMonth;
    private ArrayList<String> moodsInMonth;
    private OnItemListener onItemListener;

    public CalendarAdapter(ArrayList<String> daysOfMonth, ArrayList<String> moodsInMonth, OnItemListener onItemListener){
        Log.d("NANCY", "Calendar cell adapter " );
        this.daysOfMonth = daysOfMonth;
        this.moodsInMonth = moodsInMonth;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarCellHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarCellHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarCellHolder holder, int position) {
        holder.dayOfMonth.setText(daysOfMonth.get(position));
        holder.moodOfMonth.setText(moodsInMonth.get(position));
    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public interface  OnItemListener{
        void onItemClick(int position, String dayText);
    }
}
