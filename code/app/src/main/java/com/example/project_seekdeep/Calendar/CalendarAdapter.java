package com.example.project_seekdeep.Calendar;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project_seekdeep.R;

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

        //change row height depending on how many rows the month occupies
        if (daysOfMonth.size()< 29){
            layoutParams.height = (int) (parent.getHeight() * 0.25);
        } else if (daysOfMonth.size() < 36){
            layoutParams.height = (int) (parent.getHeight() * 0.2);
        } else {
            layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        }
        return new CalendarCellHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarCellHolder holder, int position) {

        //set date and emoticon to the cell
        holder.dayOfMonth.setText(daysOfMonth.get(position));
        holder.moodOfMonth.setText(moodsInMonth.get(position));
        //holder.cellConstraintLayout.setBackground( cellColor( moodsInMonth.get(position)));
        //holder.cardView.setBackgroundColor(cellColor( moodsInMonth.get(position)));


        //set colors to calendar cell
        GradientDrawable box_outline = (GradientDrawable) holder.cellConstraintLayout.getBackground();
        box_outline.mutate();
        //box_outline.setColor(cellColor( moodsInMonth.get(position)));
        box_outline.setStroke(5, cellColor( moodsInMonth.get(position)));


    }

    @Override
    public int getItemCount() {
        return daysOfMonth.size();
    }

    public interface  OnItemListener{
        void onItemClick(CharSequence charSequence);
    }

    public int cellColor (String emoticon){
        int color = 0;
        switch (emoticon){
            //anger
            case "\uD83D\uDE20":
                color = Color.parseColor("#eebaba");
                break;

            //confusion
            case "\uD83E\uDD14":
                color = Color.parseColor("#ffe8f6");
                break;

            //disgust
            case "\uD83E\uDD22":
                color = Color.parseColor("#baeda1");
                break;

                //fear
            case "\uD83D\uDE28":
                color = Color.parseColor("#cdcdcd");
                break;

                //happiness
            case "\uD83D\uDE04":
                color = Color.parseColor("#fff78f");
                break;

                //sadness
            case "☹️":
                color = Color.parseColor("#99bbff");
                break;

                //shame
            case "\uD83D\uDE14":
                color = Color.parseColor("#deabf8");
                break;

                //surprise
            case "\uD83D\uDE2F":
                color = Color.parseColor("#f4daa5");
                break;

            default:
                color = Color.parseColor("#FFFFFF");


        }
        return color;
    }
}
