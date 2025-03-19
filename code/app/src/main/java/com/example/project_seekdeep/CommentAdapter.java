package com.example.project_seekdeep;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
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
     * Required to create a custom ViewHolder to contain the data coming from the comment.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView commenterPfpView;
        private final TextView usernameView;
        private final TextView commentView;
        public ViewHolder(View view) {
            super(view);
            commenterPfpView = (ImageView) view.findViewById(R.id.commenter_pfp);
            usernameView = (TextView) view.findViewById(R.id.username_text_view);
            commentView = (TextView) view.findViewById(R.id.comment_text_view);
        }

        public ImageView getCommenterPfpView() {
            return commenterPfpView;
        }

        public TextView getUsernameView() {
            return usernameView;
        }

        public TextView getCommentView() {
            return commentView;
        }
    }

    public CommentAdapter(List<Comment> moodCommentsList) {
        if (moodCommentsList != null) {
            this.moodCommentsList = (ArrayList<Comment>) moodCommentsList;
        }
    }

    /**
     * Used to specify which layout to use to represent comment data.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return a ViewHolder to represent the data
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // View takes on the layout_comment specified in the layout_comment.xml file.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comment, parent, false);
        return new ViewHolder(view);
    }

    /**
     * This binds the comment data from a given mood to the ViewHolder to display.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        Comment currentComment = moodCommentsList.get(position);
//        holder.getCommenterPfpView().setImageURI(currentComment.getCommenterPfp());
        holder.getUsernameView().setText(currentComment.getUsername());
        holder.getCommentView().setText(currentComment.getComment());
    }

    /**
     * @return The item count in the dataset that was bounded
     */
    @Override
    public int getItemCount() {
        return moodCommentsList.size();
    }
}
