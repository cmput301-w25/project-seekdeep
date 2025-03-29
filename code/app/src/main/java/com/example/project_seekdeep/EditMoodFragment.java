package com.example.project_seekdeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.FileNotFoundException;
import java.util.Dictionary;

/**
 * This class is used for editing moods
 *
 * @author Jachelle Chan, modified by Nancy Lin, Saurabh
 * reuses code from Lab 7
 */
public class EditMoodFragment extends DialogFragment {
    private EditText editReason;
    private Spinner emotionSpinner;
    private Spinner socialSituationSpinner;
    private ImageView imageView;
    private Uri imageUri;
    private MoodProvider moodProvider = MoodProvider.getInstance(FirebaseFirestore.getInstance());
    private ImageProvider imageProvider = ImageProvider.getInstance(FirebaseStorage.getInstance());
    private TextView char_count;
    private Switch locationToggle;
    private Switch privacySwitch;
    private TextView explainPrivacy;


    //** btw i used seth's lab-07 code for this **//

    /**
     * Instance constructor for EditMoodFragment
     * @param mood
     * @return EditMoodFragment fragment
     */
    public static EditMoodFragment newInstance(Mood mood) {
        Bundle args = new Bundle();
        args.putSerializable("Mood", mood);

        EditMoodFragment fragment = new EditMoodFragment();
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Creates a dialog that manages editing an existing mood
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return dialog
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Log.d("NANCY", "Did we get here?");

        View view = getLayoutInflater().inflate(R.layout.fragment_edit_mood_details, null);
        editReason = view.findViewById(R.id.edit_reason);
        char_count = view.findViewById(R.id.char_count);
        emotionSpinner = view.findViewById(R.id.emotion_spinner);
        socialSituationSpinner = view.findViewById(R.id.social_situation_spinner);
        imageView = view.findViewById(R.id.image);
        locationToggle = view.findViewById(R.id.switch1);
        privacySwitch = view.findViewById(R.id.privacy_switch);
        explainPrivacy = view.findViewById(R.id.explain_privacy);

        emotionSpinner.setAdapter(new ArrayAdapter<EmotionalStates>(getContext(), android.R.layout.simple_spinner_item, EmotionalStates.values()));
        socialSituationSpinner.setAdapter(new ArrayAdapter<SocialSituations>(getContext(), android.R.layout.simple_spinner_item, SocialSituations.values()));

        String tag = getTag();
        Bundle bundle = getArguments();
        Mood mood;

        if (tag != null && tag.equals("Mood Details") && bundle != null){
            mood = (Mood) bundle.getSerializable("Mood");
            editReason.setText(mood.getReason());
            emotionSpinner.setSelection(mood.getEmotionalState().ordinal());
            socialSituationSpinner.setSelection(mood.getSocialSituation().ordinal());


            // Load the image from the mood into the dialog if it exists
            imageUri = mood.getImage();
            if (imageUri != null){
                StorageReference imageFire = imageProvider.getStorageRefFromLastPathSeg(
                        imageUri.getLastPathSegment());

                imageFire.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide.with(getContext())
                                .load(uri)
                                .into(imageView);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });
            }
            //set an on click listener for the image
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //launch the gallery
                    launcher.launch(new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build());
                }
            });

        }
        else {
            mood = null;
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
        editReason.addTextChangedListener(txtWatcher);

      //THIS CONTROLS THE TOGGLES FOR GEOLOCATION AND PRIVACY:
        // Set location toggle by checking if a location exists in the locations collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("locations")
                .whereEqualTo("moodID", mood.getDocRef().getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isLocation = !queryDocumentSnapshots.isEmpty();
                    locationToggle.setChecked(isLocation);
                    locationToggle.setCompoundDrawablesWithIntrinsicBounds(
                            isLocation ? R.drawable.location_on : R.drawable.location_off, 0, 0, 0);
                    locationToggle.setEnabled(false);
                });

        //PRIVACY (default set to OFF=private, because you might upload something and regret it)
        privacySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int drawable = isChecked ? R.drawable.public_symbol : R.drawable.private_symbol;
            privacySwitch.setCompoundDrawablesWithIntrinsicBounds(drawable,0,0,0);
            explainPrivacy.setText(isChecked ? R.string.public_mode : R.string.private_mode);
        });



        // Build the Dialog Fragment
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        AlertDialog dialog = builder
                .setView(view)
                .setTitle("Mood details")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Continue", null)
                .create();

        // Change dialog so it does not automatically dismiss, but only when valid data is entered
        dialog.setOnShowListener( d -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {


                String reason = editReason.getText().toString().trim();

                //Validate length of reason to <= 200 chars
                if (editReason != null) {
                    if (!validReasonLength()) {
                        editReason.setError("Reason must be ≤ 200 characters");
                        return;
                    }
                }
                
                String[] emoStateBreak = emotionSpinner.getSelectedItem().toString().split(" ");
                EmotionalStates emotionalStates = EmotionalStates.valueOf(
                        emoStateBreak[emoStateBreak.length -1].toUpperCase());
                
                String socSitString = socialSituationSpinner.getSelectedItem().toString();
                SocialSituations socialSituations = null;
                switch (socSitString){
                    case "Social Situations":
                        socialSituations = SocialSituations.TITLE;
                        break;
                    case "Alone":
                        socialSituations = SocialSituations.ALONE;
                        break;
                    case "With Another Person":
                        socialSituations = SocialSituations.WITH_ANOTHER;
                        break;
                    case "Two or Several People":
                        socialSituations = SocialSituations.SEVERAL_PEOPLE;
                        break;
                        
                    case "With a Crowd":
                        socialSituations = SocialSituations.CROWD;
                        break;

                }

                mood.setReason(reason);
                mood.setEmotionalState(emotionalStates);
                mood.setSocialSituation(socialSituations);

                // Upload to the MoodsCollection a simplified image Uri
                mood.setImage(Uri.parse(imageUri.getLastPathSegment()));
                if (imageUri != null){
                    imageProvider.uploadImageToFirebase(imageUri);
                }


                moodProvider.updateMood(mood);
                dialog.dismiss();

            });
        });
        return dialog;
    }

    /**
     * This method checks whether a reason is <=200 characters (excluding whitespaces)
     * @return true if the reason is ≤ 200 chars, false otherwise
     */
    public boolean validReasonLength() {
        String charCountText = char_count.getText().toString();
        return Integer.parseInt(charCountText) <= 200;
    }


    /**
     * launcher to select images
     * Loads the image into the mood event card (UI component)
     * Taken from Sarah's CreateMoodEventFragment
     */
    ActivityResultLauncher<PickVisualMediaRequest> launcher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri imageUriActivty) {
            if (imageUriActivty == null) {
                Toast.makeText(requireContext(), "No image Selected", Toast.LENGTH_SHORT).show();
            } else {
                //assign imageUri the Uri to save to the edited Mood
                imageUri = imageUriActivty;
                AssetFileDescriptor fileDescriptor = null;

                try {
                    fileDescriptor = getContext().getContentResolver().openAssetFileDescriptor(imageUri, "r");
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                long fileSize = fileDescriptor.getLength();
                if (fileSize > 65536) {
                    Toast.makeText(requireContext(), "Image too large, must be under 65536 bytes", Toast.LENGTH_SHORT).show();
                } else {
                    Glide.with(requireContext()).load(imageUriActivty).into(imageView);
                }

            }

        }
    });

}
