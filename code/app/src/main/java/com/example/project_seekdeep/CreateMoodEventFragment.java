package com.example.project_seekdeep;

import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;

/**
 * This Fragment class is designed to display a Mood Event Card, and let users Create a mood.
 * When the user clicks to select an emotion, a SelectMoodDialogFragment will pop up.
 * @author Sarah Chang, Nancy Lin, modified by Saurabh S. Baghel
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
    private TextView cancelButton;
    private TextView char_count;
    private Switch privacySwitch;
    private TextView explainPrivacy;
    private Spinner socialSitSpinner;
    private ImageView uploadImageHere;  //this imageView is set to be clickable
    private Button createConfirmButton;
    private EditText reasonEditText;

    private Mood moodEvent;
    private Uri imageUri; //this is where selected image is assigned
    private ImageProvider imageProvider;
    private MoodProvider moodProvider;
    //Attributes for selecting a mood:
    TextView clickToSelectMood; //this textView is set to clickable

    //Attributes for saving the current location while creatng mood
    private Switch locationToggle;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;

    //Attributes for the User
    private UserProfile userProfile;
    private EmotionalStates selectedEmotion;
    private String socialSit;


    /**
     * Constructor to create the fragment
     */
    public CreateMoodEventFragment() {
        super(R.layout.frag_create_mood_event);
        imageProvider = ImageProvider.getInstance(FirebaseStorage.getInstance());
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

            }

        }
    });



    /**
     * When this view is created, it will collect all the fields from the UI to create a new mood
     * Fields collected: EmotionalState, SocialSituation, and reason
     * @param view The View returned.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cancelButton = view.findViewById(R.id.cancel_button);

        //initialize char_count
        char_count = view.findViewById(R.id.char_count);

        privacySwitch = view.findViewById(R.id.privacy_switch);
        explainPrivacy = view.findViewById(R.id.explain_privacy);

        //Initialize an instance of movieProvide (so can add new mood to firestore)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        moodProvider = MoodProvider.getInstance(db);

        //Initialize the EditText where user inputs reason
        reasonEditText = view.findViewById(R.id.edit_reason);

        //Initialize the image UI element
        uploadImageHere = view.findViewById(R.id.image);
        //Initialize selectMood to UI element
        clickToSelectMood = view.findViewById(R.id.edit_emotion_editText);
        createConfirmButton = view.findViewById(R.id.confirm_create_button);
        //Social Sit Spinner:
        socialSitSpinner = view.findViewById(R.id.social_situation_spinner);
        socialSitSpinner.setAdapter(new ArrayAdapter<SocialSituations>(getContext(), android.R.layout.simple_spinner_item, SocialSituations.values()));

        //Initialize the SocialSituations socialSit
        socialSit = SocialSituations.TITLE.toString();

        // Initialize location UI switch
        locationToggle = view.findViewById(R.id.switch1);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());

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

        // Update charCount as user types their Reason
        // resource used: https://stackoverflow.com/questions/3013791/live-character-count-for-edittext
        final TextWatcher txtWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //exculde spaces from char_count
                String exclude_spaces = charSequence.toString().replaceAll("[\\s\\n]","");
                char_count.setText(String.valueOf(exclude_spaces.length()));
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        };
        reasonEditText.addTextChangedListener(txtWatcher);


        uploadImageHere.setImageResource(R.drawable.twotone_add_photo_alternate_24);

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

        //PRIVACY (default set to OFF=private, because you might upload something and regret it)
        privacySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int drawable = isChecked ? R.drawable.public_symbol : R.drawable.private_symbol;
            privacySwitch.setCompoundDrawablesWithIntrinsicBounds(drawable,0,0,0);

            explainPrivacy.setText(isChecked ? R.string.public_mode : R.string.private_mode);
        });

//        // https://developer.android.com/training/data-storage/shared-preferences#java
//        // Restore current switch state from SharedPreferences so that it is consistent with MapsFragment
//        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LocationPref", Context.MODE_PRIVATE);
        locationToggle.setChecked(false);           // Initially set it as off, cause if user wants to save location then only save location
        locationToggle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.location_off, 0, 0, 0);

        // Handle location permission request
        requestLocationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(requireContext(), "Location permission granted", Toast.LENGTH_SHORT).show();
                        locationToggle.setChecked(true);
                    } else {
                        Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                        locationToggle.setChecked(false);
                    }
                }
        );

        // Set the listener for the location toggle switch
        locationToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Update drawable
            int drawable = isChecked ? R.drawable.location_on : R.drawable.location_off;
            locationToggle.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0);
            if (isChecked) {
                // If toggle is enabled, and permission not granted then ask for permission
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }
        });

        //Set a listener for the "Create" button. This will create a new Mood object
        createConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //get the fields and create a new Mood object
                //Ensure that Emotional State has been chosen
                if (selectedEmotion == null) {
                    clickToSelectMood.setError("Emotional state is required!");
                    return;
                }

                // US 02.01.01 - I want to express the reason why for a mood is happening (<=200 chars).
                // check if a reason was inputed
                if (reasonEditText != null) {
                    String reason = reasonEditText.getText().toString().trim();
                    if (!validReasonLength()) {
                        reasonEditText.setError("Reason must be ≤ 200 characters");
                        return; //this stops execution for the rest of this click method
                    }

                    /*
                    Issue solved:
                    - in this constructor: SocialSit can't be null, so temp hardcoded to the TITLE value.

                     TO DO: SocialSituation might need a NULL field
                     */
                    moodEvent = new Mood(userProfile, selectedEmotion, new String[] {reason, socialSit.toString()} );
                }

                //Default: create Mood object with only the UserProfile and EmotionalState
                else {
                    moodEvent = new Mood(userProfile, selectedEmotion, new String[] {null, socialSit.toString()});
                }


                if (imageUri != null) {
                    imageProvider.uploadImageToFirebase(imageUri);
                }
                // Upload to firebase a simplified image Uri
                moodEvent.setImage(Uri.parse(imageUri.getLastPathSegment()));
                //Upload the new Mood to firebase
                moodProvider.addMoodEvent(moodEvent).addOnSuccessListener(documentReference -> {
                    String moodEventId = documentReference.getId();
                    moodEvent.setDocRef(documentReference);
                    if (locationToggle.isChecked() &&
                            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        saveLocationToFirestore(moodEventId);          // Pass the ID to saveLocationToFirestore
                        Toast.makeText(requireContext(), "Location saved", Toast.LENGTH_SHORT).show();
                    }

                });

                Toast.makeText(requireContext(), "Your mood has been uploaded!", Toast.LENGTH_SHORT).show();

                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Just return to whatever screen the user was previously on
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    /**
     * This method checks whether a reason is <=200 characters
     * @return true if the reason is ≤ 200 chars, false otherwise
     */
    public boolean validReasonLength() {
        String charCountText = char_count.getText().toString();
        return Integer.parseInt(charCountText) <= 200;
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

    /**
     * Function saves the user's current location to Firestore Database in the locations collection
     * The location is associated with the specific mood event and uses FusedLocationProviderClient to fetch the current oocation
     */
    private void saveLocationToFirestore(String moodEventID) {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Get the current location
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            // Create the location attributes
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            EmotionalStates emotionalState = moodEvent.getEmotionalState();
                            UserProfile currentUserProfile = ((MainActivity) requireActivity()).getCurrentUsername();
                            String userName = currentUserProfile.getUsername();
                            UserLocation userLocation = new UserLocation(latitude, longitude, emotionalState, userName, moodEventID);
                            // Save location to Firestore under "location" collection
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("locations").add(userLocation);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error to fetch location", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(requireContext(), "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }
}
