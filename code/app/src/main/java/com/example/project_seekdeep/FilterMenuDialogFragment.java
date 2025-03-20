package com.example.project_seekdeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
    private ChipGroup moodFiltersChipGroup; //User can select multiple moods to filter by
    private ChipGroup timelineChipGroup; //User can only select 1 timeline filter (either "Past week" or "Last 3 from all users" )
    private ArrayList<Integer> selectedMoodsId = new ArrayList<>();
    private ArrayList<EmotionalStates> selectedStates = new ArrayList<>();
    private int selectedTimelineId;
    private OnFilterSelectedListener listener;
    private String selectedTimeline = "";


    // This constructor is taken from https://stackoverflow.com/a/15459259
    // Author: JafarKhQ
    // Taken by: Jachelle Chan
    // Taken on: March 19, 2025
    /*
    public static FilterMenuDialogFragment newInstance(ArrayList<Integer> selectedChipIds) {

        FilterMenuDialogFragment fragment = new FilterMenuDialogFragment();
        Bundle args = new Bundle();
        args.putIntegerArrayList("chip ids", selectedChipIds);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedMoodsId = getArguments().getIntegerArrayList("chip ids");
        }
    }*/
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
/*
        // restore the checked chips if the user reopens the dialog fragment
        moodFiltersChipGroup = view.findViewById(R.id.mood_chip_group);
        if(!selectedMoodsId.isEmpty()) {
            for (int chipId : selectedMoodsId) {
                Chip chip = view.findViewById(chipId);
                if (chip != null) {
                    chip.setChecked(true);
                }
            }
        }

        // add back the states into selectedStates array based on the ids when given to the fragment
        moodFiltersChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedStates.clear();
            for (int id : checkedIds) {
                EmotionalStates state = EmotionalStates.mapChipToState(id);
                if (state != null) {
                    selectedStates.add(state);
                }
            }
        });*/

        // set up listeners for each individual chip in the chip group
        // check if it's checked so that we can add or remove them from the array list
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
                selectedTimeline = "last 3";  // or something like that
            }
            else {
                selectedTimeline = "";
            }
        });
        timelineChipGroup = view.findViewById(R.id.timeline_chip_group);

        //Adjust filtering menu if on profile OR feed page
        hideLastThreeFilterChip(view);

        Button applyFiltersButton = view.findViewById(R.id.apply_filters_button);
        applyFiltersButton.setOnClickListener(v -> applyFilters());

        Button resetFiltersButton = view.findViewById(R.id.reset_filters_button);
        resetFiltersButton.setOnClickListener(v -> resetFilters());

        //TO DO: Apply both moods and timeline filters on the current list of moods events (either in Feed, Following, or Profile)
        //Use filtering functions, from another class?


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return builder.create();
    }

    /**
     * This interface allows for FilterMenuDialogFragment to interact with the parent fragment it's attached to
     */
    public interface OnFilterSelectedListener {
        void onFiltersApplied(ArrayList<EmotionalStates> selectedMoods, String selectedTimeline, ArrayList<Integer> selectedMoodIds);
        void onFiltersReset();
    }


    public void hideLastThreeFilterChip(View view) {
        //TO DO: Check if you are in profile page OR the feed page, if yes, then hide the "Last 3 from all users" chip , because not applicable to either pages
        // IF THIS DOES NOT WORK, THEN I WILL JUST CREATE TWO SEPARATE XML FILES FOR FILTERING

        //If on profile/feed page, hide last_3_chip from the filtering menu:
        //can use:  view.findViewById(R.id.last_3_chip).setVisibility(View.GONE);
        if(Objects.equals(getTag(), "profile")) {
            view.findViewById(R.id.last_3_chip).setVisibility(View.GONE);
        }
    }

    private void applyFilters() {
        // send the selected filters back to the parent fragment
        if (listener != null) {
            //selectedMoodsId.clear();
            //selectedMoodsId.addAll(moodFiltersChipGroup.getCheckedChipIds());
             Log.d("jshello", selectedMoodsId.toString());
             Log.d("jshello", selectedStates.toString());
            listener.onFiltersApplied(selectedStates, selectedTimeline, selectedMoodsId);
        }
        dismiss(); // close the dialog after applying the filters
    }
    private void resetFilters() {
        if (listener != null) {
            listener.onFiltersReset();
        }
        dismiss();
    }
}
