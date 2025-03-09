package com.example.project_seekdeep;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

/**
 * This Fragment class is designed to display a Mood Event Card, and let users Create a mood.
 * Current working feature:
 *      - lets a user insert an image
 * Fixes to implement:
 *      - if the user does not want to select an image, then must get rid of that imageView UI element (using a new xml file?)
 */
//Image Picker code adapted from: https://medium.com/@everydayprogrammer/implement-android-photo-picker-in-android-studio-3562a85c85f1

public class CreateMoodEventFragment extends Fragment {
    //Attributes:
    private ImageView uploadImageHere;  //this imageView is set to be clickable
    private EmotionalStates mood;
    private Mood moodEvent;

    //This launches the chosen image into the imageView
    ActivityResultLauncher<PickVisualMediaRequest> launcher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri o) {
            if (o == null) {
                Toast.makeText(requireContext(), "No image Selected", Toast.LENGTH_SHORT).show();
            } else {
                Glide.with(requireContext()).load(o).into(uploadImageHere);
            }
        }
    });


    //Constructor to create the fragment
    public CreateMoodEventFragment(EmotionalStates mood) {

        super(R.layout.fragment_mood_details);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize moodEvent to a new Mood object (and pass in the selected mood)
        moodEvent = new Mood(mood);

        //Initialize the image UI element
        uploadImageHere = view.findViewById(R.id.image);

        //Set a listener for when the imageButton is clicked on
        uploadImageHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launcher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });
    }


}
