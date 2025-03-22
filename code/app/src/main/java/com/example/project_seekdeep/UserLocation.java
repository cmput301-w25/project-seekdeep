package com.example.project_seekdeep;

/**
 * Represents a user's location associated with a specific mood event.
 * This class stores store location data: latitude and longitude in Firestore database
 */
public class UserLocation {
    private double latitude;
    private double longitude;
    private String moodEventId; // To associate with a mood event

    /**
     * Default Constructor for database
     */
    public UserLocation() {

    }

    /**
     * Constructor to create a UserLocation with specifies latitude, longitude and mood event Id
     * @param latitude: The latitude coordinate
     * @param longitude: The longitude coordinate
     * @param moodEventId: The ID of the mood event
     */
    public UserLocation(double latitude, double longitude, String moodEventId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.moodEventId = moodEventId;
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
     * Gets the mood event ID
     * @return
     */
    public String getMoodEventId() {
        return moodEventId;
    }

    /**
     * Sets the mood event ID
     * @param moodEventId
     */
    public void setMoodEventId(String moodEventId) {
        this.moodEventId = moodEventId;
    }
}
