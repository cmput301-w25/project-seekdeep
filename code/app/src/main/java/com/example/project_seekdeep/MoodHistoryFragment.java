package com.example.project_seekdeep;


import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This fragment class is designed to display a list of posted moods by a given user.
 * @author Kevin Tu, Nancy Lin
 */

public class MoodHistoryFragment extends Fragment {
    // TODO: We need to figure out how to store userID to be used as primary key to access entry in the database.
    private UserProfile loggedInUser;
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
//        loggedInUser = (UserProfile) requireArguments().getSerializable("user");


        // Instantiate database for usage
        db = FirebaseFirestore.getInstance();
        users = db.collection("UserDB");
        moods = db.collection("MoodDB");

        UserProfile user = new UserProfile("kevtu2", "222");

        //2. query collection to get all mood from user
        //https://firebase.google.com/docs/firestore/query-data/queries#java_2
        Query loggedInUserMoodsQuery = moods.whereEqualTo("ownerString", "kevtu2");

        ArrayList<Mood> moodArrayList = new ArrayList<>();
        ArrayAdapter<Mood> moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList);
        moodListView.setAdapter(moodArrayAdapter);

        loggedInUserMoodsQuery.addSnapshotListener((value, error) -> {
           if (error != null) {
               Log.e("Firestore", error.toString());
           }
           if (value != null) {
               for (QueryDocumentSnapshot snapshot : value) {
                    EmotionalState emotionalState = EmotionalState.valueOf((String)snapshot.get("emotionalState"));
                    String socialSituation = (String) snapshot.get("socialSituation");
                    String trigger = (String) snapshot.get("trigger");
                    // For now, test with no followers
                    Date postedDate = new Date();

                    Mood mood = new Mood(user, emotionalState, socialSituation, trigger, null, postedDate);
                    moodArrayList.add(mood);
               }
               moodArrayAdapter.notifyDataSetChanged();
           }
        });
    }
}