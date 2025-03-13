package com.example.project_seekdeep;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import android.util.Log;

import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;


// Taken from Seth's lab-07 instructions on UI testing :)
// Taken by: Kevin Tu
// Taken on: 2025-03-08
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodHistoryFragmentTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    private UserProfile testUser = new UserProfile("kevtu2", "222");

    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void seedDatabase() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("UserDB");
        CollectionReference moodsRef = db.collection("MoodDB");

        Mood[] moods = {
                new Mood(testUser, EmotionalStates.HAPPINESS, SocialSituations.ALONE, "Food"),
                new Mood(testUser, EmotionalStates.CONFUSION, SocialSituations.WITH_ANOTHER, "Homework"),
                new Mood(testUser, EmotionalStates.SADNESS, SocialSituations.CROWD, "Midterms")
        };

        usersRef.document().set(testUser);

        for (Mood mood : moods) {
            moodsRef.document().set(mood);
        }

        // log in first
        scenario.getScenario().onActivity(activity -> activity.successful_login());
        scenario.getScenario().onActivity(activity -> activity.setCurrentUser(testUser));
    }

    @After
    public void tearDown() {
        String projectId = "project-seekdeep";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Test
    public void appShouldDisplayExistingMoods() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(5000);

        onView(withText("\uD83D\uDE04 Happiness")).check(matches(isDisplayed()));
        onView(withText("\uD83E\uDD14 Confusion")).check(matches(isDisplayed()));
        onView(withText("☹️ Sadness")).check(matches(isDisplayed()));

        // click on edit button for sadness mood
        // The way to click a button inside a listview item is taken from https://stackoverflow.com/a/25373597
        // Taken by: Jachelle Chan
        // Taken on: March 13, 2025
        onData(new BoundedMatcher<Object, Mood>(Mood.class) {
            @Override
            protected boolean matchesSafely(Mood mood) {
                return mood.getEmotionalState().toString().equals("☹️ Sadness");
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with emotion: Sadness");
            }
        }).inAdapterView(withId(R.id.history_listview)).onChildView(withId(R.id.edit_mood_button)).perform(click());
        Thread.sleep(3000);

        // check that the mood details are displayed properly
        onView(withId(R.id.emotion_spinner)).check(matches(hasDescendant(withText("☹️ Sadness"))));
        onView(withId(R.id.edit_reason)).check(matches(withText("")));
        onView(withId(R.id.edit_trigger)).check(matches(withText("Midterms")));
        onView(withId(R.id.social_situation_spinner)).check(matches(hasDescendant(withText("With a Crowd"))));
    }


    @Test
    public void testAddMood() throws InterruptedException {
        // we're gonna make this mood a fear mood event
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.create_mood_bottom_nav)).perform(click());
        Thread.sleep(1000);
        // click
        onView(withId(R.id.edit_emotion_editText)).perform(click());
        Thread.sleep(1000);
        // select emotion as fear
        onView(withId(R.id.buttonFear)).perform(click());
        onView(withId(android.R.id.button1)).perform(click());


    }
/*
    @Test
    public void testDeleteMood() {

    }

    @Test
    public void testEditMood() {

    }

    @Test
    public void reasonWithMoreThan200CharShouldThrowError() {

    }
     */

}
