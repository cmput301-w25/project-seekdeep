package com.example.project_seekdeep;

import java.util.ArrayList;

/**
 * This class is for filtering mood events across screens.
 * @author Jachelle Chan
 */
public class MoodFiltering {
    /**
     * This method sorts an ArrayList of moods in reverse chronological order
     * @param moods
     *      An ArrayList of Mood objects
     * @return
     *      The ArrayList sorted in reverse chronological order
     */
    public static ArrayList<Mood> sortReverseChronological(ArrayList<Mood> moods) {
        moods.sort((mood1, mood2) -> mood2.getPostedDate().compareTo(mood1.getPostedDate()));
        return moods;
    }
}
