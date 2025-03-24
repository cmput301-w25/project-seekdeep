package com.example.project_seekdeep;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is for filtering mood events across screens.
 * When using this class, use saveOriginal method first (after sorting reverse chronologically)
 * <p>
 * How to use: <p>
 *   <pre>
 *   ArrayList<Mood> moods
 *   MoodFiltering.sortReverseChronological(moods);
 *   MoodFiltering.saveOriginal(moods);
 *   MoodFiltering.applyFilter("recent");
 *   MoodFilter.addStates(ArrayList of EmotionalStates);
 *   MoodFiltering.applyFilter("states");
 *   ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
 *   Then use filteredMoods for displaying
 *   </pre>
 * To remove a filter: <p>
 *     <pre>
 *    MoodFiltering.removeFilter("rChronological");
 *    filteredMoods = MoodFiltering.getFilteredMoods();
 *    </pre>
 * Filters available: <p>
 * <pre>
 *     rChronological: Sort reverse chronologically
 *     recent: Sort by recent week
 * </pre>
 * @author Jachelle Chan
 */
public class MoodFiltering {
    private static ArrayList<Mood> originalMoods; // og list of moods
    private static Set<String> filters = new HashSet<>(); // the filters applied

    private static Set<EmotionalStates> selectedStates = new HashSet<>(); // the selected emotional state(s) to filter by

    /**
     * This method saves a copy of the original array
     * You must use this before applying any filters to preserve the original array
     * @param moods: An ArrayList of Mood objects
     */
    public static void saveOriginal(ArrayList<Mood> moods) {
        originalMoods = new ArrayList<>(moods);
    }

    /**
     * This method puts the filter name into a set
     * <p><STRONG>Please note that you must use the addStates method before using applyFilter("states") </STRONG></p>
     * @param filterName: The filter applied <p>
     *                    Filter names include: "rChronological", "recent", "states"
     */
    public static void applyFilter(String filterName) {
        filters.add(filterName);
    }

    /**
     * This method removes the filter name from a set
     * @param filterName: The filter to be removed
     */
    public static void removeFilter(String filterName) {
        filters.remove(filterName);
    }

    /**
     * This method MUST be used before using applyFilter("states")
     * @param states
     */

    public static void addStates(ArrayList<EmotionalStates> states) {
        selectedStates.clear();
        selectedStates.addAll(states);
    }

    /**
     * This method removes all filters that were selected by the user.
     */
    public static void removeAllFilters() {
        // reverse chronological should NOT be removed ever
        filters.remove("recent");
        filters.remove("states");
    }
    /**
     * This method gets the moods that matches the filters that were applied
     * @return
     *      An ArrayList with filtered moods
     */
    public static ArrayList<Mood> getFilteredMoods() {
        ArrayList<Mood> filteredMoods = new ArrayList<>(originalMoods);

        for (String filter : filters) {
            if (filter.equals("rChronological")) {
                sortReverseChronological(filteredMoods);
            }
            if (filter.equals("recent")) {
                sortRecentWeek(filteredMoods);
            }
            if (filter.equals("states")) {
                sortEmotionalState(filteredMoods);
            }
        }
        return filteredMoods;
    }

    /**
     * This method sorts an ArrayList of moods in reverse chronological order
     * @param moods: An ArrayList of Mood objects
     */
    public static void sortReverseChronological(ArrayList<Mood> moods) {
        moods.sort((mood1, mood2) -> mood2.getPostedDate().compareTo(mood1.getPostedDate()));
    }

    /**
     * This method sorts an ArrayList of moods to only include those in the recent week
     * @param moods: An ArrayList of Mood objects
     */
    public static void sortRecentWeek(ArrayList<Mood> moods) {
        Calendar calendar = Calendar.getInstance();
        // go back 7 days to get recent week
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date recentWeek = calendar.getTime();

        // remove the mood from the moods arraylist if it happened before the recent week
        moods.removeIf(mood -> mood.getPostedDate().before(recentWeek));
    }

    /**
     * This method sorts/filters an Arraylist of moods to only include those with particular emotional state(s)
     * @param moods: An ArrayList of Mood objects
     */
    public static void sortEmotionalState(ArrayList<Mood> moods) {
        // remove moods from the arraylist if it's not one of the moods selected and in the selectedStates arraylist
        moods.removeIf(mood -> !selectedStates.contains(mood.getEmotionalState()));
    }

    public static Set<String> getFilters() {
        return filters;
    }
}
