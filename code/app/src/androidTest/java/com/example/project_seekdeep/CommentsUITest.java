package com.example.project_seekdeep;

import static androidx.core.app.PendingIntentCompat.getActivity;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.not;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.hamcrest.Matchers;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CommentsUITest {
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

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -10);

        Mood mood1 = new Mood(user1, EmotionalStates.HAPPINESS, SocialSituations.ALONE, null, calendar.getTime(), "I love food!");
        // give time so we can sort the list
        Thread.sleep(5000);
        Mood mood2 = new Mood(user1, EmotionalStates.CONFUSION, SocialSituations.WITH_ANOTHER, null, calendar.getTime(), "I hate homework!!");
        Thread.sleep(5000);

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

    @Test
    public void clickOnMoodShouldDisplayMoodCommentsFragment() throws InterruptedException {

        // Navigate to feed
        onView(withId(R.id.feed_bottom_nav)).perform(click());
        Thread.sleep(1000);

        // Click on first item
        onView(withText("I hate homework!!")).perform(click());

        Thread.sleep(1000);

        // Check to see if clicked on mood displays correct mood information.

        onView(withText("check2" + "'s Mood")).check(matches(isDisplayed()));
        onView(withText(EmotionalStates.CONFUSION.toString())).check(matches(isDisplayed()));
        onView(withText("I hate homework!!")).check(matches(isDisplayed()));
        onView(withText(SocialSituations.WITH_ANOTHER.toString())).check(matches(isDisplayed()));
    }

    @Test
    public void clickOnMoodShouldDisplayCorrectComments() throws InterruptedException {
        // Navigate to feed
        onView(withId(R.id.feed_bottom_nav)).perform(click());
        Thread.sleep(1000);

        // Click on second item
        onView(withText("I love food!")).perform(click());

        Thread.sleep(1000);

        ArrayList<Comment> comments = new ArrayList<>();
        Query CommentsQuery = commentsRef.whereEqualTo("reason", "I love food!");
        CommentsQuery.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {
                for (QueryDocumentSnapshot snapshot : value) {
                    Comment comment = new Comment(snapshot.getDocumentReference("mood"), snapshot.getString("username"), snapshot.getString("comment"));
                    comments.add(comment);
                }
            }
        });
        // Check if mood displays all correct comments
        for (Comment comment : comments) {
            onView(withText(comment.getComment())).check(matches(isDisplayed()));
        }
    }

    @Test
    public void newCommentAddedShouldBeDisplayed() throws InterruptedException {
        // Navigate to feed
        onView(withId(R.id.feed_bottom_nav)).perform(click());
        Thread.sleep(1000);

        // Click on first item
        onView(withText("I love food!")).perform(click());

        onView(withId(R.id.add_comment_input)).perform(ViewActions.typeText("This is a test comment!!!"));
        onView(withContentDescription("Add comment")).perform(click());
        Thread.sleep(1000);
        onView(withText("This is a test comment!!!")).check(matches(isDisplayed()));
        Thread.sleep(1000);

        // Go back, add comment on the other mood event.
        onView(withId(R.id.back_button)).perform(click());

        // Click on second item
        onView(withText("I hate homework!!")).perform(click());

        onView(withId(R.id.add_comment_input)).perform(ViewActions.typeText("This is another test comment!!!"));
        onView(withContentDescription("Add comment")).perform(click());
        Thread.sleep(1000);
        onView(withText("This is another test comment!!!")).check(matches(isDisplayed()));
        Thread.sleep(1000);
    }
}
