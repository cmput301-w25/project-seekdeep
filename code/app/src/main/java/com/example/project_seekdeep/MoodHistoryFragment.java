package com.example.project_seekdeep;


import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This fragment class is designed to display a list of posted moods by a given user.
 * @author Kevin Tu, Nancy Lin
 */

public class MoodHistoryFragment extends Fragment {
    // TODO: We need to figure out how to store userID to be used as primary key to access entry in the database.
    private UserProfile user;
    private FirebaseFirestore db;
    private CollectionReference users;
    private CollectionReference moods;
    private ArrayList<Mood> moodsList;

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

        //-----------------get mood from specific user-------------------------
        db = FirebaseFirestore.getInstance();
        CollectionReference MoodDB = db.collection("MoodDB");

        //1. get logged in user
        // todo   bruh idk, maybe wait on US 030101
        // set a value for now
        user = new UserProfile("User1", "pass1");
        //https://stackoverflow.com/questions/53140913/querying-by-a-field-with-type-reference-in-firestore
        DocumentReference userDocRef = db.collection("users").document(user.getUsername());




        //2. query collection to get all mood from user
        //https://firebase.google.com/docs/firestore/query-data/queries#java_2
        Query loggedInUserMoodsQuery = MoodDB.whereEqualTo("owner", userDocRef);

        ArrayList<Mood> moodArrayList = new ArrayList<>(Arrays.asList(dummyMoods));
        moodArrayList = new ArrayList<>();

        loggedInUserMoodsQuery.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("NANCY", document.getId() + " => " + document.getData());

                                Map<String, Object> moodDocument = document.getData();
                                moodDocument.replace("owner", user);
                                Timestamp timestamp = (Timestamp) moodDocument.get("postedDate");
                                moodDocument.replace("postedDate", timestamp.toDate());
                                Log.d("NANCY", moodDocument.toString());

                                for (var entry : moodDocument.entrySet()){
                                    Log.d("NANCY", "Map items: " + entry.getKey() + "/" + entry.getValue() + "/" + entry.getValue().getClass().getName());
                                }

                                Mood mood = new Mood(moodDocument);

        ArrayAdapter<Mood> moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList);
        moodListView.setAdapter(moodArrayAdapter);
    }


    private List<Mood> getUserMoods() {

    }

}