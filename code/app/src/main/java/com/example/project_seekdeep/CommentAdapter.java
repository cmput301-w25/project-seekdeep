package com.example.project_seekdeep;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to wrap comments inside of a RecyclerView which is childed inside of a
 * nestedScrollView.
 * Suggestion by Vesko: https://stackoverflow.com/questions/32881222/listview-not-expanding-inside-nestedscrollview
 * Other references: https://developer.android.com/develop/ui/views/layout/recyclerview
 * @author Kevin Tu, 2025-03-18
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private ArrayList<Comment> moodCommentsList;
    /**
     * Required to create a custom ViewHolder to contain the data coming from the comments.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView commenterPfp;
        private final TextView username;
        private final TextView comment;
        public ViewHolder(View view) {
            super(view);
            commenterPfp = (ImageView) view.findViewById(R.id.commenter_pfp);
            username = (TextView) view.findViewById(R.id.username_text_view);
            comment = (TextView) view.findViewById(R.id.comment_text_view);
        }

        public ImageView getCommenterPfp() {
            return commenterPfp;
        }

        public TextView getUsername() {
            return username;
        }

        public TextView getComment() {
            return comment;
        }
    }

    public CommentAdapter(List<Comment> moodCommentsList) {
        if (moodCommentsList != null) {
            this.moodCommentsList = (ArrayList<Comment>) moodCommentsList;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
