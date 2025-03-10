package com.example.project_seekdeep;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

/**
 * java doc for test
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodHistoryFragmentTest {

    /**
     * java doc for test
     */
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    /**
     * java doc for test
     */
    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    /**
     * java doc for test
     */
    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("UserDB");
        CollectionReference moodsRef = db.collection("MoodDB");

        UserProfile testUser = new UserProfile("kevtu2", "222");
        Mood[] moods = {
                new Mood(testUser, EmotionalStates.HAPPINESS, SocialSituations.ALONE, "Food"),
                new Mood(testUser, EmotionalStates.CONFUSION, SocialSituations.WITH_ANOTHER, "Homework"),
                new Mood(testUser, EmotionalStates.SADNESS, SocialSituations.CROWD, "Midterms")
        };

        usersRef.document().set(testUser);

        for (Mood mood : moods) {
            moodsRef.document().set(mood);
        }

    }

    /**
     * java doc for test
     */
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

    /**
     * java doc for test
     */
    @Test
    public void appShouldDisplayExistingMoods() throws InterruptedException {
        onView(withId(R.id.History)).perform(click());

        // Write tests for displaying existing moods..
    }


}
