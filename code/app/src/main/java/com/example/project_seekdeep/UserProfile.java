package com.example.project_seekdeep;

import java.io.Serializable;

/**
 * This is a class to hold user information (pulled from 4-us-030101 on 3/7/25 ~10pm).
 * @author Saurabh Singh Baghel
 */
public class UserProfile implements Serializable {
    private String username;
    private String password;

    /**
     * Default empty constructor required by Firestore for deserialization.
     */
    public UserProfile() {
    }

    /**
     * Constructor to create a UserProfile instance
     * @param username : The username of the user.
     * @param password : The password of the user.
     */
    public UserProfile(String username, String password) {
        this.username = username;
        this.password = password;
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
}
