package com.example.project_seekdeep;


import android.net.Uri;

/**
 * This is a wrapper class used to represent comments that are made inside of a mood event.
 * @author Kevin Tu
 */
public class Comment {
    private Uri commenterPfp;
    private String username;
    private String comment;

    public Comment(Uri commenterPfp, String username, String comment) {
        this.commenterPfp = commenterPfp;
        this.username = username;
        this.comment = comment;
    }

    public Uri getCommenterPfp() {
        return commenterPfp;
    }

    public void setCommenterPfp(Uri commenterPfp) {
        this.commenterPfp = commenterPfp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
