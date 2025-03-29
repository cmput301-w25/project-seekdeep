package com.example.project_seekdeep;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.Process;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


// This testing file is taken from Seth's Lab-08 code
// Taken by: Deryk Fong
// Taken on: March 28, 2025
@RunWith(MockitoJUnitRunner.class)
public class UserProviderTest {
    @Mock
    private FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockFollowingsList;
    @Mock
    private DocumentReference mockDocReference;
    private UserProvider userProvider;
    @Mock
    private Context mockContext;
    @Mock
    private Process mockProcess;

    private UserProfile testUser1 = new UserProfile("follower", "Does he even read our tests?");
    private UserProfile testUser2 = new UserProfile("followee", "Probably does right?");

    @Before
    public void setUp() {
        // Start up mocks
        MockitoAnnotations.openMocks(this);
        // Define the behaviour we want during our tests. This part is what avoids the calls to firestore.
        when(mockFirestore.collection("followings_and_requests")).thenReturn(mockFollowingsList);
        when(mockFollowingsList.document()).thenReturn(mockDocReference);
        when(mockFollowingsList.document(anyString())).thenReturn(mockDocReference);
        when(Process.myPid()).thenReturn(10000);
        // make sure we have a fresh instance for each test
        MoodProvider.setInstanceForTesting(mockFirestore);
        userProvider = UserProvider.getInstance(mockContext, testUser2);

    }


    @Test
    public void testSendFollowRequestToDataBase() {
        FollowRequest frq = new FollowRequest(testUser1.getUsername(), testUser2.getUsername());
        // add the request
        userProvider.sendFollowRequestToDataBase(frq);

        // verify that the add method did in fact add a follow request to the collection
        verify(mockDocReference).set(frq);
    }

    @Test
    public void testAcceptFollowRequest() {
        FollowRequest frq = new FollowRequest(testUser1.getUsername(), testUser2.getUsername());
        // add the request
        userProvider.sendFollowRequestToDataBase(frq);

        userProvider.acceptFollowRequest(testUser1);
        verify(mockDocReference).update("status", "following");
    }
    @Test
    public void testDeclineFollowRequest() {
        FollowRequest frq = new FollowRequest(testUser1.getUsername(), testUser2.getUsername());
        // add the request
        userProvider.sendFollowRequestToDataBase(frq);

        userProvider.declineFollowRequest(testUser1);
        verify(mockDocReference).delete();
    }
}
