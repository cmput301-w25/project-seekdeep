package com.example.project_seekdeep;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import java.util.Date;
import java.util.List;

import java.util.Objects;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

/**
 * This fragment class is designed to display a list of posted moods by a given user.
 * @author Kevin Tu, Nancy Lin
 */

public class MoodHistoryFragment extends Fragment {
    // TODO: We need to figure out how to store userID to be used as primary key to access entry in the database.
    private UserProfile loggedInUser;
    private ArrayList<Mood> moodsList;

    private ListView moodListView;

    private ArrayList<Mood> moodArrayList;
    private ArrayAdapter<Mood> moodArrayAdapter;



    private FirebaseFirestore db;
    CollectionReference MoodDB;

    public MoodHistoryFragment() {
        super(R.layout.layout_feed);
    }

    // add username as variable
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ;
    }

    /**
     * Upon creating this view, it will query the database and load in all the moods
     * that the user has created.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set views
        ListView moodListView = view.findViewById(R.id.list_view_mood);

        // Get current user that is currently logged in
//        loggedInUser = (UserProfile) requireArguments().getSerializable("user");




        // Instantiate database for usage
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("UserDB");
        CollectionReference moods = db.collection("MoodDB");

        UserProfile user = new UserProfile("User1", "pass1");

        //2. query collection to get all mood from user
        //https://firebase.google.com/docs/firestore/query-data/queries#java_2
        Query loggedInUserMoodsQuery = moods.whereEqualTo("owner", "User1");

        ArrayList<Mood> moodArrayList = new ArrayList<>();
        ArrayAdapter<Mood> moodArrayAdapter = new UserMoodArrayAdapter(view.getContext(), moodArrayList);
        moodListView.setAdapter(moodArrayAdapter);

        loggedInUserMoodsQuery.addSnapshotListener((value, error) -> {
           if (error != null) {
               Log.e("Firestore", error.toString());
           }
           if (value != null) {
               moodArrayList.clear();
               for (QueryDocumentSnapshot snapshot : value) {
                    EmotionalStates emotionalState = EmotionalStates.valueOf((String)snapshot.get("emotionalState"));
                    SocialSituations socialSituation = SocialSituations.valueOf((String) snapshot.get("socialSituation"));
                    String trigger = (String) snapshot.get("trigger");
                    List<String> followers = (List<String>) snapshot.get("followers");
                    Date postedDate = Objects.requireNonNull(snapshot.getTimestamp("postedDate")).toDate();

                    Mood mood = new Mood(user, emotionalState, socialSituation, trigger, followers, postedDate);

                    mood.setDocRef(snapshot.getReference());

                    moodArrayList.add(mood);
               }
               moodArrayAdapter.notifyDataSetChanged();
           }
        });
    }
}