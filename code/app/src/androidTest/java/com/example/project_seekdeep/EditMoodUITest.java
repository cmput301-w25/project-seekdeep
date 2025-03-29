package com.example.project_seekdeep;

import android.net.Uri;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


// This doesn't work
// maybe ill do it later
// maybe it should be repurposed
public class EditMoodUITest {
    // borrows code from Jachelle's tests and the Labs
    private UserProfile testUser = new UserProfile("jshello", "tofu123");


    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }
    @Before
    public void seedDatabase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodsRef = db.collection("Moods");

        EmotionalStates emotionalState = EmotionalStates.ANGER;
        SocialSituations socialSituation = null;
        List<String> followers = new ArrayList<>();
        Date postedDate = new Date();
        String reason = null;
        Uri image = null;


        DocumentReference moodDocRef1 = moodsRef.document();
        DocumentReference moodDocRef2 = moodsRef.document();

        Mood mood1 = new Mood(testUser, EmotionalStates.ANGER, socialSituation, followers, postedDate, reason);
        Mood mood2 = new Mood(testUser, EmotionalStates.SADNESS, socialSituation, followers, postedDate, reason);
        mood1.setDocRef(moodDocRef1);
        mood2.setDocRef(moodDocRef2);

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

}
