package com.example.project_seekdeep;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.Calendar;

/**
 * Original code from https://www.youtube.com/watch?v=Ba0Q-cK1fJo,
 * taken on 3/31/2025
 * modified by Nancy
 */
public class CalendarCellHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {

    public final TextView dayOfMonth;
    public TextView moodOfMonth;
    public View cellConstraintLayout;
    private final CalendarAdapter.OnItemListener onItemListener;
    public CalendarCellHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener)
    {
        super(itemView);
        Log.d("NANCY", "Calendar cell holder month construct" );
        dayOfMonth = itemView.findViewById(R.id.cell_date);
        moodOfMonth = itemView.findViewById(R.id.cell_text);
        cellConstraintLayout = itemView.findViewById(R.id.cell_constraint_layout);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        onItemListener.onItemClick(getAdapterPosition(), (String) dayOfMonth.getText());
    }
}
