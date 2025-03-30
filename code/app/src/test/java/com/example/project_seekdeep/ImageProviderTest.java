package com.example.project_seekdeep;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

import android.net.Uri;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Objects;


// This testing file is taken from Jachelle Chan's MoodProvider code Seth's Lab-08 code
// Taken by: NANCY lin
// Taken on: March 26, 2025


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({Uri.class})
public class ImageProviderTest {
    @Mock
    public FirebaseFirestore mockFirestore;
    @Mock
    private CollectionReference mockMoodCollection;
    @Mock
    private DocumentReference mockDocReference;

    @Mock
    private FirebaseStorage mockStorage;
    @Mock
    private StorageReference mockImagesReference;
    @Mock
    private StorageReference mockSelectedImageReference;

    @Mock
    private Uri mockUploadUri;

    @Mock
    private Uri mockStorageUri;

    @Mock
    private UploadTask mockUploadTask;



    private MoodProvider moodProvider;

    private ImageProvider imageProvider;
    private UserProfile testUser = new UserProfile("jshello", "tofu123");


    //@PrepareForTest({Uri.class})
    @Before
    public void setUp() throws Exception {
        // Start up mocks
        MockitoAnnotations.openMocks(this);
        // Define the behaviour we want during our tests. This part is what avoids the calls to firestore.
        when(mockFirestore.collection("MoodDB")).thenReturn(mockMoodCollection);
        when(mockMoodCollection.document()).thenReturn(mockDocReference);
        when(mockMoodCollection.document(anyString())).thenReturn(mockDocReference);

        when(mockStorage.getReference()).thenReturn(mockImagesReference);
        when(mockStorage.getReference().child("Images/")).thenReturn(mockImagesReference);
        //when(mockImagesReference.child( anyString() ).thenReturn(mockSelectedImageReference);

        PowerMockito.mockStatic(Uri.class);
        mockUploadUri = mock(Uri.class);
        PowerMockito.when(Uri.class, "parse", anyString()).thenReturn(mockUploadUri);

        //when(Objects.requireNonNull(mockUploadUri.getLastPathSegment())).thenReturn("123");
        when(mockUploadUri.getLastPathSegment()).thenReturn("123");
        when(mockImagesReference.child(any())).thenReturn(mockSelectedImageReference);
        when(mockSelectedImageReference.putFile(any())).thenReturn(mockUploadTask);



        // make sure we have a fresh instance for each test
        MoodProvider.setInstanceForTesting(mockFirestore);
        moodProvider = MoodProvider.getInstance(mockFirestore);
        imageProvider = ImageProvider.getInstance(mockStorage);

        //imageUri = Uri.parse("123");

    }


    //@PrepareForTest({Uri.class})
    @Test
    public void testUploadImage(){

        imageProvider.uploadImageToFirebase(mockUploadUri);
        verify(mockSelectedImageReference).putFile(mockUploadUri);
        //assertEquals(1, 0);



    }


}
