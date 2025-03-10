package com.example.project_seekdeep;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FeedFragment extends Fragment {

    private ListView moodListView;

    private ArrayList<Mood> moodArrayList;
    private ArrayAdapter<Mood> moodArrayAdapter;

    public FeedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.layout_feed, container, false);
        return inflatedView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set views
        moodListView = view.findViewById(R.id.list_view_mood);

        //create dummy array data
        Mood[] dummyMoods ={
                new Mood("User1", EmotionalStates.ANGER,
                        SocialSituations.ALONE, "What the trigger"),

                new Mood("User2", EmotionalStates.CONFUSION,
                        SocialSituations.SEVERAL_PEOPLE, "What the trigger"),

                new Mood("User3", EmotionalStates.DISGUST,
                        SocialSituations.CROWD, "What the trigger"),

                new Mood("User4", EmotionalStates.FEAR,
                        SocialSituations.WITH_ANOTHER, "What the trigger"),

                new Mood("User5", EmotionalStates.HAPPINESS,
                        SocialSituations.ALONE, "What the trigger"),

                new Mood("User6", EmotionalStates.SADNESS,
                        SocialSituations.SEVERAL_PEOPLE, "What the trigger"),

                new Mood("User7", EmotionalStates.SHAME,
                        SocialSituations.SEVERAL_PEOPLE, "What the trigger"),

                new Mood("User8", EmotionalStates.SURPRISE,
                        SocialSituations.ALONE, "What the trigger")
        };

        //create mood array

        moodArrayList = new ArrayList<>();
        moodArrayList.addAll(Arrays.asList(dummyMoods));


        moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList);
        moodListView.setAdapter(moodArrayAdapter);
    }

}