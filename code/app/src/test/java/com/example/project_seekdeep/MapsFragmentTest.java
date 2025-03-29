package com.example.project_seekdeep;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MapsFragmentTest {
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockFollowingsCollection;
    @Mock
    private DocumentReference mockDocRef;

    @Before
    public void setUp() {
        // Start up mocks
        MockitoAnnotations.openMocks(this);
    }
}
