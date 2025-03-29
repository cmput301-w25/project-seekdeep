package com.example.project_seekdeep;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MapsFragmentUITest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Test
    public void testMapFragmentLoads() {

    }
    @Test
    public void mapDisplaysCurrentUsersMoods(){

    }
    @Test
    public void testFilterMoodHistoryButtonClick() {

    }

    @Test
    public void testFilterFollowingButtonShowsToast() {
    }

    @Test
    public void test5KmRadiusButtonShowsToast() {
    }

}
