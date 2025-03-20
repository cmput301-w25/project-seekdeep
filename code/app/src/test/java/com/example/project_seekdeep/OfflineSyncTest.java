package com.example.project_seekdeep;

import static android.content.Context.WIFI_SERVICE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.util.Log;
import android.net.wifi.WifiManager;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
// This testing file is taken from Seth's Lab-08 code
// Taken by: Deryk Fong
// Taken on: March 20, 2025
public class OfflineSyncTest {
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockMoodCollection;
    @Mock
    private DocumentReference mockDocReference;
    private MoodProvider moodProvider;
    private WifiManager wifiManager;
    private UserProfile testUser = new UserProfile("pivner", "abcdefg4321");

    @Before
    public void setUp() {
        // Start up mocks
        MockitoAnnotations.openMocks(this);
        // Define the behaviour we want during our tests.
        try{
            wifiManager=(WifiManager) ApplicationProvider.getApplicationContext().getSystemService(WIFI_SERVICE);
        } catch(Exception e){
            //TODO: add catch for finding exception
        }
        when(mockFirestore.collection("MoodDB")).thenReturn(mockMoodCollection);
        when(mockMoodCollection.document()).thenReturn(mockDocReference);
        when(mockMoodCollection.document(anyString())).thenReturn(mockDocReference);

        // make sure we have a fresh instance for each test
        MoodProvider.setInstanceForTesting(mockFirestore);
        moodProvider = MoodProvider.getInstance(mockFirestore);
    }

    @Test
    public void testAddMood() throws InterruptedException {
        try {
            wifiManager.setWifiEnabled(false);
        } catch(Exception e){
            //TODO:Add catch for not setting wifi as false
        }
        Mood mood = new Mood(testUser, EmotionalStates.ANGER);
        // add the mood
        moodProvider.addMoodEvent(mood);

        // verify that the add method did in fact add a mood to the collection
        try {
            wifiManager.setWifiEnabled(true);
        } catch(Exception e){
            //TODO:Add catch for not setting wifi as false
        }
        Thread.sleep(2000);
        verify(mockDocReference).set(mood);
    }

    @Test
    public void testDeleteMood() throws InterruptedException {
        try {
            wifiManager.setWifiEnabled(false);
        } catch(Exception e){
            //TODO:Add catch for not setting wifi as false
        }
        // create mood
        Mood mood = new Mood(testUser, EmotionalStates.ANGER);
        mood.setDocRef(mockDocReference);
        // Call the delete mood and verify the firebase delete method was called.
        moodProvider.deleteMood(mood);
        try {
            wifiManager.setWifiEnabled(true);
        } catch(Exception e){
            //TODO:Add catch for not setting wifi as false
        }
        Thread.sleep(2000);
        verify(mockDocReference).delete();
    }

}
