package com.example.project_seekdeep;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.UUID;

/**
 * This Fragment class is designed to display a Mood Event Card, and let users Create a mood.
 * Current working feature:
 *      - lets a user insert an image
 *      - user can select their mood
 * Fixes to implement:
 *      - if the user does not want to select an image, then must get rid of that imageView UI element (using a new xml file?)
 */
//Image Picker code adapted from: https://medium.com/@everydayprogrammer/implement-android-photo-picker-in-android-studio-3562a85c85f1

public class CreateMoodEventFragment extends Fragment implements SelectMoodDialogFragment.MoodSelectionListener {
    //ATTRIBUTES:
    private ImageView uploadImageHere;  //this imageView is set to be clickable
    private Button createConfirmButton;
    private Mood moodEvent = new Mood();
    private Uri imageUri; //this is where selected image is assigned
    private MoodProvider moodProvider;
    //Attributes for selecting a mood:
    TextView clickToSelectMood; //this textView is set to clickable

    //Constructor to create the fragment
    public CreateMoodEventFragment() {
        super(R.layout.frag_create_mood_event);
    }

    /*
    US 02.02.01 : As a participant, I want to express the reason why for a mood event using a
    photograph.
    Subissue #114-Limit user such that the user can only add a single to photo for a mood event.

    Implemented by: the PickVisualMedia() activity result contract only allows users to pick one image from their gallery
    */

    //This launches the chosen image into the imageView
    ActivityResultLauncher<PickVisualMediaRequest> launcher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri imageUri) {
            if (imageUri == null) {
                Toast.makeText(requireContext(), "No image Selected", Toast.LENGTH_SHORT).show();
            } else {
                //Load the image into the mood event card (UI component)
                Glide.with(requireContext()).load(imageUri).into(uploadImageHere);

                //Call a method that'll upload the image to Firebase
                uploadImageToFirebase(imageUri);
            }
        }
    });

    private void uploadImageToFirebase(Uri selectedImage) {
        //Initialize a MoodProvider object and pass in the firebase
        moodProvider = MoodProvider.getInstance(FirebaseFirestore.getInstance());
        moodProvider.addMoodEvent(this.moodEvent);
        //Get a reference to the firebase storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference temp = storageRef.child(selectedImage.getEncodedPath());
        /*TO DO:
        - create a reference to the image URI
        - assign it to the image field in the mood event
        - Have to use Firebase storage to store photos
         */

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Initialize the image UI element
        uploadImageHere = view.findViewById(R.id.image);
        //Initialize selectMood to UI element
        clickToSelectMood = view.findViewById(R.id.edit_emotion_editText);
        createConfirmButton = view.findViewById(R.id.confirm_create_button);
        createConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moodProvider.addMoodEvent(moodEvent); //add the mood to the database
            }
        });
        //Set a listener for when the imageView is clicked on
        uploadImageHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //launch the gallery
                launcher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        //Set listener for the select_mood is clicked on -> open the mood wheel in a dialog fragment:
        clickToSelectMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open the mood wheel dialog
                SelectMoodDialogFragment newDialog = new SelectMoodDialogFragment();
                newDialog.show(getChildFragmentManager(), "SelectMoodDialog");
            }
        });
    }

    //Must implement this listener method from the SelectMoodDialogFragment
    @Override
    public void moodHasBeenSelected(EmotionalStates mood) {
        this.moodEvent.setEmotionalState(mood);
        clickToSelectMood.setText(mood.toString());
    }
}
