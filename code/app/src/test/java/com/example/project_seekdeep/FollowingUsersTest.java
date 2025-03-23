package com.example.project_seekdeep;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FollowingUsersTest {
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockFollowingsCollection;
    @Mock
    private DocumentReference mockDocRef;
    private UserProfile testUser1 = new UserProfile("testUser1", "12345678");
    private UserProfile testUser2 = new UserProfile("testUser2", "12345678");

    @Before
    public void setUp() {
        // Start up mocks
        MockitoAnnotations.openMocks(this);
        // Define the behaviour we want during our tests. This part is what avoids the calls to firestore.
        when(mockFirestore.collection("followings_and_requests")).thenReturn(mockFollowingsCollection);
        when(mockFollowingsCollection.document()).thenReturn(mockDocRef);
        when(mockFollowingsCollection.document(anyString())).thenReturn(mockDocRef);

//        // make sure we have a fresh instance for each test
//        MoodProvider.setInstanceForTesting(mockFirestore);
//        moodProvider = MoodProvider.getInstance(mockFirestore);

    }

//    @Test
//    public void testGoIntoProfile


}
