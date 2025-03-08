package com.example.project_seekdeep;


import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This fragment class is designed to display a list of posted moods by a given user.
 * @author Kevin Tu
 */

public class MoodHistoryFragment extends Fragment {
    // TODO: We need to figure out how to store userID to be used as primary key to access entry in the database.
    private UserProfile user;
    private FirebaseFirestore db;
    private CollectionReference users;
    private CollectionReference moods;
    private ArrayList<Mood> moodsList;


    public MoodHistoryFragment() {
        super(R.layout.layout_feed);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set views
        ListView moodListView = view.findViewById(R.id.mood_list);

        // Get current user that is currently logged in
        user =  (UserProfile) requireArguments().getSerializable("user");

        // Instantiate database for usage
        db = FirebaseFirestore.getInstance();
        users = db.collection("users");
        moods = db.collection("MoodDB");

        // Add event listeners to collections
        moods.addSnapshotListener((value, error) -> {
            moodsList.clear();
            for (QueryDocumentSnapshot snapshot : value) {
                String trigger =
            }
        });

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

        ArrayList<Mood> moodArrayList = new ArrayList<>(Arrays.asList(dummyMoods));

        ArrayAdapter<Mood> moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList);
        moodListView.setAdapter(moodArrayAdapter);
    }


    private List<Mood> getUserMoods() {

    }

}