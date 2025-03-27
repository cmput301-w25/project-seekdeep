package com.example.project_seekdeep;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
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
import java.util.Calendar;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OtherUsersProfileFragmentTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    private UserProfile testUser1 = new UserProfile("User1", "12345678");
    private UserProfile testUser2 = new UserProfile("User2", "12345678");

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

        Mood mood1 = new Mood(testUser2, EmotionalStates.HAPPINESS, SocialSituations.ALONE);
        // give time so we can sort the list
        Thread.sleep(5000);
        Mood mood2 = new Mood(testUser2, EmotionalStates.CONFUSION, SocialSituations.WITH_ANOTHER);
        Thread.sleep(5000);
        Mood mood3 = new Mood(testUser2, EmotionalStates.SADNESS, SocialSituations.CROWD);
        // make mood with a date outside of recent week
        Thread.sleep(2000);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -10);
        Mood mood4 = new Mood(testUser2, EmotionalStates.ANGER, SocialSituations.ALONE, calendar.getTime());

        usersRef.document().set(testUser2);

        moodsRef.document().set(mood1);
        moodsRef.document().set(mood2);
        moodsRef.document().set(mood3);
        moodsRef.document().set(mood4);


        // log in first
        scenario.getScenario().onActivity(activity -> activity.setCurrentUser(testUser1));
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
    public void testClickOnAnotherUsernameToGoToProfile() throws InterruptedException {
        //Give time to log into User1's account
        Thread.sleep(2000);

        //On Feed fragment, these moods from User2 should be displayed
//        onView(withText("\uD83D\uDE04 Happiness")).check(matches(isDisplayed()));
//        onView(withText("\uD83E\uDD14 Confusion")).check(matches(isDisplayed()));
//        onView(withText("☹️ Sadness")).check(matches(isDisplayed()));
//        onView(withText("\uD83D\uDE20 Anger")).check(matches(isDisplayed()));

        //Click on the User2's username
        onData(new BoundedMatcher<Object, Mood>(Mood.class) {
            @Override
            protected boolean matchesSafely(Mood mood) {
                return mood.getEmotionalState().toString().equals("☹️ Sadness");
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with emotion: Sadness");
            }
        }).inAdapterView(withId(R.id.list_view_mood)).onChildView(withId(R.id.username)).perform(click());
        Thread.sleep(2000);

        //Check that the clicked-on user's profile is displayed
        onView(withText("User2")).check(matches(isDisplayed()));
    }


}
