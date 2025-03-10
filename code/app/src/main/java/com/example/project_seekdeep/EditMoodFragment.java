package com.example.project_seekdeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Dictionary;

/**
 * This class is used for editing moods
 *
 * @author Jachelle Chan, modified by Nancy Lin
 * reuses code from Lab 7
 */
public class EditMoodFragment extends DialogFragment {
    private EditText editReason;
    private EditText editTrigger;
    private Spinner emotionSpinner;
    private Spinner socialSituationSpinner;
    private ImageView imageView;
    private MoodDialogListener listener;


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

     //please implement a MoodDialogListener and the rest, refer to lab-07

    /**
     * Modifies onAttach to add a listener that will handle editing moods
     * @param context
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MoodDialogListener) {
            listener = (MoodDialogListener) context;
        }
        else {

            throw new RuntimeException( context.toString() + " Implement listener");
        }
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
        editTrigger = view.findViewById(R.id.edit_trigger);
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
            editTrigger.setText(mood.getTrigger());

            // i think the spinners setting previous info may have to be implemented in a different way but that will be dealt with later
            emotionSpinner.setSelection(mood.getEmotionalState().ordinal());  // maybe like this? hard to test currently
            socialSituationSpinner.setSelection(mood.getSocialSituation().ordinal());


            //todo add edit image functionality
        }
        else {
            mood = null;
        }


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
                
                String trigger = editTrigger.getText().toString().trim();
                
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
                
              
                //Image image = imageView.getImageAlpha();

                mood.setReason(reason);
                mood.setTrigger(trigger);
                mood.setEmotionalState(emotionalStates);
                mood.setSocialSituation(socialSituations);

                //todo add edit image functionality

                listener.updateMood(mood);

                dialog.dismiss();


                // the spinners and image ...
                // the only required thing is emotional state i believe
            });
        });
        return dialog;
    }
}
