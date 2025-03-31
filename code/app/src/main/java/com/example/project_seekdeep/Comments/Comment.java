package com.example.project_seekdeep.Comments;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;
import java.util.Date;

/**
 * This is a wrapper class used to represent comments that are made inside of a mood event.
 * @author Kevin Tu
 */
public class Comment implements Serializable, Comparable<Comment>{
//    private Uri commenterPfp; Currently no support for pfp for user profiles
    DocumentReference mood;
    private String username;
    private String comment;
    private Date date;

    public Comment(DocumentReference mood, String username, String comment) {
        this.mood = mood;
        this.username = username;
        this.comment = comment;
        this.date = Timestamp.now().toDate();
    }

    public Comment(DocumentReference mood, String username, String comment, Date date) {
        this.mood = mood;
        this.username = username;
        this.comment = comment;
        this.date = date;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    // For sorting
    // See: https://medium.com/@thecodebean/java-object-sorting-explained-using-comparable-and-comparator-03b93b988f75
    // For more information on sorting.
    // Kevin Tu (2025-03-27)
    @Override
    public int compareTo(Comment o) {
        Date otherDate = o.getDate();
        return this.date.compareTo(otherDate);
    }
}
