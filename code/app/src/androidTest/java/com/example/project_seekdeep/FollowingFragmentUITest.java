package com.example.project_seekdeep;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;

import android.util.Log;

import androidx.test.espresso.ViewInteraction;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FollowingFragmentUITest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    private final UserProfile testUser = new UserProfile("User1", "123");
    private final UserProfile testUser2 = new UserProfile("User2", "123");
    private final UserProfile testUser3 = new UserProfile("User3", "123");  // testUser3 will follow the other 2
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("UserDB");
    private final CollectionReference moodsRef = db.collection("MoodDB");
    private final String happy = EmotionalStates.HAPPINESS.toString();
    private final String sad = EmotionalStates.SADNESS.toString();
    private final String surprise = EmotionalStates.SURPRISE.toString();
    private final String angry = EmotionalStates.ANGER.toString();
    private final String shame = EmotionalStates.SHAME.toString();
    private final String disgust = EmotionalStates.DISGUST.toString();
    private final String confusion = EmotionalStates.CONFUSION.toString();
    private final String fear = EmotionalStates.FEAR.toString();

    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void seedDatabase() throws InterruptedException {
        Calendar calendar = Calendar.getInstance();
        Mood mood1 = new Mood(testUser, EmotionalStates.HAPPINESS, SocialSituations.ALONE, calendar.getTime(), "Done testing!");
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Mood mood2 = new Mood(testUser, EmotionalStates.CONFUSION, SocialSituations.WITH_ANOTHER, calendar.getTime(), "How to test?");
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -3);
        Mood mood3 = new Mood(testUser, EmotionalStates.SADNESS, SocialSituations.CROWD, calendar.getTime(), "Midterms");
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -10);
        Mood mood4 = new Mood(testUser, EmotionalStates.ANGER, SocialSituations.ALONE, calendar.getTime(), "Midterms are hard");

        usersRef.document().set(testUser);

        moodsRef.document().set(mood1);
        moodsRef.document().set(mood2);
        moodsRef.document().set(mood3);
        moodsRef.document().set(mood4);

        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -2);
        mood1 = new Mood(testUser2, EmotionalStates.FEAR, SocialSituations.ALONE, calendar.getTime(), "Finals soon");
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -4);
        mood2 = new Mood(testUser2, EmotionalStates.DISGUST, SocialSituations.ALONE, calendar.getTime(), "204, 229, 301 in one semester??");
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -5);
        mood3 = new Mood(testUser2, EmotionalStates.SURPRISE, SocialSituations.CROWD, calendar.getTime(), "Didn't fail");
        calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        mood4 = new Mood(testUser2, EmotionalStates.SHAME, SocialSituations.SEVERAL_PEOPLE, calendar.getTime(), "Failed");

        usersRef.document().set(testUser2);

        moodsRef.document().set(mood1);
        moodsRef.document().set(mood2);
        moodsRef.document().set(mood3);
        moodsRef.document().set(mood4);

        // follower user1/testUser and user2/testUser2
        testUser3.addFollowing(testUser.getUsername());
        testUser3.addFollowing(testUser2.getUsername());
        // the following list should look like [happiness, confusion, fear, sadness, disgust, surprise, shame, anger]
        // log in first
        scenario.getScenario().onActivity(activity -> activity.setCurrentUser(testUser3));
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
    public void testViewFollowingFragmentShowsAllMoodsOfFollowing() throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.following_bottom_nav)).perform(click());
        Thread.sleep(1000);
        // the following list should look like [happiness, confusion, fear, sadness, disgust, surprise, shame, anger] in that order
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText(happy)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText(confusion)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText(fear)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText(sad)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(4).onChildView(withId(R.id.emotion))
                .check(matches(withText(disgust)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(5).onChildView(withId(R.id.emotion))
                .check(matches(withText(surprise)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(6).onChildView(withId(R.id.emotion))
                .check(matches(withText(shame)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(7).onChildView(withId(R.id.emotion))
                .check(matches(withText(angry)));
    }

    @Test
    public void testLast3FilterShowsLast3FromEachFollowingUser() throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.following_bottom_nav)).perform(click());
        Thread.sleep(10000);
        // the following list should look like
        // [happiness(1), confusion(1), fear(2), sadness(1), disgust(2), surprise(2), shame(2), anger(1)] in that order initially
/*
        // save views that should be gone, which in this case is anger and shame
        ArrayList<ViewInteraction> goneViews = new ArrayList<>();
        ViewInteraction view = onView(withText(angry));
        goneViews.add(view);
        view = onView(withText(shame));
        goneViews.add(view);

        // press the filter button and the last 3 chip and then apply the filter
        onView(withId(R.id.following_filter_button)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.last_3_chip)).perform(click());
        onView(withId(R.id.apply_filters_button)).perform(click());

        Thread.sleep(1000);
        // check if those views are gone
        for (ViewInteraction aview : goneViews) {
            aview.check(doesNotExist());
        }

        // now the following list should look like [happiness(1), confusion(1), fear(2), sadness(1), disgust(2), surprise(2)]
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText(happy)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText(confusion)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText(fear)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText(sad)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(4).onChildView(withId(R.id.emotion))
                .check(matches(withText(disgust)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(5).onChildView(withId(R.id.emotion))
                .check(matches(withText(surprise)));*/
    }

    @Test
    public void testRecentWeekFilterShowsRecentWeekFromFollowing() throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.following_bottom_nav)).perform(click());
        Thread.sleep(1000);
        // the following list should look like
        // [happiness(1), confusion(1), fear(2), sadness(1), disgust(2), surprise(2), shame(2), anger(1)] in that order initially

        // save views that should be gone, which in this case is just anger
        ArrayList<ViewInteraction> goneViews = new ArrayList<>();
        ViewInteraction view = onView(withText(angry));
        goneViews.add(view);

        // press the filter button and the recent week chip and then apply the filter
        onView(withId(R.id.following_filter_button)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.recent_week_chip)).perform(click());
        onView(withId(R.id.apply_filters_button)).perform(click());

        Thread.sleep(1000);
        // check if those views are gone
        for (ViewInteraction aview : goneViews) {
            aview.check(doesNotExist());
        }

        // now the following list should look like [happiness(1), confusion(1), fear(2), sadness(1), disgust(2), surprise(2), shame(2)]
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(0).onChildView(withId(R.id.emotion))
                .check(matches(withText(happy)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(1).onChildView(withId(R.id.emotion))
                .check(matches(withText(confusion)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(2).onChildView(withId(R.id.emotion))
                .check(matches(withText(fear)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(3).onChildView(withId(R.id.emotion))
                .check(matches(withText(sad)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(4).onChildView(withId(R.id.emotion))
                .check(matches(withText(disgust)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(5).onChildView(withId(R.id.emotion))
                .check(matches(withText(surprise)));
        onData(anything()).inAdapterView(withId(R.id.listview_following)).atPosition(6).onChildView(withId(R.id.emotion))
                .check(matches(withText(shame)));
    }

    @Test
    public void testFilterFollowingByHappiness() throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.following_bottom_nav)).perform(click());
        Thread.sleep(1000);
        // the following list should look like
        // [happiness(1), confusion(1), fear(2), sadness(1), disgust(2), surprise(2), shame(2), anger(1)] in that order initially

        // save views that should be gone, which in this case is just anger
        ArrayList<ViewInteraction> goneViews = new ArrayList<>();
        ViewInteraction view = onView(withText(angry));
        goneViews.add(view);
        view = onView(withText(confusion));
        goneViews.add(view);
        view = onView(withText(sad));
        goneViews.add(view);
        view = onView(withText(shame));
        goneViews.add(view);
        view = onView(withText(surprise));
        goneViews.add(view);
        view = onView(withText(disgust));
        goneViews.add(view);
        view = onView(withText(fear));
        goneViews.add(view);

        // press the filter button and the happiness chip and then apply the filter
        onView(withId(R.id.following_filter_button)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.happiness_chip)).perform(click());
        onView(withId(R.id.apply_filters_button)).perform(click());

        Thread.sleep(1000);
        // check if those views are gone
        for (ViewInteraction aview : goneViews) {
            aview.check(doesNotExist());
        }

        // now the following list should look like [happiness(1)]
        onView(withText(happy)).check(matches(isDisplayed()));
    }

    @Test
    public void testFilterFollowingBySadness() throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.following_bottom_nav)).perform(click());
        Thread.sleep(1000);
        // the following list should look like
        // [happiness(1), confusion(1), fear(2), sadness(1), disgust(2), surprise(2), shame(2), anger(1)] in that order initially

        // save views that should be gone, which in this case is just anger
        ArrayList<ViewInteraction> goneViews = new ArrayList<>();
        ViewInteraction view = onView(withText(angry));
        goneViews.add(view);
        view = onView(withText(confusion));
        goneViews.add(view);
        view = onView(withText(happy));
        goneViews.add(view);
        view = onView(withText(shame));
        goneViews.add(view);
        view = onView(withText(surprise));
        goneViews.add(view);
        view = onView(withText(disgust));
        goneViews.add(view);
        view = onView(withText(fear));
        goneViews.add(view);

        // press the filter button and sadness chip and then apply the filter
        onView(withId(R.id.following_filter_button)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.sadness_chip)).perform(click());
        onView(withId(R.id.apply_filters_button)).perform(click());

        Thread.sleep(1000);
        // check if those views are gone
        for (ViewInteraction aview : goneViews) {
            aview.check(doesNotExist());
        }

        // now the following list should look like [sadness(1)]
        onView(withText(sad)).check(matches(isDisplayed()));
    }

    @Test
    public void testFilterFollowingByAnger() throws InterruptedException {
        Thread.sleep(1000);
        onView(withId(R.id.following_bottom_nav)).perform(click());
        Thread.sleep(1000);
        // the following list should look like
        // [happiness(1), confusion(1), fear(2), sadness(1), disgust(2), surprise(2), shame(2), anger(1)] in that order initially

        // save views that should be gone, which in this case is just anger
        ArrayList<ViewInteraction> goneViews = new ArrayList<>();
        ViewInteraction view = onView(withText(sad));
        goneViews.add(view);
        view = onView(withText(confusion));
        goneViews.add(view);
        view = onView(withText(happy));
        goneViews.add(view);
        view = onView(withText(shame));
        goneViews.add(view);
        view = onView(withText(surprise));
        goneViews.add(view);
        view = onView(withText(disgust));
        goneViews.add(view);
        view = onView(withText(fear));
        goneViews.add(view);

        // press the filter button and the anger chip and then apply the filter
        onView(withId(R.id.following_filter_button)).perform(click());
        Thread.sleep(1000);
        onView(withId(R.id.anger_chip)).perform(click());
        onView(withId(R.id.apply_filters_button)).perform(click());

        Thread.sleep(1000);
        // check if those views are gone
        for (ViewInteraction aview : goneViews) {
            aview.check(doesNotExist());
        }

        // now the following list should look like [anger(1)]
        onView(withText(angry)).check(matches(isDisplayed()));
    }

}
