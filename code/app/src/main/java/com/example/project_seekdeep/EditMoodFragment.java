package com.example.project_seekdeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.util.Dictionary;

/**
 * This class is used for editing moods
 *
 * @author Jachelle Chan, modified by Nancy Lin
 * reuses code from Lab 7
 */
public class EditMoodFragment extends DialogFragment {
    private EditText editReason;
    private Spinner emotionSpinner;
    private Spinner socialSituationSpinner;
    private ImageView imageView;
    private Uri imageUri;
    private MoodProvider moodProvider = MoodProvider.getInstance(FirebaseFirestore.getInstance());

    private FirebaseStorage storage;
    private StorageReference storageRef;

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



    //This launches the chosen image into the imageView
    //Taken from createMoodEventFragment
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
                    Glide.with(requireContext()).load(imageUriActivty).into(imageView);
                }

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

        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        View view = getLayoutInflater().inflate(R.layout.fragment_edit_mood_details, null);
        editReason = view.findViewById(R.id.edit_reason);
        emotionSpinner = view.findViewById(R.id.emotion_spinner);
        socialSituationSpinner = view.findViewById(R.id.social_situation_spinner);
        imageView = view.findViewById(R.id.image);
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


            //todo add edit image functionality


            // if image DNE, then hide the image view?
            if (mood.getImage() != null){
                imageView.setVisibility(View.VISIBLE);


                StorageReference imageFire = storage.getReference("Images/" +
                        mood.getImage().getLastPathSegment());

                Log.d("NANCY", "Non null image fire/ " + imageFire);

                imageFire.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // Got the download URL for 'users/me/profile.png'
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
        }
        else {
            mood = null;
        }





        //Set a listener for when the imageView is clicked on
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //launch the gallery
                launcher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });


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

                if (imageUri != null) {
                    uploadImageToFirebase(imageUri);
                }
                mood.setImage(imageUri);

                mood.setReason(reason);
                mood.setEmotionalState(emotionalStates);
                mood.setSocialSituation(socialSituations);

                //todo add edit image functionality

                moodProvider.updateMood(mood);

                dialog.dismiss();


                // the only required thing is emotional state i believe
            });
        });
        return dialog;
    }
}