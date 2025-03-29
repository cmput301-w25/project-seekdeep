package com.example.project_seekdeep;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.not;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PrivacyUITest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);
    private final UserProfile testUser = new UserProfile("User1", "pass1");
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference usersRef = db.collection("UserDB");
    private CollectionReference moodsRef = db.collection("MoodDB");
    private CollectionReference commentsRef = db.collection("comments");

    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void seedDatabase() throws InterruptedException {
        // Add some data
        UserProfile user1 = new UserProfile("check2", "pass");
        UserProfile michael = new UserProfile("jordan", "ball");
        UserProfile alan = new UserProfile("turingMachine", "enigma");


        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -10);

        // These don't necessarily have a isPrivate field specified (ensure that our app doesn't crash because of this)
        Mood mood1 = new Mood(user1, EmotionalStates.HAPPINESS, SocialSituations.ALONE, null, calendar.getTime(), "I love food!");
        // give time so we can sort the list
        Thread.sleep(5000);
        Mood mood2 = new Mood(user1, EmotionalStates.CONFUSION, SocialSituations.WITH_ANOTHER, null, calendar.getTime(), "I hate homework!!");
        Thread.sleep(5000);

        String[] mood3Fields = {
                "It works!!!",
                SocialSituations.CROWD.toString()
        };
        Mood mood3 = new Mood(alan, EmotionalStates.HAPPINESS,  mood3Fields, true);

        String[] mood4Fields = {
                "I've missed more than 9000 shots in my career. I've lost almost 300 games. 26 times, I've been trusted to take the game winning shot and missed. I've failed over and over and over again in my life. And that is why I succeed.",
                SocialSituations.ALONE.toString()
        };
        Mood mood4 = new Mood(michael, EmotionalStates.HAPPINESS, mood4Fields, false);

        String[] mood5Fields = {
            "Turing machine up and running",
            SocialSituations.ALONE.toString()
        };
        Mood mood5 = new Mood(alan, EmotionalStates.HAPPINESS, mood5Fields, false);

        usersRef.document().set(user1);

        moodsRef.add(mood1).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                // For some reason doesn't work with Comment object lol
                Map<String, Object> comment1 = new HashMap<>();
                comment1.put("username", "User1");
                comment1.put("comment", "Heck yeah! I love food too. We should get some food together");
                comment1.put("mood", documentReference);
                comment1.put("date", Timestamp.now().toDate());

                Map<String, Object> comment2 = new HashMap<>();
                comment2.put("username", "Saurabh");
                comment2.put("comment", "Where is my invite?");
                comment2.put("mood", documentReference);
                comment2.put("date", Timestamp.now().toDate());

                Map<String, Object> comment3 = new HashMap<>();
                comment3.put("username", "George_Washington_1776");
                comment3.put("comment", "What kind of food did you get?");
                comment3.put("mood", documentReference);
                comment3.put("date", Timestamp.now().toDate());

                commentsRef.add(comment1);
                commentsRef.add(comment2);
                commentsRef.add(comment3);
            }
        });

        moodsRef.add(mood2).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Map<String, Object> comment1 = new HashMap<>();
                comment1.put("username", "User42");
                comment1.put("comment", "Study harder bud.");
                comment1.put("mood", documentReference);
                comment1.put("date", Timestamp.now().toDate());

                Map<String, Object> comment3 = new HashMap<>();
                comment3.put("username", "ZhongXiNa");
                comment3.put("comment", "Can I study with?");
                comment3.put("mood", documentReference);
                comment3.put("date", Timestamp.now().toDate());

                commentsRef.add(comment1);
                commentsRef.add(comment3);
            }
        });

        moodsRef.add(mood3).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Map<String, Object> comment1 = new HashMap<>();
                comment1.put("username", "Churchill");
                comment1.put("comment", "Lets go!!!");
                comment1.put("mood", documentReference);
                comment1.put("date", Timestamp.now().toDate());
                commentsRef.add(comment1);
            }
        });

        moodsRef.add(mood4).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Map<String, Object> comment1 = new HashMap<>();
                comment1.put("username", "JordanFan123");
                comment1.put("comment", "Great words from the greatest.");
                comment1.put("mood", documentReference);
                comment1.put("date", Timestamp.now().toDate());

                Map<String, Object> comment2 = new HashMap<>();
                comment2.put("username", "ChicagoLover22");
                comment2.put("comment", "Couldn't have said it any better.");
                comment2.put("mood", documentReference);
                comment2.put("date", Timestamp.now().toDate());

                Map<String, Object> comment3 = new HashMap<>();
                comment3.put("username", "Saurabh");
                comment3.put("comment", "The goat!");
                comment3.put("mood", documentReference);
                comment3.put("date", Timestamp.now().toDate());

                commentsRef.add(comment1);
                commentsRef.add(comment2);
            }
        });

        moodsRef.add(mood5);

        // Force log in
        scenario.getScenario().onActivity(activity -> activity.setCurrentUser(testUser));
        scenario.getScenario().onActivity(MainActivity::successful_login);
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

    /**
     * This test is to ensure that previous moods in the DB that do not have the "private"
     * field does not crash the entire app if that field is read. They should also have
     * public visibility.
     */
    @Test
    public void preExistingMoodsShouldBePublicAndShown() throws InterruptedException {
        // Navigate to feed
        onView(withId(R.id.feed_bottom_nav)).perform(click());
        Thread.sleep(1000);

        // The moods with these reason fields are ones that are added to the db without private field.
        onView(withText("I love food!")).check(matches(isDisplayed()));
        onView(withText("I hate homework!!")).check(matches(isDisplayed()));
    }

    @Test
    public void privateMoodsShouldNotDisplay() throws InterruptedException {
        // Navigate to feed
        onView(withId(R.id.feed_bottom_nav)).perform(click());
        Thread.sleep(1000);

        onView(withText("It works!!!")).check(doesNotExist());
        onView(withText("Turing machine up and running")).check(doesNotExist());
    }

}
