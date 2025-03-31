package com.example.project_seekdeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private Button lastSelectedButton = null; // Track the last selected button (used for animating the pop-ups)

    public interface MoodSelectionListener {
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
        addButtonAnimation(view.findViewById(R.id.buttonSurprise), EmotionalStates.SURPRISE, "SURPRISED");
        addButtonAnimation(view.findViewById(R.id.buttonShame), EmotionalStates.SHAME, "SHAME");
        addButtonAnimation(view.findViewById(R.id.buttonSad), EmotionalStates.SADNESS, "SAD");
        addButtonAnimation(view.findViewById(R.id.buttonHappy), EmotionalStates.HAPPINESS, "HAPPY");
        addButtonAnimation(view.findViewById(R.id.buttonFear), EmotionalStates.FEAR, "FEAR");
        addButtonAnimation(view.findViewById(R.id.buttonDisgusted), EmotionalStates.DISGUST, "DISGUSTED");
        addButtonAnimation(view.findViewById(R.id.buttonConfused), EmotionalStates.CONFUSION, "CONFUSED");
        addButtonAnimation(view.findViewById(R.id.buttonAngry), EmotionalStates.ANGER, "ANGRY");

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("Select", (dialog, which) -> {
            if (listener != null) {
                listener.moodHasBeenSelected(mood);  //the listener notifies parent frag (since they implement the listener)
                //dialog closes automatically
            }
        });

        return builder.create(); // Return the built dialog

    }

    /**
     * This method sets an OnClickListener on a button,
     *  and makes the currently selected emotion-button on the mood wheel pop-up on the screen.
     * @param button This is the button that will be 'listened' to for clicks.
     * @param moodState This is the EmotionState of the button.
     * @param moodText This is the actual text that will display in the middle of the wheel (to tell users what they've selected)
     */
    private void addButtonAnimation(Button button, EmotionalStates moodState, String moodText) {
        button.setOnClickListener(v -> {
            // If there was a previously selected button, reset its scale back to normal
            if (lastSelectedButton != null && lastSelectedButton != button) {
                lastSelectedButton.animate().scaleX(1f).scaleY(1f).setDuration(200);
            }

            // Animate the current button to pop up and stay popped up
            v.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200);

            // Update UI elements
            selectedMood.setText(moodText);
            selectedMood.setTextColor(Color.parseColor(moodState.getColour()));
            mood = moodState;

            // Update the last selected button to be the current button
            lastSelectedButton = button;
        });
    }





}
