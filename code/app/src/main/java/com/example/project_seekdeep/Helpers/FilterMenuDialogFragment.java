package com.example.project_seekdeep.Helpers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.project_seekdeep.R;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class displays the filtering menu for Feed, History, and Profile.
 *
 * All 3 pages display the same options (mood and timeline),
 *      except that Profile and Feed has no option to see "the 3 most recent mood events of the participants I am granted to follow".
 *
 * Note: Multiple mood filters can be selected. But only one timeline filter can be selected
 *       If a user chooses a combination of moods and a timeline filter, they must all be applied.
 * @author Sarah Chang and Jachelle Chan
 */
public class FilterMenuDialogFragment extends DialogFragment {
    //Attributes
    private ArrayList<EmotionalStates> selectedStates = new ArrayList<>();
    private OnFilterSelectedListener listener;
    private String selectedTimeline = "";
    private List<String> keywords = new ArrayList<>();

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof FilterMenuDialogFragment.OnFilterSelectedListener) {   //the parent fragment is CreateMoodEventFragment
            listener = (FilterMenuDialogFragment.OnFilterSelectedListener) getParentFragment();
            // add an else if statement for the other pages
        } else {
            throw new RuntimeException("Parent fragment did not implement OnFilterSelectedListener!!");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //Initialize the view and builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.filter_menu,null);
        builder.setView(view);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // set up listeners for each individual chip in the chip group
        // check if it's checked so that we can add or remove them from the array list
        //User can select multiple moods to filter by
        Chip surpriseChip = view.findViewById(R.id.surprise_chip);
        surpriseChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                selectedStates.add(EmotionalStates.SURPRISE);
            }
            else selectedStates.remove(EmotionalStates.SURPRISE);
        });
        Chip angerChip = view.findViewById(R.id.anger_chip);
        angerChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                selectedStates.add(EmotionalStates.ANGER);
            }
            else selectedStates.remove(EmotionalStates.ANGER);
        });
        Chip confusionChip = view.findViewById(R.id.confusion_chip);
        confusionChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                selectedStates.add(EmotionalStates.CONFUSION);
            }
            else selectedStates.remove(EmotionalStates.CONFUSION);
        });
        Chip disgustChip = view.findViewById(R.id.disgust_chip);
        disgustChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if(isChecked) {
                selectedStates.add(EmotionalStates.DISGUST);
            }
            else selectedStates.remove(EmotionalStates.DISGUST);
        });
        Chip fearChip = view.findViewById(R.id.fear_chip);
        fearChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if (isChecked) {
                selectedStates.add(EmotionalStates.FEAR);
            }
            else selectedStates.remove(EmotionalStates.FEAR);
        });
        Chip happyChip = view.findViewById(R.id.happiness_chip);
        happyChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if(isChecked) {
                selectedStates.add(EmotionalStates.HAPPINESS);
            }
            else selectedStates.remove(EmotionalStates.HAPPINESS);
        });
        Chip sadChip = view.findViewById(R.id.sadness_chip);
        sadChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if(isChecked) {
                selectedStates.add(EmotionalStates.SADNESS);
            }
            else selectedStates.remove(EmotionalStates.SADNESS);
        });
        Chip shameChip = view.findViewById(R.id.shame_chip);
        shameChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if(isChecked) {
                selectedStates.add(EmotionalStates.SHAME);
            }
            else selectedStates.remove(EmotionalStates.SADNESS);
        });

        //User can only select 1 timeline filter (either "Past week" or "Last 3 from all users" )
        Chip recentChip = view.findViewById(R.id.recent_week_chip);
        recentChip.setOnCheckedChangeListener((chip, isChecked) -> {
            if(isChecked) {
                selectedTimeline = "recent";
            }
            else {
                selectedTimeline = "";
            }
        });

        Chip last3chip = view.findViewById(R.id.last_3_chip);
        last3chip.setOnCheckedChangeListener((chip, isChecked) -> {
            if(isChecked) {
                selectedTimeline = "last3";  // or something like that
            }
            else {
                selectedTimeline = "";
            }
        });

        //Adjust filtering menu if on profile OR feed page
        hideChipsAndLabels(view);

        Button applyFiltersButton = view.findViewById(R.id.apply_filters_button);
        //applyFiltersButton.setOnClickListener(v -> applyFilters());
        applyFiltersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText keywordTextView = view.findViewById(R.id.dialog_keyword_search);
                // this use of .asList is taken from https://stackoverflow.com/a/7488710
                // Author: colinD
                // Taken by: Jachelle Chan
                // Taken on: March 29, 2025
                keywords = Arrays.asList(keywordTextView.getText().toString().split(" "));
                applyFilters();
            }
        });

        Button resetFiltersButton = view.findViewById(R.id.reset_filters_button);
        resetFiltersButton.setOnClickListener(v -> resetFilters());
        return builder.create();
    }

    /**
     * This interface allows for FilterMenuDialogFragment to interact with the parent fragment it's attached to.
     * You will need to implement your own interface for this because every fragment has different needs.
     */
    public interface OnFilterSelectedListener {
        void onFiltersApplied(ArrayList<EmotionalStates> selectedMoods, String selectedTimeline, List<String> keywords);
        void onFiltersReset();
    }

    /**
     * This method is used to hide a chip or label if the user is on certain pages depending on the tag attached
     * @param view: The view the user is on.
     */
    public void hideChipsAndLabels(View view) {
        //If on profile/feed page, hide last_3_chip from the filtering menu:
        //can use:  view.findViewById(R.id.last_3_chip).setVisibility(View.GONE);
        if(Objects.equals(getTag(), "profile")) {
            view.findViewById(R.id.last_3_chip).setVisibility(View.GONE);
        }
        // edit the else as you see fit
        else {
            view.findViewById(R.id.keyword_label).setVisibility(View.GONE);
            view.findViewById(R.id.dialog_keyword_search).setVisibility(View.GONE);
        }
    }

    /**
     * This method sends the information of the filters applied to the fragment it was called from.
     */
    private void applyFilters() {
        // send the selected filters back to the parent fragment
        if (listener != null) {
            listener.onFiltersApplied(selectedStates, selectedTimeline, keywords);
        }
        dismiss(); // close the dialog after applying the filters
    }

    /**
     * This method sends to the fragment it was called from that the user wants to reset all filters
     */
    private void resetFilters() {
        if (listener != null) {
            listener.onFiltersReset();
        }
        dismiss();
    }
}
