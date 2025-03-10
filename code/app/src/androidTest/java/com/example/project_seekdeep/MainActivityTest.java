package com.example.project_seekdeep;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;

import static org.hamcrest.CoreMatchers.not;

import static org.hamcrest.Matchers.allOf;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    /**
     * Test that BottomNavigationView is initially hidden before login.
     */
    @Test
    public void testBottomNavigationIsHiddenInitially() {
        onView(withId(R.id.bottomNavigationView)).check(matches(not(isDisplayed())));
    }

    /**
     * Test that after a successful login, the navigation bar appears.
     */
    @Test
    public void testNavigationBarAppearsAfterLogin() {
        scenario.getScenario().onActivity(activity -> activity.successful_login());

        // Check if navigation bar is now visible
        onView(withId(R.id.bottomNavigationView)).check(matches(isDisplayed()));
    }

    // Not functional at the moment
//    /**
//     * Test if clicking on navigation bar buttons switches fragments.
//     */
//    @Test
//    public void testNavigationFragmentSwitching() {
//        scenario.getScenario().onActivity(activity -> activity.successful_login());
//
//        // First, we should be on the Feed fragment after successful login
//        onView(withId(R.id.frameLayout)).check(matches(isDisplayed()));
//
//
//    }
}
