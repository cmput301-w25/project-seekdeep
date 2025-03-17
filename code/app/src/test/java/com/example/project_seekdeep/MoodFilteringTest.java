package com.example.project_seekdeep;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

public class MoodFilteringTest {
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockMoodCollection;
    @Mock
    private DocumentReference mockDocReference;
    private UserProfile testUser = new UserProfile("jshello", "tofu123");

    @Before
    public void setUp() {
        // Start up mocks
        MockitoAnnotations.openMocks(this);
        // Define the behaviour we want during our tests. This part is what avoids the calls to firestore.
        when(mockFirestore.collection("MoodDB")).thenReturn(mockMoodCollection);
        when(mockMoodCollection.document()).thenReturn(mockDocReference);
        when(mockMoodCollection.document(anyString())).thenReturn(mockDocReference);
    }

    @Test
    public void testSortReverseChronological() throws InterruptedException {
        Mood mood1 = new Mood(testUser, EmotionalStates.ANGER);
        // give time before the next mood is created so that they can be sorted
        Thread.sleep(5000);
        Mood mood2 = new Mood(testUser, EmotionalStates.SHAME);
        Thread.sleep(5000);
        Mood mood3 = new Mood(testUser, EmotionalStates.SHAME);

        // ddd moods to an ArrayList
        ArrayList<Mood> moods = new ArrayList<>();
        moods.add(mood1);
        moods.add(mood2);
        moods.add(mood3);

        // test the .sortReverseChronological method
        MoodFiltering.sortReverseChronological(moods);

        // assert to see if the moods are actually in reverse chronological order
        assertTrue(moods.get(0).getPostedDate().after(moods.get(1).getPostedDate()));
        assertTrue(moods.get(1).getPostedDate().after(moods.get(2).getPostedDate()));
    }
}
