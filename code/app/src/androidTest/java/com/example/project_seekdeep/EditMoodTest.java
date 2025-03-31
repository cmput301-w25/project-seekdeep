package com.example.project_seekdeep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static java.util.Objects.requireNonNull;

import android.content.Context;
import android.net.Uri;
import android.os.FileUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.project_seekdeep.Helpers.EmotionalStates;
import com.example.project_seekdeep.Helpers.ImageProvider;
import com.example.project_seekdeep.Helpers.SocialSituations;
import com.example.project_seekdeep.Helpers.UserProfile;
import com.example.project_seekdeep.Moods.Mood;
import com.example.project_seekdeep.Moods.MoodProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    ImageProvider imageProvider;

    Mood mood1 = new Mood(testUser, EmotionalStates.ANGER);

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
    public void seedDatabase() throws InterruptedException {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference moodsRef = db.collection("MoodDB");

        moodProvider = MoodProvider.getInstance(db);
        imageProvider = ImageProvider.getInstance(FirebaseStorage.getInstance());

        //Add document reference and "null" social situation
        DocumentReference documentReference = moodsRef.document();
        mood1.setDocRef(documentReference);
        mood1.setSocialSituation(SocialSituations.TITLE);
        moodProvider.updateMood(mood1);

        Thread.sleep(1000);

    }


    @Test
    public void testUpdateMoodReason() throws InterruptedException {

        //edit mood

        DocumentReference mood1DocRef = mood1.getDocRef();
        Log.d("except nancy", "Nancy doc ref: "+mood1DocRef.getPath());
        mood1.setReason("Edit reason");
        moodProvider.updateMood(mood1);

        Thread.sleep(1000);

        //check database for edited mood

        mood1DocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {

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

                        assertEquals(mood1.getReason(), mood.getReason());

                    } else {
                        Log.d("exception", "no such document found");
                        fail();
                    }
                } else {
                    //  failed
                    Log.d("exception", "get failed with ", task.getException());
                    fail();
                }
            }
        });


    }

    @Test
    public void testUpdateMoodEmotionalState() throws InterruptedException {

        //edit mood
        DocumentReference mood1DocRef = mood1.getDocRef();
        mood1.setEmotionalState(EmotionalStates.DISGUST);
        moodProvider.updateMood(mood1);

        Thread.sleep(1000);

        //check database for edited mood

        mood1DocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {

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

                        assertEquals(mood1.getEmotionalState(), mood.getEmotionalState());

                    } else {
                        Log.d("exception", "no such document found");
                        fail();
                    }
                } else {
                    //  failed
                    Log.d("exception", "get failed with ", task.getException());
                    fail();
                }
            }
        });
    }

    @Test
    public void testUpdateMoodSocialSituation() throws InterruptedException {
        //edit mood
        DocumentReference mood1DocRef = mood1.getDocRef();
        mood1.setSocialSituation(SocialSituations.ALONE);
        moodProvider.updateMood(mood1);

        Thread.sleep(1000);

        //check database for edited mood

        mood1DocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {

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

                        assertEquals(mood1.getSocialSituation(), mood.getSocialSituation());

                    } else {
                        Log.d("exception", "no such document found");
                        fail();
                    }
                } else {
                    //  failed
                    Log.d("exception", "get failed with ", task.getException());
                    fail();
                }
            }
        });
    }


    @Test
    public void testUpdateMoodImage() throws InterruptedException {

        //edit the mood

        DocumentReference mood1DocRef = mood1.getDocRef();
        Uri uri = Uri.parse("123");
        mood1.setImage(uri);
        moodProvider.updateMood(mood1);
        Thread.sleep(1000);

        // get the mood from database and check

        mood1DocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {

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

                        assertEquals(mood1.getImage(), mood.getImage());

                    } else {
                        Log.d("exception", "no such document:" + mood1DocRef.getPath() + " found");
                        fail();
                    }
                } else {
                    //  failed
                    Log.d("exception", "get failed with ", task.getException());
                    fail();
                }
            }
        });

    }

    @Test
    public void testUploadImageStorageCheck() throws InterruptedException, IOException {

        final Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        File file1 = File.createTempFile("123", "jfif");
        Thread.sleep(1000);

        InputStream ims = context.getAssets().open("togepi.jfif");
        Thread.sleep(1000);

        FileOutputStream fos = new FileOutputStream(file1);
        FileUtils.copy (ims, fos);
        Thread.sleep(1000);

        fos.close();
        Thread.sleep(1000);

        Uri uploadUri = Uri.fromFile(file1);
        imageProvider.uploadImageToFirebase(uploadUri);
        Thread.sleep(1000);

        StorageReference imageFire = imageProvider.getStorageRefFromLastPathSeg(uploadUri.getLastPathSegment());
        imageFire.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // idk how to teardown storage so I'll delete it from here
                imageFire.delete();
                return;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                fail();
            }
        });



    }




    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(10000);
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
