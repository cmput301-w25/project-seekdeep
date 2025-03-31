package com.example.project_seekdeep;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;

/**
 * Original code from https://www.youtube.com/watch?v=Ba0Q-cK1fJo,
 * taken on 3/31/2025
 * modified by Nancy
 */
public class CalendarCellHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {

    public final TextView dayOfMonth;
    private final CalendarAdapter.OnItemListener onItemListener;
    public CalendarCellHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener)
    {
        super(itemView);
        Log.d("NANCY", "Calendar cell holder month construct" );
        dayOfMonth = itemView.findViewById(R.id.cell_date);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        onItemListener.onItemClick(getAdapterPosition(), (String) dayOfMonth.getText());
    }
}
