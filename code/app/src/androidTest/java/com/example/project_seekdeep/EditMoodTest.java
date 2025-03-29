package com.example.project_seekdeep;

import static org.junit.Assert.assertEquals;


import static java.util.Objects.*;

import android.net.Uri;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


//I couldn't find a way to do this test using mockito/ unit test and it doesn't run in the other folder
//so here we are (Nancy)

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EditMoodTest {
    // borrows code from Jachelle's tests and the Lab 7
    private UserProfile testUser = new UserProfile("jshello", "tofu123");
    private MoodProvider moodProvider;

    Mood mood1 = new Mood(testUser, EmotionalStates.ANGER);
    Mood mood2 = new Mood(testUser, EmotionalStates.SADNESS);

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        int porNumberStorage = 9199;

        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);

        FirebaseStorage.getInstance().useEmulator(androidLocalhost, porNumberStorage);
    }
    @Before
    public void seedDatabase() {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodsRef = db.collection("MoodDB");

        moodProvider = MoodProvider.getInstance(db);

        moodProvider.addMoodEvent(mood1);
        moodProvider.addMoodEvent(mood2);

    }


    @Test
    public void testUpdateMoodReason(){
        DocumentReference mood1DocRef = mood1.getDocRef();
        mood1.setReason("Edit reason");
        moodProvider.updateMood(mood1);

        Query MoodsQuery = FirebaseFirestore.getInstance().collection("MoodDB").whereEqualTo(
                "docRef", mood1DocRef.toString() );

        MoodsQuery.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {

                for (QueryDocumentSnapshot snapshot : value) {
                    HashMap<String, Object> ownerSnapshot = (HashMap<String, Object>) snapshot.getData().get("owner");
                    UserProfile user = new UserProfile( ownerSnapshot.get("username").toString(),
                            ownerSnapshot.get("password").toString());
                    EmotionalStates emotionalState = EmotionalStates.valueOf((String)snapshot.get("emotionalState"));
                    SocialSituations socialSituation = SocialSituations.valueOf((String) snapshot.get("socialSituation"));

                    List<String> followers = (List<String>) snapshot.get("followers");
                    Date postedDate = requireNonNull(snapshot.getTimestamp("postedDate")).toDate();
                    String reason = (String) snapshot.get("reason");

                    String imageStr = (String) snapshot.get("image");
                    Uri image = null;
                    if (imageStr != null){
                        image = Uri.parse(imageStr);
                    }

                    Mood mood = new Mood(user, emotionalState, socialSituation, followers, postedDate, reason);

                    mood.setImage(image);
                    mood.setDocRef(snapshot.getReference());

                    assertEquals(mood1, mood);

                }

            }
        });
    }

    @Test
    public void testUpdateMoodEmotionalState(){
        DocumentReference mood1DocRef = mood1.getDocRef();
        mood1.setEmotionalState(EmotionalStates.FEAR);
        moodProvider.updateMood(mood1);

        Query MoodsQuery = FirebaseFirestore.getInstance().collection("MoodDB").whereEqualTo(
                "docRef", mood1DocRef.toString() );

        MoodsQuery.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {

                for (QueryDocumentSnapshot snapshot : value) {
                    HashMap<String, Object> ownerSnapshot = (HashMap<String, Object>) snapshot.getData().get("owner");
                    UserProfile user = new UserProfile( ownerSnapshot.get("username").toString(),
                            ownerSnapshot.get("password").toString());
                    EmotionalStates emotionalState = EmotionalStates.valueOf((String)snapshot.get("emotionalState"));
                    SocialSituations socialSituation = SocialSituations.valueOf((String) snapshot.get("socialSituation"));

                    List<String> followers = (List<String>) snapshot.get("followers");
                    Date postedDate = requireNonNull(snapshot.getTimestamp("postedDate")).toDate();
                    String reason = (String) snapshot.get("reason");

                    String imageStr = (String) snapshot.get("image");
                    Uri image = null;
                    if (imageStr != null){
                        image = Uri.parse(imageStr);
                    }

                    Mood mood = new Mood(user, emotionalState, socialSituation, followers, postedDate, reason);

                    mood.setImage(image);
                    mood.setDocRef(snapshot.getReference());

                    assertEquals(mood1, mood);

                }

            }
        });
    }

    @Test
    public void testUpdateMoodSocialSituation(){
        DocumentReference mood1DocRef = mood1.getDocRef();
        mood1.setSocialSituation(SocialSituations.ALONE);
        moodProvider.updateMood(mood1);

        Query MoodsQuery = FirebaseFirestore.getInstance().collection("MoodDB").whereEqualTo(
                "docRef", mood1DocRef.toString() );

        MoodsQuery.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {

                for (QueryDocumentSnapshot snapshot : value) {
                    HashMap<String, Object> ownerSnapshot = (HashMap<String, Object>) snapshot.getData().get("owner");
                    UserProfile user = new UserProfile( ownerSnapshot.get("username").toString(),
                            ownerSnapshot.get("password").toString());
                    EmotionalStates emotionalState = EmotionalStates.valueOf((String)snapshot.get("emotionalState"));
                    SocialSituations socialSituation = SocialSituations.valueOf((String) snapshot.get("socialSituation"));

                    List<String> followers = (List<String>) snapshot.get("followers");
                    Date postedDate = requireNonNull(snapshot.getTimestamp("postedDate")).toDate();
                    String reason = (String) snapshot.get("reason");

                    String imageStr = (String) snapshot.get("image");
                    Uri image = null;
                    if (imageStr != null){
                        image = Uri.parse(imageStr);
                    }

                    Mood mood = new Mood(user, emotionalState, socialSituation, followers, postedDate, reason);

                    mood.setImage(image);
                    mood.setDocRef(snapshot.getReference());

                    assertEquals(mood1, mood);

                }

            }
        });
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
