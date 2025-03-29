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
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
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
        MoodFiltering.sortReverseChronological(moods);
        MoodFiltering.sortRecentWeek(moods);

        // filteredMoods should now contain [SADNESS, SHAME] in that order
        // assert to see if the moods are actually in reverse chronological order
        assertTrue(moods.get(0).getPostedDate().after(moods.get(1).getPostedDate()));
        assertEquals(EmotionalStates.SADNESS.toString(), moods.get(0).getEmotionalState().toString());
        assertEquals(EmotionalStates.SHAME.toString(), moods.get(1).getEmotionalState().toString());

        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : moods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood1 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
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
        MoodFiltering.removeAllFilters();
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
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
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
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood20 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
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
        MoodFiltering.removeAllFilters();
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
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
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
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood20 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood30 = true;
            }
        }

        // check to see if shame and sadness are there and ensure anger isn't
        assertTrue("Filtered moods should contain sadness", containsMood30);
        assertTrue("Filtered moods should contain shame", containsMood20);
        assertFalse("Filtered moods should NOT contain 10 days ago", containsMood10);
    }

    @Test
    public void testSortEmotionalState() {
        MoodFiltering.removeAllFilters();
        ArrayList<Mood> moods = getMoods();
        MoodFiltering.saveOriginal(moods);
        ArrayList<EmotionalStates> states = new ArrayList<>();
        states.add(EmotionalStates.ANGER);

        MoodFiltering.addStates(states);
        MoodFiltering.applyFilter("states");
        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
        // filteredMoods should now contain [ANGER] only
        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood1 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
        }

        // check to see if anger is there and shame and sadness are NOT there
        assertTrue("Filtered moods should contain anger", containsMood1);
        assertFalse("Filtered moods should NOT contain sadness", containsMood3);
        assertFalse("Filtered moods should NOT contain shame", containsMood2);
    }

    @Test
    public void testSortManyEmotionalStates() {
        MoodFiltering.removeAllFilters();
        ArrayList<Mood> moods = getMoods();
        MoodFiltering.saveOriginal(moods);
        ArrayList<EmotionalStates> states = new ArrayList<>();
        states.add(EmotionalStates.ANGER);
        states.add(EmotionalStates.SADNESS);

        MoodFiltering.addStates(states);
        MoodFiltering.applyFilter("states");
        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
        // filteredMoods should now contain [SADNESS, ANGER] only and in that order
        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood1 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
        }

        // check to see if anger is there and shame and sadness are NOT there
        assertTrue("Filtered moods should contain anger", containsMood1);
        assertTrue("Filtered moods should contain sadness", containsMood3);
        assertFalse("Filtered moods should NOT contain shame", containsMood2);
    }

    @Test
    public void testRemoveOneEmotionalStateFilter() {
        MoodFiltering.removeAllFilters();
        ArrayList<Mood> moods = getMoods();
        MoodFiltering.saveOriginal(moods);
        ArrayList<EmotionalStates> states = new ArrayList<>();
        states.add(EmotionalStates.ANGER);
        states.add(EmotionalStates.SADNESS);

        MoodFiltering.addStates(states);
        MoodFiltering.applyFilter("states");
        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
        // filteredMoods should now contain [SADNESS, ANGER] only and in that order
        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood1 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
        }
        // check to see if anger and sadness is there and shame isnt there
        assertTrue("Filtered moods should contain anger", containsMood1);
        assertTrue("Filtered moods should contain sadness", containsMood3);
        assertFalse("Filtered moods should NOT contain shame", containsMood2);
        // now remove one of the emotional state filters
        states.remove(EmotionalStates.SADNESS);  // should only have ANGER now
        MoodFiltering.addStates(states);
        MoodFiltering.applyFilter("states");
        filteredMoods = MoodFiltering.getFilteredMoods();

        boolean containsMood10 = false;
        boolean containsMood20 = false;
        boolean containsMood30 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood10 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood20 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood30 = true;
            }
        }
        // check to see if anger is there and shame and sadness are NOT there
        assertTrue("Filtered moods should contain anger", containsMood10);
        assertFalse("Filtered moods should NOT contain sadness", containsMood30);
        assertFalse("Filtered moods should NOT contain shame", containsMood20);
    }

    @Test
    public void testRemoveAllFilters() {
        MoodFiltering.removeAllFilters();
        ArrayList<Mood> moods = getMoods();
        MoodFiltering.saveOriginal(moods);
        ArrayList<EmotionalStates> states = new ArrayList<>();
        // apply recent filter and also an emotional state one
        MoodFiltering.applyFilter("recent");
        MoodFiltering.applyFilter("rChronological");
        ArrayList<EmotionalStates> testEmotion = new ArrayList<EmotionalStates>();
        testEmotion.add(EmotionalStates.SADNESS);
        MoodFiltering.addStates(testEmotion);
        MoodFiltering.applyFilter("states");
        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();

        // filteredMoods should now contain [sadness] only
        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood1 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
        }
        // check to see if sadness is the only one there
        assertFalse("Filtered moods should NOT contain anger", containsMood1);
        assertTrue("Filtered moods should contain sadness", containsMood3);
        assertFalse("Filtered moods should NOT contain shame", containsMood2);

        // remove all filters, should NOT remove the chronological order
        MoodFiltering.removeAllFilters();
        filteredMoods = MoodFiltering.getFilteredMoods();

        // filteredMoods should now contain [ANGER] only
        boolean containsMood10 = false;
        boolean containsMood20 = false;
        boolean containsMood30 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood10 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood20 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood30 = true;
            }
        }
        // check to see all moods are back
        assertTrue("Filtered moods should contain anger", containsMood10);
        assertTrue("Filtered moods should contain sadness", containsMood30);
        assertTrue("Filtered mood should contain shame", containsMood20);
        // assert to see if the moods are actually in reverse chronological order
        assertTrue(filteredMoods.get(0).getPostedDate().after(filteredMoods.get(1).getPostedDate()));
        assertTrue(filteredMoods.get(1).getPostedDate().after(filteredMoods.get(2).getPostedDate()));
    }

    @Test
    public void testSortKeyword() {
        MoodFiltering.removeAllFilters();
        ArrayList<Mood> moods = getMoods();
        MoodFiltering.saveOriginal(moods);
        MoodFiltering.addKeyword("123 ");
        MoodFiltering.applyFilter("keyword");

        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
        // filteredMoods should now contain [ANGER] only
        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood1 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
        }
        // check to see if anger is the only one
        assertTrue("Filtered moods should contain anger", containsMood1);
        assertFalse("Filtered moods should NOT contain sadness", containsMood3);
        assertFalse("Filtered moods should NOT contain shame", containsMood2);
    }

    @Test
    public void testFilterEmotionalStateAndRecentWeek() {
        MoodFiltering.removeAllFilters();
        ArrayList<Mood> moods = getMoods();
        // add another mood to test anger
        Calendar calendar = Calendar.getInstance();
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Mood mood10 = new Mood(testUser, EmotionalStates.ANGER, calendar.getTime(), "hi");
        moods.add(mood10);
        MoodFiltering.saveOriginal(moods);
        ArrayList<EmotionalStates> states = new ArrayList<>();
        states.add(EmotionalStates.ANGER);

        MoodFiltering.addStates(states);
        MoodFiltering.applyFilter("states");
        MoodFiltering.applyFilter("recent");
        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
        // filteredMoods should contain only the anger that was in the recent week
        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;
        boolean containsMood10 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER) && mood.getReason().equals("123 me")) {
                containsMood1 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.ANGER) && !mood.getReason().equals("123 me")) {
                containsMood10 = true;
            }
        }

        // check to see if anger is there and shame and sadness are NOT there
        assertFalse("Filtered moods should NOT contain anger from before recent week", containsMood1);
        assertFalse("Filtered moods should NOT contain sadness", containsMood3);
        assertFalse("Filtered moods should NOT contain shame", containsMood2);
        assertTrue("Filtered moods should contain anger in the recent week", containsMood10);
    }

    @Test
    public void testSortKeywordAndEmotionalState() {
        MoodFiltering.removeAllFilters();
        ArrayList<Mood> moods = getMoods();
        // add another mood to test with more
        Calendar calendar = Calendar.getInstance();
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Mood mood10 = new Mood(testUser, EmotionalStates.ANGER, calendar.getTime(), "hi");
        moods.add(mood10);
        MoodFiltering.saveOriginal(moods);
        ArrayList<EmotionalStates> states = new ArrayList<>();
        // sort by anger and keyword
        states.add(EmotionalStates.ANGER);
        MoodFiltering.addStates(states);
        MoodFiltering.addKeyword("123 ");
        MoodFiltering.applyFilter("keyword");
        MoodFiltering.applyFilter("states");

        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
        // filteredMoods should now contain [ANGER] only (the one with 123)
        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;
        boolean containsMood10 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER) && mood.getReason().equals("123 me")) {
                containsMood1 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.ANGER) && !mood.getReason().equals("123 me")) {
                containsMood10 = true;
            }
        }
        // check to see if anger is the only one
        assertTrue("Filtered moods should contain anger", containsMood1);
        assertFalse("Filtered moods should NOT contain sadness", containsMood3);
        assertFalse("Filtered moods should NOT contain shame", containsMood2);
        assertFalse("Filtered moods should NOT contain anger without 123", containsMood10);
    }

    @Test
    public void testSortKeywordAndEmotionalStateAndRecentWeek() {
        MoodFiltering.removeAllFilters();
        ArrayList<Mood> moods = getMoods();
        // add another mood to test with more
        Calendar calendar = Calendar.getInstance();
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Mood mood10 = new Mood(testUser, EmotionalStates.ANGER, calendar.getTime(), "hi");
        moods.add(mood10);
        MoodFiltering.saveOriginal(moods);
        ArrayList<EmotionalStates> states = new ArrayList<>();
        // sort by anger and keyword
        states.add(EmotionalStates.ANGER);
        MoodFiltering.addStates(states);
        MoodFiltering.addKeyword("123 ");
        MoodFiltering.applyFilter("keyword");
        MoodFiltering.applyFilter("states");
        MoodFiltering.applyFilter("recent");

        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
        // filteredMoods should now contain nothing because nothing should match
        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;
        boolean containsMood10 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER) && mood.getReason().equals("123 me")) {
                containsMood1 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.ANGER) && !mood.getReason().equals("123 me")) {
                containsMood10 = true;
            }
        }
        // check to see if anger is the only one
        assertFalse("Filtered moods should contain anger", containsMood1);
        assertFalse("Filtered moods should NOT contain sadness", containsMood3);
        assertFalse("Filtered moods should NOT contain shame", containsMood2);
        assertFalse("Filtered moods should NOT contain anger without 123", containsMood10);
    }

    @Test
    public void testSortKeywordShouldBeNotCaseSensitive() {
        MoodFiltering.removeAllFilters();
        ArrayList<Mood> moods = getMoods();
        MoodFiltering.saveOriginal(moods);
        MoodFiltering.addKeyword("GUYS ");
        MoodFiltering.applyFilter("keyword");

        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
        // filteredMoods should now contain [SHAME] only
        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;

        // iterate through the filtered moods to check the emotional states
        for (Mood mood : filteredMoods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood1 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
        }
        // check to see if anger is the only one
        assertFalse("Filtered moods should NOT contain anger", containsMood1);
        assertFalse("Filtered moods should NOT contain sadness", containsMood3);
        assertTrue("Filtered moods should contain shame", containsMood2);
    }

    @Test
    public void testSortLast3() {
        ArrayList<Mood> moods = getMoods();
        // add another mood so there's 4 moods instead of 3
        Calendar calendar = Calendar.getInstance();
        Mood amood = new Mood(testUser, EmotionalStates.HAPPINESS, calendar.getTime(), "3");
        moods.add(amood);

        // sort reverse chronological and recent 3
        MoodFiltering.sortReverseChronological(moods);
        MoodFiltering.sortLast3(moods);

        // should now only contain [happiness, sadness, shame]
        boolean containsMood1 = false;
        boolean containsMood2 = false;
        boolean containsMood3 = false;
        boolean containsMood4 = false;


        // iterate through the filtered moods to check the emotional states
        for (Mood mood : moods) {
            if (mood.getEmotionalState().equals(EmotionalStates.ANGER)) {
                containsMood1 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SHAME)) {
                containsMood2 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.SADNESS)) {
                containsMood3 = true;
            }
            else if (mood.getEmotionalState().equals(EmotionalStates.HAPPINESS)) {
                containsMood4 = true;
            }
        }
        // check to see if anger is the only one not there and the rest are
        assertFalse("Filtered moods should NOT contain anger", containsMood1);
        assertTrue("Filtered moods should contain sadness", containsMood3);
        assertTrue("Filtered moods should contain shame", containsMood2);
        assertTrue("Filtered moods should contain happiness", containsMood4);

    }

    @NonNull
    private ArrayList<Mood> getMoods() {
        // creating moods to work with
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -10);
        Mood mood1 = new Mood(testUser, EmotionalStates.ANGER, calendar.getTime(), "123 me");

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        Mood mood2 = new Mood(testUser, EmotionalStates.SHAME, calendar.getTime(), "guys im scared123");

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Mood mood3 = new Mood(testUser, EmotionalStates.SADNESS, calendar.getTime(), "crashing out rn");

        // add moods to an ArrayList
        ArrayList<Mood> moods = new ArrayList<>();
        moods.add(mood1);
        moods.add(mood2);
        moods.add(mood3);
        return moods;
    }
}
