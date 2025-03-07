package com.example.project_seekdeep;

import android.content.Context;
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
 * Use the {@link TestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TestFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView moodListView;

    private ArrayList<Mood> moodArrayList;
    private ArrayAdapter<Mood> moodArrayAdapter;

    public TestFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TestFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TestFragment newInstance(String param1, String param2) {
        TestFragment fragment = new TestFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.layout_feed, container, false);



        //copy the code and edit from lab 5 and etc.

        //set views
        //moodListView = inflatedView.findViewById(R.id.mood_list);

        //create dummy array data

        /*
        Mood[] dummyMoods ={
            new Mood(new UserProfile("User1", "pass1"), EmotionalState.ANGER,
                    "some social situation", "What the trigger"),

            new Mood(new UserProfile("User2", "pass2"), EmotionalState.CONFUSION,
                        "some social situation", "What the trigger"),

            new Mood(new UserProfile("User1", "pass1"), EmotionalState.DISGUST,
                        "some social situation", "What the trigger"),

            new Mood(new UserProfile("User2", "pass2"), EmotionalState.FEAR,
                        "some social situation", "What the trigger"),

            new Mood(new UserProfile("User1", "pass1"), EmotionalState.HAPPINESS,
                        "some social situation", "What the trigger"),

            new Mood(new UserProfile("User2", "pass2"), EmotionalState.SADNESS,
                        "some social situation", "What the trigger"),

            new Mood(new UserProfile("User1", "pass1"), EmotionalState.SHAME,
                        "some social situation", "What the trigger"),

            new Mood(new UserProfile("User2", "pass2"), EmotionalState.SURPRISE,
                        "some social situation", "What the trigger"),
        };



        moodArrayList = new ArrayList<>();
        moodArrayList.addAll(Arrays.asList(dummyMoods));


        moodArrayAdapter = new MoodArrayAdapter(getActivity()., moodArrayList);
        moodListView.setAdapter(moodArrayAdapter);
         */


        return inflatedView;
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

                new Mood(new UserProfile("User2", "pass2"), EmotionalState.CONFUSION,
                        "some social situation", "What the trigger"),

                new Mood(new UserProfile("User3", "pass1"), EmotionalState.DISGUST,
                        "some social situation", "What the trigger"),

                new Mood(new UserProfile("User4", "pass2"), EmotionalState.FEAR,
                        "some social situation", "What the trigger"),

                new Mood(new UserProfile("User5", "pass1"), EmotionalState.HAPPINESS,
                        "some social situation", "What the trigger"),

                new Mood(new UserProfile("User6", "pass2"), EmotionalState.SADNESS,
                        "some social situation", "What the trigger"),

                new Mood(new UserProfile("User7", "pass1"), EmotionalState.SHAME,
                        "some social situation", "What the trigger"),

                new Mood(new UserProfile("User8", "pass2"), EmotionalState.SURPRISE,
                        "some social situation", "What the trigger")
        };

        //create mood array

        moodArrayList = new ArrayList<>();
        moodArrayList.addAll(Arrays.asList(dummyMoods));


        moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList);
        moodListView.setAdapter(moodArrayAdapter);
    }

    //@Override
    //public void onAttach(@NonNull Context context) {
    //    super.onAttach(context);
    //}
}