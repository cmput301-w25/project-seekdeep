package com.example.project_seekdeep;

import com.google.firebase.firestore.DocumentReference;

/**
 * This is a class that gets instantiated whenever a new follow request is made between two users.
 * It will then be used to store into firebase.
 * </p>
 * It stores:
 *      <pre>
 *      follower: user that sent the request
 *      followee: user that receives the request
 *      status: either "pending" or "following"
 *      </pre>
 * @see OtherUsersProfileFragment The only fragment that creates FollowRequest objects
 * @author Sarah Chang
 */
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

}
