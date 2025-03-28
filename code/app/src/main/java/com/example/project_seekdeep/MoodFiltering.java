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
 * How to use:
 *   <pre>
 *   ArrayList<Mood> moods
 *   MoodFiltering.removeAllFilters();
 *   MoodFiltering.sortReverseChronological(moods);
 *   MoodFiltering.saveOriginal(moods);
 *   MoodFiltering.applyFilter("recent");
 *   MoodFilter.addStates(ArrayList of EmotionalStates);
 *   MoodFiltering.applyFilter("states");
 *   ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
 *   Then use filteredMoods for displaying
 *   </pre>
 * To remove a filter:
 *     <pre>
 *    MoodFiltering.removeFilter("rChronological");
 *    filteredMoods = MoodFiltering.getFilteredMoods();
 *    </pre>
 * Filters available:
 * <pre>
 *     rChronological: Sort reverse chronologically
 *     recent: Sort by recent week
 * </pre>
 *     <STRONG>Note that since this class is static, applied filters from other fragments may still be applied. So use MoodFiltering.removeAllFilters() first</STRONG>
 * @author Jachelle Chan
 */
public class MoodFiltering {
    private static ArrayList<Mood> originalMoods; // og list of moods
    private static Set<String> filters = new HashSet<>(); // the filters applied

    private static Set<EmotionalStates> selectedStates = new HashSet<>(); // the selected emotional state(s) to filter by
    private static String keyword = "";

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
     *                    Filter names include: "rChronological", "recent", "states", "keyword", "last3"
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
     * @param states: An arraylist of EmotionalStates that the user wants to filter by
     */

    public static void addStates(ArrayList<EmotionalStates> states) {
        selectedStates.clear();
        selectedStates.addAll(states);
    }

    /**
     * This method adds the keyword the user wants to filter
     * @param word
     */
    public static void addKeyword(String word) {
        keyword = word;
    }

    /**
     * This method removes all filters that were selected by the user.
     */
    public static void removeAllFilters() {
        // reverse chronological should NOT be removed ever
        filters.remove("recent");
        filters.remove("states");
        filters.remove("keyword");
        filters.remove("last3");
    }
    /**
     * This method gets the moods that matches the filters that were applied
     * @return
     *      An ArrayList with filtered moods
     */
    public static ArrayList<Mood> getFilteredMoods() {
        ArrayList<Mood> filteredMoods = new ArrayList<>(originalMoods);

        for (String filter : filters) {
            switch (filter) {
                case "rChronological":
                    sortReverseChronological(filteredMoods);
                    break;
                case "recent":
                    sortRecentWeek(filteredMoods);
                    break;
                case "states":
                    sortEmotionalState(filteredMoods);
                    break;
                case "keyword":
                    sortKeyword(filteredMoods);
                    break;
                case "last3":
                    sortLast3(filteredMoods);
                    break;
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

    /**
     * This method filters an ArrayList of moods to only include those with a keyword. Exact matches only, not case-sensitive.
     * @param moods: An ArrayList of Mood objects
     */
    public static void sortKeyword(ArrayList<Mood> moods) {
        // the regex for this method is taken from https://stackoverflow.com/a/9464309
        // Author: DNA
        // Taken by: Jachelle Chan
        // Taken on: March 19, 2025
        // remove moods from the arraylist if it doesn't contain the keyword
        moods.removeIf(mood -> !mood.getReason().toLowerCase().contains(keyword));
    }


    /**
     * This method filters an ArrayList of moods to only include those that are the most recent.
     * @param moods: An ArrayList of Mood objects
     */
    public static void sortLast3(ArrayList<Mood> moods) {
        // directly modify the arraylist
        // keep only the first 3 moods by using sublist from the 3rd to the end of the arraylist and then clear
        moods.subList(3,moods.size()).clear();
    }

    /**
     * This method returns the filters currently applied.
     * Note that since this class is static, applied filters from other fragments may still be applied.
     * @return: A set of string filters applied
     */
    public static Set<String> getFilters() {
        return filters;
    }
}
