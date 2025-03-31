package com.example.project_seekdeep;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MapsFragmentTest {
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockFollowingsCollection;
    @Mock
    private CollectionReference mockLocationsCollection;
    @Mock
    private DocumentReference mockDocRef;
    @Mock
    private GoogleMap mockGoogleMap;
    @Mock
    private Query mockQuery;
    @Mock
    private QuerySnapshot mockQuerySnapshot;
    @Mock
    private QueryDocumentSnapshot mockDocSnapshot;
    @Mock
    private MainActivity mockActivity;
    private MapsFragment mapsFragment;

    @Before
    public void setUp() {
    }

}
