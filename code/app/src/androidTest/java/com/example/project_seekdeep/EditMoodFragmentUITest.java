package com.example.project_seekdeep;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static org.hamcrest.CoreMatchers.anything;

import android.util.Log;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;


// Taken from Seth's lab-07 instructions on UI testing :)
// and Kevin Tu and Jachelle Chan's MoodHistoryFragmentUiTest
// Taken on: 2025-03-08
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EditMoodFragmentUITest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    private final UserProfile testUser = new UserProfile("kevtu2", "222");
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("UserDB");
    private final CollectionReference moodsRef = db.collection("MoodDB");

    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void seedDatabase() throws InterruptedException {
        Mood mood1 = new Mood(testUser, EmotionalStates.HAPPINESS, new String[]{"Done testing!","Alone"});

        // give time so we can sort the list
        Thread.sleep(5000);
        Mood mood2 = new Mood(testUser, EmotionalStates.CONFUSION, new String[]{"How to test?", "With Another Person"});
        Thread.sleep(5000);
        Mood mood3 = new Mood(testUser, EmotionalStates.SADNESS, new String[]{"Midterms", "With a Crowd"});
        // make mood with a date outside of recent week
        Thread.sleep(2000);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -10);
        Mood mood4 = new Mood(testUser, EmotionalStates.ANGER, SocialSituations.ALONE, calendar.getTime(), "Midterms are hard");

        usersRef.document().set(testUser);

        moodsRef.document().set(mood1);
        moodsRef.document().set(mood2);
        moodsRef.document().set(mood3);
        moodsRef.document().set(mood4);


        // log in first
        scenario.getScenario().onActivity(activity -> activity.setCurrentUser(testUser));
        scenario.getScenario().onActivity(activity -> activity.successful_login());
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
    public void testEditMoodWithInvalidReasonShouldError() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save the view with the sadness mood event still available
        ViewInteraction view = onView(withText("☹️ Sadness"));

        // choose the sadness mood to edit and press the edit button associated with that mood event
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
        // change reason to be Midterms oh no!
        onView(withId(R.id.edit_reason)).perform(clearText());
        String invalidReason = "A".repeat(201);
        onView(withId(R.id.edit_reason)).perform(ViewActions.typeText(invalidReason));

        // submit form and check if the error is shown to the user
        onView(withId(android.R.id.button1)).perform(click());
        onView(withId(R.id.edit_reason)).check(matches(hasErrorText("Reason must be ≤ 200 characters")));
    }




    @Test
    public void testEditMood() throws InterruptedException {
        // give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.History)).perform(click());
        // give time for the history/profile page to show up
        Thread.sleep(2000);
        // save the view with the sadness mood event still available
        ViewInteraction view = onView(withText("☹️ Sadness"));

        // choose the sadness mood to edit and press the edit button associated with that mood event
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
        // change reason to be Midterms oh no!
        onView(withId(R.id.edit_reason)).perform(clearText());
        onView(withId(R.id.edit_reason)).perform(typeText("Midterms oh no!"));

        onView(withId(R.id.emotion_spinner)).perform(click());
        // change sadness to fear
        // This next line is taken from https://stackoverflow.com/a/40015436
        // Taken by: Jachelle Chan
        // Taken on: March 13, 2025
        onView(withText("😨 Fear")).inRoot(isPlatformPopup()).perform(click());

        onView(withId(android.R.id.button1)).perform(click());
        // check to see if the edit has edited those details on the history page
        onView(withText("😨 Fear")).check(matches(isDisplayed()));

        // check if the edit has edited details when clicking
        onData(new BoundedMatcher<Object, Mood>(Mood.class) {
            @Override
            protected boolean matchesSafely(Mood mood) {
                return mood.getEmotionalState().toString().equals("😨 Fear");
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with emotion: Fear");
            }
        }).inAdapterView(withId(R.id.history_listview)).onChildView(withId(R.id.edit_mood_button)).perform(click());
        Thread.sleep(3000);

        // check that the mood details are displayed properly
        onView(withId(R.id.emotion_spinner)).check(matches(hasDescendant(withText("😨 Fear"))));
        onView(withId(R.id.edit_reason)).check(matches(withText("Midterms oh no!")));
        onView(withId(R.id.social_situation_spinner)).check(matches(hasDescendant(withText("With a Crowd"))));
    }


}
