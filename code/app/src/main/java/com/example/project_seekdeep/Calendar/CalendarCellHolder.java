package com.example.project_seekdeep.Calendar;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_seekdeep.R;

/**
 * Original code from https://www.youtube.com/watch?v=Ba0Q-cK1fJo,
 * taken on 3/31/2025
 * modified by Nancy
 */
public class CalendarCellHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {

    public final TextView dayOfMonth;
    public TextView moodOfMonth;
    public View cellConstraintLayout;
    public CardView cardView;
    private final CalendarAdapter.OnItemListener onItemListener;
    public CalendarCellHolder(@NonNull View itemView, CalendarAdapter.OnItemListener onItemListener)
    {
        super(itemView);
        dayOfMonth = itemView.findViewById(R.id.cell_date);
        moodOfMonth = itemView.findViewById(R.id.cell_text);
        cellConstraintLayout = itemView.findViewById(R.id.cell_constraint_layout);
        //cardView = itemView.findViewById(R.id.cardView);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        onItemListener.onItemClick( moodOfMonth.getText() );
    }
}
