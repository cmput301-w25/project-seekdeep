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
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static org.hamcrest.CoreMatchers.anything;

import android.app.Instrumentation;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapsFragmentUITest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);
    private final UserProfile testUser = new UserProfile("sbaghel", "pass1234");
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference usersRef = db.collection("users");
    private final CollectionReference moodsRef = db.collection("MoodDB");
    private final CollectionReference locationsRef = db.collection("locations");

    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void seedDatabase() throws InterruptedException {
        Mood mood1 = new Mood(testUser, EmotionalStates.HAPPINESS, new String[]{"Start testing!","Alone"});

        // Spend some Time to sort the list
        Thread.sleep(5000);
        Mood mood2 = new Mood(testUser, EmotionalStates.CONFUSION, new String[]{"How to test?", "With Another Person"});
        Thread.sleep(5000);
        Mood mood3 = new Mood(testUser, EmotionalStates.SADNESS, new String[]{"Finals", "With a Crowd"});
        // Mood with date outside of recent week
        Thread.sleep(2000);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -10);
        Mood mood4 = new Mood(testUser, EmotionalStates.ANGER, SocialSituations.ALONE, calendar.getTime(), "Let's get this done");

        usersRef.document().set(testUser);

        moodsRef.document().set(mood1);
        moodsRef.document().set(mood2);
        moodsRef.document().set(mood3);
        moodsRef.document().set(mood4);

        locationsRef.document().set(new UserLocation(53.5259933,-113.5234554, EmotionalStates.HAPPINESS,"sbaghel","mood1" ));
        locationsRef.document().set(new UserLocation(53.5259999,-113.5225457, EmotionalStates.CONFUSION, "sbaghel","mood2" ));
        locationsRef.document().set(new UserLocation(53.5259343,-113.5245227, EmotionalStates.SADNESS, "sbaghel","mood3"));
        locationsRef.document().set(new UserLocation(53.5253453,-113.5212687, EmotionalStates.ANGER,  "sbaghel","mood4"));

        // Test User Log in first
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
    public void testMapFragmentLoads() throws InterruptedException {
        // Give time for the login to process
        Thread.sleep(2000);
        onView(withId(R.id.Map)).perform(click());
        // Give time for the map Fragment  to show up
        Thread.sleep(2000);

        // Check the map view is displayed
        onView(withId(R.id.map)).check(matches(isDisplayed()));

        // Simulate a mock location to ensure the blue dot appears (San Francisco coordinates)
        Location mockLocation = new Location("mock_provider");
        mockLocation.setLatitude(53.5259343);
        mockLocation.setLongitude(-113.5234554);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setAccuracy(1.0f);

        LocationManager locationManager = (LocationManager) InstrumentationRegistry.getInstrumentation().getTargetContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.setTestProviderEnabled("gps", true);
        locationManager.setTestProviderLocation("gps", mockLocation);


        onView(withId(R.id.map)).check(matches(isDisplayed()));



        onView(withId(R.id.map)).check(matches(isDisplayed()));
    }

    @Test
    public void testLocationUpdates(){

    }

    @Test
    public void testMoodHistoryDisplays(){
    }

    @Test
    public void testMoodFollowingDisplays(){

    }

    @Test
    public void testFilterMoodHistoryButtonClick() {

    }

    @Test
    public void testFilterMoodFollowingButtonClick() {
    }

    @Test
    public void testFilter5KmRadiusFollowing() {
    }

    @Test
    public void testInfoViewMarkers(){

    }

}
