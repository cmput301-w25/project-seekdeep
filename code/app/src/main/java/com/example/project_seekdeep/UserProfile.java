package com.example.project_seekdeep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is a class to hold user information (pulled from 4-us-030101 on 3/7/25 ~10pm).
 * @author Saurabh Singh Baghel
 */
public class UserProfile implements Serializable {
    private String username;
    private String password;
    private List<String> followings;

    /**
     * Default empty constructor required by Firestore for deserialization.
     */
    public UserProfile() {
        this.followings = new ArrayList<>(); //initialize followers as empty list to avoid null issues
    }

    /**
     * Constructor to create a UserProfile instance
     * @param username : The username of the user.
     * @param password : The password of the user.
     */
    public UserProfile(String username, String password) {
        this.username = username;
        this.password = password;
        this.followings = new ArrayList<>();
    }

    /**
     * Constructor to be used in mapsFragment
     * @param username
     */
    public UserProfile(String username) {
        this.username = username;
        this.password = null;
        this.followings = new ArrayList<>();
    }

    /**
     * Gets the username of the user.
     * @return : The username of the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username for the user.
     * @param username : The username to set for the user.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password of the user.
     * @return : The password of the user.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password for the user.
     * @param password : The password to set for the user.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getFollowings() {
        return followings;
    }

    public void setFollowings(List<String> followings) {
        this.followings = followings;
    }
    public void addFollowing(String followerUsername) {
        if  (!followings.contains(followerUsername)) {
            followings.add(followerUsername);
        }
    }
    public void removeFollowing(String followerUsername) {
        followings.remove(followerUsername);
    }
}
