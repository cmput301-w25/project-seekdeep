package com.example.project_seekdeep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import org.junit.Test;

import java.util.Calendar;

import java.util.ArrayList;

public class MoodFilteringTest {
    private UserProfile testUser = new UserProfile("jshello", "tofu123");

    @Test
    public void testSortReverseChronological() {
        ArrayList<Mood> moods = getMoods();

        // test the .sortReverseChronological method
        MoodFiltering.sortReverseChronological(moods);

        // assert to see if the moods are actually in reverse chronological order
        assertTrue(moods.get(0).getPostedDate().after(moods.get(1).getPostedDate()));
        assertTrue(moods.get(1).getPostedDate().after(moods.get(2).getPostedDate()));
    }

    @Test
    public void testSortRecentWeek() {
        ArrayList<Mood> moods = getMoods();

        // test the .sortRecentWeek method
        MoodFiltering.sortRecentWeek(moods);

        // see if only the 2 moods that are in the range are there
        assertEquals("Expected exactly 2 moods within the last week", 2, moods.size());

        // mood 2: shame (within the week)
        // mood 3: sadness (within the week)

        // flags to check if specific moods are in moods
        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : moods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood1 = true;
            }
            if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
        }

        // check to see if shame and sadness are there and ensure anger isn't
        assertTrue("Filtered moods should contain sadness", containsMood3);
        assertTrue("Filtered moods should contain shame", containsMood2);
        assertFalse("Filtered moods should NOT contain anger from 10 days ago", containsMood1);

    }

    @Test
    public void testStackMoods() {
        ArrayList<Mood> moods = getMoods();
        MoodFiltering.saveOriginal(moods);

        MoodFiltering.applyFilter("rChronological");
        MoodFiltering.applyFilter("recent");
        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
        // filteredMoods should now contain [SADNESS, SHAME] in that order
        // assert to see if the moods are actually in reverse chronological order
        assertTrue(filteredMoods.get(0).getPostedDate().after(filteredMoods.get(1).getPostedDate()));
        assertEquals(EmotionalStates.SADNESS.toString(), filteredMoods.get(0).getEmotionalState().toString());
        assertEquals(EmotionalStates.SHAME.toString(), filteredMoods.get(1).getEmotionalState().toString());

        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood1 = true;
            }
            if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
        }

        // check to see if shame and sadness are there and ensure anger isn't
        assertTrue("Filtered moods should contain sadness", containsMood3);
        assertTrue("Filtered moods should contain shame", containsMood2);
        assertFalse("Filtered moods should NOT contain anger from 10 days ago", containsMood1);
    }

    @Test
    public void testRemoveRecentFilter() {
        ArrayList<Mood> moods = getMoods();
        MoodFiltering.saveOriginal(moods);

        MoodFiltering.applyFilter("rChronological");
        MoodFiltering.applyFilter("recent");
        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
        // filteredMoods should now contain [SADNESS, SHAME] in that order

        // assert to see if the moods are actually in reverse chronological order
        assertTrue(filteredMoods.get(0).getPostedDate().after(filteredMoods.get(1).getPostedDate()));
        assertEquals(EmotionalStates.SADNESS.toString(), filteredMoods.get(0).getEmotionalState().toString());
        assertEquals(EmotionalStates.SHAME.toString(), filteredMoods.get(1).getEmotionalState().toString());

        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood1 = true;
            }
            if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
        }

        // check to see if shame and sadness are there and ensure anger isn't
        assertTrue("Filtered moods should contain sadness", containsMood3);
        assertTrue("Filtered moods should contain shame", containsMood2);
        assertFalse("Filtered moods should NOT contain anger from 10 days ago", containsMood1);

        // now remove the recent week filter
        MoodFiltering.removeFilter("recent");
        filteredMoods = MoodFiltering.getFilteredMoods();
        // filteredMoods should now be [SADNESS, SHAME, ANGER] in that order

        // assert to see if the moods are actually in reverse chronological order
        assertTrue(filteredMoods.get(0).getPostedDate().after(filteredMoods.get(1).getPostedDate()));
        assertTrue(filteredMoods.get(1).getPostedDate().after(filteredMoods.get(2).getPostedDate()));
        assertEquals(EmotionalStates.SADNESS.toString(), filteredMoods.get(0).getEmotionalState().toString());
        assertEquals(EmotionalStates.SHAME.toString(), filteredMoods.get(1).getEmotionalState().toString());
        assertEquals(EmotionalStates.ANGER.toString(), filteredMoods.get(2).getEmotionalState().toString());

        boolean containsMood10 = false;
        boolean containsMood20 = false;
        boolean containsMood30 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood10 = true;
            }
            if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood20 = true;
            }
            if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood30 = true;
            }
        }

        // check to see if shame and sadness are there and ensure anger isn't
        assertTrue("Filtered moods should contain sadness", containsMood30);
        assertTrue("Filtered moods should contain shame", containsMood20);
        assertTrue("Filtered moods should 10 days ago", containsMood10);
    }

    @Test
    public void testRemoveRChronoFilter() {
        ArrayList<Mood> moods = getMoods();
        MoodFiltering.saveOriginal(moods);

        MoodFiltering.applyFilter("rChronological");
        MoodFiltering.applyFilter("recent");
        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
        // filteredMoods should now contain [SADNESS, SHAME] in that order

        // assert to see if the moods are actually in reverse chronological order
        assertTrue(filteredMoods.get(0).getPostedDate().after(filteredMoods.get(1).getPostedDate()));
        assertEquals(EmotionalStates.SADNESS.toString(), filteredMoods.get(0).getEmotionalState().toString());
        assertEquals(EmotionalStates.SHAME.toString(), filteredMoods.get(1).getEmotionalState().toString());

        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood1 = true;
            }
            if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
        }

        // check to see if shame and sadness are there and ensure anger isn't
        assertTrue("Filtered moods should contain sadness", containsMood3);
        assertTrue("Filtered moods should contain shame", containsMood2);
        assertFalse("Filtered moods should NOT contain anger from 10 days ago", containsMood1);

        // now remove the rChrono filter
        MoodFiltering.removeFilter("rChronological");
        filteredMoods = MoodFiltering.getFilteredMoods();
        // filteredMoods should now be [SADNESS, SHAME]

        boolean containsMood10 = false;
        boolean containsMood20 = false;
        boolean containsMood30 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood10 = true;
            }
            if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood20 = true;
            }
            if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood30 = true;
            }
        }

        // check to see if shame and sadness are there and ensure anger isn't
        assertTrue("Filtered moods should contain sadness", containsMood30);
        assertTrue("Filtered moods should contain shame", containsMood20);
        assertFalse("Filtered moods should NOT contain 10 days ago", containsMood10);
    }

    @NonNull
    private ArrayList<Mood> getMoods() {
        // creating moods to work with
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -10);
        Mood mood1 = new Mood(testUser, EmotionalStates.ANGER, calendar.getTime());

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        Mood mood2 = new Mood(testUser, EmotionalStates.SHAME, calendar.getTime());

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Mood mood3 = new Mood(testUser, EmotionalStates.SADNESS, calendar.getTime());

        // add moods to an ArrayList
        ArrayList<Mood> moods = new ArrayList<>();
        moods.add(mood1);
        moods.add(mood2);
        moods.add(mood3);
        return moods;
    }
}
