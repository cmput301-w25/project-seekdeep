package com.example.project_seekdeep;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import com.google.android.gms.tasks.Continuation;


// This testing file is taken from Seth's Lab-08 code
// Taken by: Jachelle Chan
// Taken on: March 14, 2025
public class MoodProviderTest {
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockMoodCollection;
    @Mock
    private DocumentReference mockDocReference;
    @Mock
    private Task<Void> mockSetTask;     // Mock for the set operation
    @Mock
    private Task<DocumentReference> mockContinueWithTask;   // Mock for the continueWith result

    private MoodProvider moodProvider;
    private UserProfile testUser = new UserProfile("jshello", "tofu123");

    @Before
    public void setUp() {
        // Start up mocks
        MockitoAnnotations.openMocks(this);
        // Define the behaviour we want during our tests. This part is what avoids the calls to firestore.
        when(mockFirestore.collection("MoodDB")).thenReturn(mockMoodCollection);
        when(mockMoodCollection.document()).thenReturn(mockDocReference);
        when(mockMoodCollection.document(anyString())).thenReturn(mockDocReference);

        // Mock the set operation to return Task<Void>
        when(mockDocReference.set(any())).thenReturn(mockSetTask);
        // Mock the continueWith to return Task<DocumentReference> using thenAnswer
        when(mockSetTask.continueWith(any(Continuation.class))).thenAnswer(invocation -> {
            return mockContinueWithTask;
        });

        // make sure we have a fresh instance for each test
        MoodProvider.setInstanceForTesting(mockFirestore);
        moodProvider = MoodProvider.getInstance(mockFirestore);

    }

    @Test
    public void testAddMood() {
        Mood mood = new Mood(testUser, EmotionalStates.ANGER);
        // add the mood
        moodProvider.addMoodEvent(mood);

        // verify that the add method did in fact add a mood to the collection
        verify(mockDocReference).set(mood);
    }

    @Test
    public void testDeleteMood() {
        // create mood
        Mood mood = new Mood(testUser, EmotionalStates.ANGER);
        mood.setDocRef(mockDocReference);
        // Call the delete mood and verify the firebase delete method was called.
        moodProvider.deleteMood(mood);
        verify(mockDocReference).delete();
    }
}
