package com.example.project_seekdeep;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.internal.StorageReferenceUri;

import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.UUID;

/**
 * This Fragment class is designed to display a Mood Event Card, and let users Create a mood.
 * When the user clicks to select an emotion, a SelectMoodDialogFragment will pop up.
 * @author Sarah Chang, Nancy Lin
 */

/*
 * Current working features:
 *      - lets a user insert an image
 *      - user can select an emotion
 *      - a new Mood will be created and passed to ModdProvider, where it will be stored in firebase
 * TO DO:
 *      - if the user does not want to select an image, then must get rid of that imageView UI element (using a new xml file?)
 *      - need to pass in the UserProfile object to create a new Mood object
 *      - image needs to be uploaded to firebase
 *      - once a mood is made, return to whatever the user was previously doing
 *      - the uploadImageToFirebase should technically be implemented in MoodProvider
 */

//Resources:
//  Image Picker code adapted from: https://medium.com/@everydayprogrammer/implement-android-photo-picker-in-android-studio-3562a85c85f1

public class CreateMoodEventFragment extends Fragment implements SelectMoodDialogFragment.MoodSelectionListener {
    //ATTRIBUTES:
    private Spinner socialSitSpinner;
    private ImageView uploadImageHere;  //this imageView is set to be clickable
    private Button createConfirmButton;
    private EditText reasonEditText;
    private EditText triggerEditText;
    private Mood moodEvent;
    private Uri imageUri; //this is where selected image is assigned
    private MoodProvider moodProvider;
    //Attributes for selecting a mood:
    TextView clickToSelectMood; //this textView is set to clickable

    //Attributes for the User
    private UserProfile userProfile;
    private EmotionalStates selectedEmotion;
    private String socialSit;



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
        public void onActivityResult(Uri imageUriActivty) {
            if (imageUriActivty == null) {
                Toast.makeText(requireContext(), "No image Selected", Toast.LENGTH_SHORT).show();
            } else {
                //Load the image into the mood event card (UI component)
                imageUri = imageUriActivty;

                AssetFileDescriptor fileDescriptor = null;
                try {
                    fileDescriptor = getContext().getContentResolver().openAssetFileDescriptor(imageUri, "r");
                } catch (FileNotFoundException e) {
                    Log.d("NANCY", "check file zie error");
                    throw new RuntimeException(e);
                }
                long fileSize = fileDescriptor.getLength();

                if (fileSize > 65536) {
                    Toast.makeText(requireContext(), "Image too large, must be under 65536 bytes", Toast.LENGTH_SHORT).show();
                } else {
                    Glide.with(requireContext()).load(imageUriActivty).into(uploadImageHere);
                }

                Log.d("NANCY", "did the glide work at least? imguri" + imageUri.toString());
                //TO DO: Implement a method that'll upload the image to Firebase
                //uploadImageToFirebase(imageUri);
            }

        }
    });

    /**
     * This uploads the image that a user selects into Firebase Storage
     * @param selectedImage
     */
    private void uploadImageToFirebase(Uri selectedImage) {
        //Initialize a MoodProvider object and pass in the firebase

        Log.d("NANCY", "do we ever get here in uploadimage?");

        moodProvider = MoodProvider.getInstance(FirebaseFirestore.getInstance());

        //Get a reference to the firebase storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        //StorageReference temp = storageRef.child(selectedImage.getEncodedPath());

        StorageReference selectedImageRef = storageRef.child("Images/" + selectedImage.getLastPathSegment());
        UploadTask uploadTask = selectedImageRef.putFile(selectedImage);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d("nancy", "unsuccessful upload");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d("nancy", "working image upload");
            }
        });


    }

    /**
     * When this view is created, it will collect all the fields from the UI to create a new mood
     * Fields collected: EmotionalState, trigger, SocialSituation, and reason
     * @param view The View returned.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize an instance of movieProvide (so can add new mood to firestore)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        moodProvider = MoodProvider.getInstance(db);

        //Initialize the EditText where user inputs reason
        reasonEditText = view.findViewById(R.id.edit_reason);
        //Initialize the EditText for trigger
        triggerEditText = view.findViewById(R.id.edit_trigger);
        //Initialize the image UI element
        uploadImageHere = view.findViewById(R.id.image);
        //Initialize selectMood to UI element
        clickToSelectMood = view.findViewById(R.id.edit_emotion_editText);
        createConfirmButton = view.findViewById(R.id.confirm_create_button);
        //Social Sit Spinner:
        socialSitSpinner = view.findViewById(R.id.social_situation_spinner);
        socialSitSpinner.setAdapter(new ArrayAdapter<SocialSituations>(getContext(), android.R.layout.simple_spinner_item, SocialSituations.values()));

        //intialize the SocialSituations socialSit
        socialSit = SocialSituations.TITLE.toString();

        //Retrieve UserProfile from the bundle (to use inside this fragment)
        if (getArguments() != null) {
            //Retrieve UserProfile (must be casted), save it in userProfile class attribute
            userProfile = (UserProfile) getArguments().getSerializable("userProfile");
        }
        else {  //Throw error into logcat (use for debugging)
            String message = "UserProfile was not passed to CreateMoodEventFragment correctly!";
            Log.e("CreateMoodEventFragment", message);
            throw new NullPointerException(message);
        }


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


        //Set a listener for choosing a social situation
         socialSitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
             @Override
             public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                 //Get the selected social sit (how?)
                 socialSit = socialSitSpinner.getSelectedItem().toString();
                 //Display it on UI?
                 // Initialize the class attribute (will be used when in the Create button's listener)
                 //selectedSocialSit = socialSit;
             }
             @Override
             public void onNothingSelected(AdapterView<?> adapterView) {
                //social sit remains empty/null
             }
         });


        //Set a listener for the "Create" button. This will create a new Mood object
        createConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //get the fields and create a new Mood object
                //Get the trigger text from UI
                String trigger = triggerEditText.getText().toString().trim();

                // US 02.01.01 - I want to express the reason why for a mood is happening (no more than 20 characters or 3 words).
                // check if a reason was inputed
                if (reasonEditText != null) {
                    String reason = reasonEditText.getText().toString().trim();
                    if (!validReasonLength(reason)) {
                        reasonEditText.setError("Reason must be ≤ 20 chars or ≤ 3 words!");
                        return; //this stops execution for the rest of this click method
                    }

                    /*
                    Issue solved:
                    - in this constructor: SocialSit can't be null, so temp hardcoded to the TITLE value.
                    - In the constructor, trigger is just a string, so it can be null (errors shouldn't happen here)
                    - If trigger == null, getText() will just return ""
                     TO DO: SocialSituation might need a NULL field
                     */
                    moodEvent = new Mood(userProfile, selectedEmotion, new String[] {trigger, reason, socialSit.toString()} );
                }

                //Default: create Mood object with only the UserProfile and EmotionalState
                else {
                    moodEvent = new Mood(userProfile, selectedEmotion, new String[] {trigger, null, socialSit.toString()});
                }

                //Log.d("NANCY", imageUri.toString());

                if (imageUri != null) {
                    uploadImageToFirebase(imageUri);
                }
                moodEvent.setImage(imageUri);
                //Upload the new Mood to firebase
                moodProvider.addMoodEvent(moodEvent);

                Toast.makeText(requireContext(), "Your mood has been uploaded!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This checks whether a reason is under 20 characters or under 3 words
     * @param reason
     * @return true if the reason is ≤ 20 chars or ≤ 3 words, false otherwise
     */
    public boolean validReasonLength(String reason) {
        //throw new IllegalArgumentException("User's reason must be <=20 chars OR <=3 words!");
        return reason.length() <= 20 && reason.split(" ").length <= 3; //returns true or false

    }

    //Must implement this listener method from the SelectMoodDialogFragment
    /**
     * This updates the UI to display the selected mood from the mood wheel (in SelectMoodDialogFragment)
     * It also set the isnstance of CreateMoodEventFragment's selectedEmotion attribute to the selected mood,
     * which will be used in createConfirmButton.setOnClickListener
     * @param selectedMood
     *          This is the selected mood to be displayed and set to selectedEmotion
     */
    @Override
    public void moodHasBeenSelected(EmotionalStates selectedMood) {
        //Save selectedMood to this fragment-instance's selectedEmotion attribute
        this.selectedEmotion = selectedMood;
        //Update UI element to display the selected emotion
        clickToSelectMood.setText(selectedMood.toString());
    }
}
