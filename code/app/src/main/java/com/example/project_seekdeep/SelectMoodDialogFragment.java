package com.example.project_seekdeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * This class extends DialogFragment, and will display the mood wheel when a user wants to create or edit a mood event.
 * It is implemented as a child fragment of the CreateMoodEventFragment.
 */
public class SelectMoodDialogFragment extends DialogFragment {

    TextView selectedMood; //displays the currently selected mood in the middle of wheel
    EmotionalStates mood; //this is the mood that will be passed back to CreateMoodEventFragment

    /**
     * Interface for the fragment
     */
    public interface MoodSelectionListener {
        /**
         * interface method
         * @param mood
         *      emotional state to look at
         */
        void moodHasBeenSelected(EmotionalStates mood);
    }

    private MoodSelectionListener listener;

    // Attach the listener (Make sure the parent fragment implements it)
    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof MoodSelectionListener) {   //the parent fragment is CreateMoodEventFragment
            listener = (MoodSelectionListener) getParentFragment();
        } else {
            throw new RuntimeException("Parent fragment did not implement MoodSelectionListener!!");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

        // Inflate custom mood selection layout
        View view = getLayoutInflater().inflate(R.layout.mood_wheel, null);
        builder.setView(view);

        // Handle mood selection logic here (e.g., find buttons inside mood_wheel and set click listeners):

        //Initialize the TextView to the UI element (to show the currently selected text
        //In case users are not sure what each emoji represents, they can read this text.
        selectedMood = view.findViewById(R.id.currently_selected_mood);

        //Define actions for each mood button
        view.findViewById(R.id.buttonSurprise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //filled in later
                selectedMood.setText("SURPRISED");
                mood = EmotionalStates.SURPRISE;
//                fragManager.beginTransaction().replace(R.id.fragment_container, new CreateMoodEventFragment()).commit();
            }
        });
        view.findViewById(R.id.buttonShame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("SHAME");
                mood = EmotionalStates.SHAME;
            }
        });
        view.findViewById(R.id.buttonSad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("SAD");
                mood = EmotionalStates.SADNESS;
            }
        });
        view.findViewById(R.id.buttonHappy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("HAPPY");
                mood = EmotionalStates.HAPPINESS;
            }
        });
        view.findViewById(R.id.buttonFear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("FEAR");
                mood = EmotionalStates.FEAR;
            }
        });
        view.findViewById(R.id.buttonDisgusted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("DISGUSTED");
                mood = EmotionalStates.DISGUST;
            }
        });
        view.findViewById(R.id.buttonConfused).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("CONFUSED");
                mood = EmotionalStates.CONFUSION;
            }
        });
        view.findViewById(R.id.buttonAngry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("ANGRY");
                mood = EmotionalStates.ANGER;
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Select", (dialog, which) -> {
            if (listener != null) {
                listener.moodHasBeenSelected(mood);  //the listener notifies parent frag (since they implement the listener)
                //dialog closes automatically
            }
        });

        return builder.create(); // Return the built dialog

    }
}
