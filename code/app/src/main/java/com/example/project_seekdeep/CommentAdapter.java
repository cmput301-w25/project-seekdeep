package com.example.project_seekdeep;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * This class is used to wrap comments inside of a RecyclerView which is childed inside of a
 * nestedScrollView.
 * Suggestion by Vesko: https://stackoverflow.com/questions/32881222/listview-not-expanding-inside-nestedscrollview
 * Other references: https://developer.android.com/develop/ui/views/layout/recyclerview
 * @author Kevin Tu, 2025-03-18
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
