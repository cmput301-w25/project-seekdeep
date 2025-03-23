package com.example.project_seekdeep;

/**
 * Represents a user's location associated with a specific mood event.
 * This class stores store location data: latitude and longitude in Firestore database
 */
public class UserLocation {
    private double latitude;
    private double longitude;
    private EmotionalStates emotionalState;
    private String userId;

    /**
     * Default Constructor for database
     */
    public UserLocation() {

    }

    /**
     * Constructor to create a UserLocation with specifies latitude, longitude and mood event Id
     * @param latitude: The latitude coordinate
     * @param longitude: The longitude coordinate
     */
    public UserLocation(double latitude, double longitude, EmotionalStates emotionalState, String userId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.emotionalState = emotionalState;
        this.userId = userId;
    }

    public EmotionalStates getEmotionalState() {
        return emotionalState;
    }

    public void setEmotionalState(EmotionalStates emotionalState) {
        this.emotionalState = emotionalState;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
}
