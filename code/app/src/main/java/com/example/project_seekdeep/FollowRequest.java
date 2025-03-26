package com.example.project_seekdeep;

import com.google.firebase.firestore.DocumentReference;

public class FollowRequest {
    private DocumentReference docRef;
    private String follower;
    private String followee;
    private String status;

    /**
     * Constructor for FollowRequest. When a new FollowRequest is made, the status is always initialized as "pending"
     * @param follower The user that sends a follow request
     * @param followee The user that receives a follow request
     */
    public FollowRequest(String follower, String followee) {
        this.follower = follower;
        this.followee = followee;
        this.status = "pending";
    }
    public DocumentReference getDocRef() {
        return docRef;
    }

    public void setDocRef(DocumentReference docRef) {
        this.docRef = docRef;
    }
    public String getFollower() {
        return follower;
    }

    public void setFollower(String follower) {
        this.follower = follower;
    }

    public String getFollowee() {
        return followee;
    }

    public void setFollowee(String followee) {
        this.followee = followee;
    }

    public String getStatus() {
        return status;
    }
    public void setStatusToFollowing() {
        this.status = "following";
    }

//    /**
//     * Sets the status
//     * @param status
//     */
//    public void setStatus(String status) {
//        if ("pending".equals(status) || "following".equals(status)) {
//            this.status = status;
//        }
//        else {
//            throw new IllegalArgumentException("invalid status!! Can only set as 'pending' or 'following'");
//        }
//    }

}
