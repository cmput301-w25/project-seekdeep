package com.example.project_seekdeep;


import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This fragment class is designed to display a list of posted moods by a given user.
 * @author Kevin Tu
 */

public class MoodHistoryFragment extends Fragment {

    private ListView moodListView;

    private ArrayList<Mood> moodArrayList;
    private ArrayAdapter<Mood> moodArrayAdapter;

    public MoodHistoryFragment() {
        super(R.layout.layout_feed);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set views
        moodListView = view.findViewById(R.id.mood_list);

        //create dummy array data
        Mood[] dummyMoods ={
                new Mood(new UserProfile("User1", "pass1"), EmotionalState.ANGER,
                        "some social situation", "What the trigger"),

                new Mood(new UserProfile("User1", "pass2"), EmotionalState.CONFUSION,
                        "some social situation", "What the trigger"),

                new Mood(new UserProfile("User1", "pass1"), EmotionalState.DISGUST,
                        "some social situation", "What the trigger"),

                new Mood(new UserProfile("User1", "pass2"), EmotionalState.FEAR,
                        "some social situation", "What the trigger"),

        };

        //get mood from specific user


        //create mood array

        moodArrayList = new ArrayList<>();
        moodArrayList.addAll(Arrays.asList(dummyMoods));


        moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList);
        moodListView.setAdapter(moodArrayAdapter);
    }



}