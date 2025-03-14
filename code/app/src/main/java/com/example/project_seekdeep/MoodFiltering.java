package com.example.project_seekdeep;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

    public static ArrayList<Mood> sortRecentWeek(ArrayList<Mood> moods) {
        Calendar calendar = Calendar.getInstance();
        // go back 7 days to get recent week
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date recentWeek = calendar.getTime();

        // remove the mood from the moods arraylist if it happened before the recent week
        moods.removeIf(mood -> mood.getPostedDate().before(recentWeek));
        return moods;
    }
}
