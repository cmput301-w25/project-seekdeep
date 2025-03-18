package com.example.project_seekdeep;

import android.widget.ImageView;
import android.widget.TextView;

/**
 * This is a wrapper class used to represent comments that are made inside of a mood event.
 * @author Kevin Tu
 */
public class Comment {
    private ImageView commenterPfp;
    private TextView username;
    private TextView comment;

    public Comment(ImageView commenterPfp, TextView username, TextView comment) {
        this.commenterPfp = commenterPfp;
        this.username = username;
        this.comment = comment;
    }

    public ImageView getCommenterPfp() {
        return commenterPfp;
    }

    public void setCommenterPfp(ImageView commenterPfp) {
        this.commenterPfp = commenterPfp;
    }

    public TextView getUsername() {
        return username;
    }

    public void setUsername(TextView username) {
        this.username = username;
    }

    public TextView getComment() {
        return comment;
    }

    public void setComment(TextView comment) {
        this.comment = comment;
    }
}
