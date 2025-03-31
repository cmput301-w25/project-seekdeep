package com.example.project_seekdeep.Helpers;

/**
 * Represents a user's location associated with a specific mood event.
 * This class stores store location data: latitude and longitude in Firestore database
 * @author Saurabh
 */
public class UserLocation {
    private double latitude;
    private double longitude;
    private EmotionalStates emotionalState;
    private String userId;
    private String moodID;

    /**
     * Empty Constructor required for Firebase database operations
     */
    public UserLocation(){

    }
    /**
     * Constructor to create a UserLocation with specifies latitude, longitude and mood event Id
     * @param latitude: The latitude coordinate
     * @param longitude: The longitude coordinate
     */
    public UserLocation(double latitude, double longitude, EmotionalStates emotionalState, String userId, String moodID) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.emotionalState = emotionalState;
        this.userId = userId;
        this.moodID = moodID;
    }

    /**
     * Gets the emotional state of the mood event
     * @return
     */
    public EmotionalStates getEmotionalState() {
        return emotionalState;
    }

    /**
     * Sets the emotional state of the mood event
     * @param emotionalState
     */
    public void setEmotionalState(EmotionalStates emotionalState) {
        this.emotionalState = emotionalState;
    }

    /**
     * Gets the UserId of the owner mood event
     * @return
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gets the latitude coordinate of the location
     * @return
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude coordinate of the location
     * @param latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets the longitude of the location
     * @return
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude coordinate of the location
     * @param longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets the moodEvent ID
     * @return
     */
    public String getMoodID() {
        return moodID;
    }

    /**
     * Sets the moodEvent ID
     * @param moodID
     */
    public void setMoodID(String moodID) {
        this.moodID = moodID;
    }
}
