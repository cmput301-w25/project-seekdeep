package com.example.project_seekdeep;


import android.net.Uri;

import com.google.firebase.firestore.DocumentReference;

import org.w3c.dom.Document;

import java.io.Serializable;

/**
 * This is a wrapper class used to represent comments that are made inside of a mood event.
 * @author Kevin Tu
 */
public class Comment implements Serializable {
//    private Uri commenterPfp; Currently no support for pfp for user profiles
    DocumentReference mood;
    private String username;
    private String comment;

    public Comment(DocumentReference mood, String username, String comment) {
        this.mood = mood;
        this.username = username;
        this.comment = comment;
    }


//    public Uri getCommenterPfp() {
//        return commenterPfp;
//    }
//
//    public void setCommenterPfp(Uri commenterPfp) {
//        this.commenterPfp = commenterPfp;
//    }

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
