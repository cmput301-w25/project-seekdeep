package com.example.project_seekdeep;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.isSelected;
import static org.hamcrest.Matchers.not;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import com.example.project_seekdeep.Followings.FollowRequest;
import com.example.project_seekdeep.Helpers.EmotionalStates;
import com.example.project_seekdeep.Helpers.SocialSituations;
import com.example.project_seekdeep.Helpers.UserLocation;
import com.example.project_seekdeep.Helpers.UserProfile;
import com.example.project_seekdeep.Moods.Mood;
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

// Taken from MoodHistoryFragmentUiTest.java
// https://developer.android.com/reference/androidx/test/uiautomator/UiDevice#getInstance(android.app.Instrumentation)
// https://stackoverflow.com/questions/26449391/setelapsedrealtimenanos-call-in-android-mock-location-provider-app
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
    private final CollectionReference followRef = db.collection("followings_and_requests");

    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void seedDatabase() throws InterruptedException, IOException {
        Mood mood1 = new Mood(testUser, EmotionalStates.HAPPINESS, new String[]{"Start testing!","Alone"});

        // Spend some Time to sort the list
        Thread.sleep(5000);
        Mood mood2 = new Mood(testUser, EmotionalStates.CONFUSION, new String[]{"How to test?", "With Another Person"});
        Thread.sleep(5000);

        usersRef.document().set(testUser);

        moodsRef.document().set(mood1);
        moodsRef.document().set(mood2);

        locationsRef.document().set(new UserLocation(53.5259933,-113.5234554, EmotionalStates.HAPPINESS,"sbaghel","mood1" ));
        locationsRef.document().set(new UserLocation(53.5259999,-113.5225457, EmotionalStates.CONFUSION, "sbaghel","mood2" ));

        UserProfile followedUser = new UserProfile("followedUser", "pass5678");
        usersRef.document().set(followedUser);
        Mood followedMood = new Mood(followedUser, EmotionalStates.SURPRISE, new String[]{"Let's follow!!", "Alone"});
        moodsRef.document().set(followedMood);
        locationsRef.document().set(new UserLocation(53.5260000, -113.5230000, EmotionalStates.SURPRISE, "followedUser", "followedMood"));
        followRef.document().set(new FollowRequest("sbaghel", "followedUser", "following"));

        // User Login
        scenario.getScenario().onActivity(activity -> activity.setCurrentUser(testUser));
        scenario.getScenario().onActivity(activity -> activity.successful_login());

        // Location Permission
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.executeShellCommand("pm grant " + InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName() + " android.permission.ACCESS_FINE_LOCATION");
        device.executeShellCommand("appops set " + InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName() + " android:mock_location allow");
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
    public void testMapFragmentLoads() throws InterruptedException{
        // Give time for the login to process
        Thread.sleep(2000);
        // Go to maps fragment
        onView(withId(R.id.Map)).perform(click());
        // Give time for the map Fragment  to show up
        Thread.sleep(2000);

        // Check the map view is displayed
        onView(withId(R.id.map)).check(matches(isDisplayed()));

        // Set mockLocation
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.addTestProvider("test", false, false, false, false, true, true, true, 1, 1);
        locationManager.setTestProviderEnabled("test", true);

        // Create a mock location to ensure the blue dot appears
        Location mockLoc = new Location("test");
        mockLoc.setLatitude(53.5259343);
        mockLoc.setLongitude(-113.5234554);
        mockLoc.setTime(System.currentTimeMillis());
        mockLoc.setAccuracy(1.0f);

        // Returns the current time elapsed since boot in nanoseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mockLoc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        Thread.sleep(5000);
        locationManager.setTestProviderLocation("test", mockLoc);

        // Wait for location update
        Thread.sleep(5000);

        // Check the map view is displayed
        onView(withId(R.id.map)).check(matches(isDisplayed()));
        // Verify markers are displayed
        onView(withContentDescription("My Location")).check(matches(isDisplayed()));
    }

    @Test
    public void testLocationUpdates() throws InterruptedException {
        // Go to maps fragment
        onView(withId(R.id.Map)).perform(click());
        // Give time for the map Fragment  to show up
        Thread.sleep(2000);
        // Check the map view is displayed
        onView(withId(R.id.map)).check(matches(isDisplayed()));
        // Set mock location
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.addTestProvider("test", false, false, false, false, true, true, true, 1, 1);
        locationManager.setTestProviderEnabled("test", true);

        // Create a mock location to ensure the blue dot appears
        Location mockLoc = new Location("test");
        mockLoc.setLatitude(53.5259343);
        mockLoc.setLongitude(-113.5234554);
        mockLoc.setTime(System.currentTimeMillis());
        mockLoc.setAccuracy(1.0f);

        // Returns the current time elapsed since boot in nanoseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mockLoc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        // Set mock test provider
        locationManager.setTestProviderLocation("test", mockLoc);
        // Wait for location update
        Thread.sleep(5000);

        // Update to new location
        Location updatedLoc = new Location("test");
        updatedLoc.setLatitude(53.5379343);
        updatedLoc.setLongitude(-113.5234554);
        updatedLoc.setTime(System.currentTimeMillis());
        updatedLoc.setAccuracy(1.0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            updatedLoc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        // Set mock test provider
        locationManager.setTestProviderLocation("test", updatedLoc);
        Thread.sleep(3000);

        // Check the map view is displayed
        onView(withId(R.id.map)).check(matches(isDisplayed()));
        // Verify markers are displayed
        onView(withContentDescription("My Location")).check(matches(isDisplayed()));
    }

    @Test
    public void testFilter5KmRadiusFollowingButtonClick() throws InterruptedException {
        // Go to maps fragment
        onView(withId(R.id.Map)).perform(click());
        Thread.sleep(2000);

        // Switch to Following mode
        onView(withId(R.id.display_toggle)).perform(click());
        Thread.sleep(2000);

        // Set mock location
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.addTestProvider("test", false, false, false, false, true, true, true, 1, 1);
        locationManager.setTestProviderEnabled("test", true);

        // Create a mock location to ensure the blue dot appears
        Location mockLoc = new Location("test");
        mockLoc.setLatitude(53.526000);
        mockLoc.setLongitude(-113.523000);
        mockLoc.setTime(System.currentTimeMillis());
        mockLoc.setAccuracy(1.0f);

        // Returns the current time elapsed since boot in nanoseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mockLoc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        // Set mock test provider
        locationManager.setTestProviderLocation("test", mockLoc);
        // Wait for location update
        Thread.sleep(5000);

        // Apply 5 km filter
        onView(withId(R.id.filter_5_km_radius)).perform(click());
        Thread.sleep(3000);

        // Verify circle by checking button state and map visibility
        onView(withId(R.id.filter_5_km_radius)).check(matches(isSelected()));
        onView(withId(R.id.filter_mood_following)).check(matches(not(isEnabled())));
        onView(withId(R.id.filter_mood_history)).check(matches(not(isEnabled())));
        onView(withId(R.id.map)).check(matches(isDisplayed()));
    }
}
