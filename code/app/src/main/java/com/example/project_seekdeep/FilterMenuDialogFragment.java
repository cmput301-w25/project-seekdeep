package com.example.project_seekdeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.chip.ChipGroup;

import java.util.List;

/**
 * This class displays the filtering menu for Feed, History, and Profile.
 *
 * All 3 pages display the same options (mood and timeline),
 *      except that Profile and Feed has no option to see "the 3 most recent mood events of the participants I am granted to follow".
 *
 * Note: Multiple mood filters can be selected. But only one timeline filter can be selected
 *       If a user chooses a combination of moods and a timeline filter, they must all be applied.
 */
public class FilterMenuDialogFragment extends DialogFragment {
    //Attributes
    ChipGroup moodFiltersChipGroup; //User can select multiple moods to filter by
    ChipGroup timelineChipGroup; //User can only select 1 timeline filter (either "Past week" or "Last 3 from all users" )
    List<Integer> selectedMoods;
    int selectedTimeline;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //Initialize the view and builder
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = getLayoutInflater().inflate(R.layout.filter_menu,null);
        builder.setView(view);

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////

        moodFiltersChipGroup = view.findViewById(R.id.mood_chip_group);
        timelineChipGroup = view.findViewById(R.id.timeline_chip_group);

        //Adjust filtering menu if on profile OR feed page
        hideLastThreeFilterChip(view);

        //Get a list of chip IDs that were selected for moods
        // (ie. [R.id.surprise_chip, R.id.happiness_chip],  [] if nothing selected )
        selectedMoods = moodFiltersChipGroup.getCheckedChipIds();

        //Get the chip IDs that were selected for timeline
        //  (ie. R.id.recent_week_chip,  View.NO_ID if nothing selected )
        selectedTimeline = timelineChipGroup.getCheckedChipId();


        //TO DO: Apply both moods and timeline filters on the current list of moods events (either in Feed, Following, or Profile)
        //Use filtering functions, from another class?


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        return builder.create();
    }


    public void hideLastThreeFilterChip(View view) {
        //TO DO: Check if you are in profile page OR the feed page, if yes, then hide the "Last 3 from all users" chip , because not applicable to either pages
        // IF THIS DOES NOT WORK, THEN I WILL JUST CREATE TWO SEPARATE XML FILES FOR FILTERING

        //If on profile/feed page, hide last_3_chip from the filtering menu:
        //can use:  view.findViewById(R.id.last_3_chip).setVisibility(View.GONE);
    }

}
