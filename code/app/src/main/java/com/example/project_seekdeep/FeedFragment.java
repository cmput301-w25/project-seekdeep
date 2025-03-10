package com.example.project_seekdeep;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This fragment class is designed to display all the moods in the database.
 * @author Kevin Tu, Nancy Lin
 */
public class FeedFragment extends Fragment {

    private ListView moodListView;

    private ArrayList<Mood> moodArrayList;
    private ArrayAdapter<Mood> moodArrayAdapter;

    private FirebaseFirestore db;
    CollectionReference MoodDB;

    /**
     * Require empty public constructor for the Feed Fragment
     */
    public FeedFragment() {
        // Required empty public constructor
    }


    /**
     * Upon creating this fragment, it will create a basic view, as well as
     * assign value to the firebase/ database references
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //initialize variables
        db = FirebaseFirestore.getInstance();
        MoodDB = db.collection("MoodDB");

        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.layout_feed, container, false);
        return inflatedView;
    }

    /**
     * When this view is created, it will query the mood database for all of the moods and show them
     *
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set views
        moodListView = view.findViewById(R.id.list_view_mood);

        //create dummy array data
        Mood[] dummyMoods ={
                new Mood(new UserProfile("User1", "pass1"), EmotionalStates.ANGER,
                        SocialSituations.ALONE, "What the trigger"),

                new Mood(new UserProfile("User2", "pass2"), EmotionalStates.CONFUSION,
                        SocialSituations.SEVERAL_PEOPLE, "What the trigger"),

                new Mood(new UserProfile("User3", "pass1"), EmotionalStates.DISGUST,
                        SocialSituations.CROWD, "What the trigger"),

                new Mood(new UserProfile("User4", "pass2"), EmotionalStates.FEAR,
                        SocialSituations.WITH_ANOTHER, "What the trigger"),

                new Mood(new UserProfile("User5", "pass1"), EmotionalStates.HAPPINESS,
                        SocialSituations.ALONE, "What the trigger"),

                new Mood(new UserProfile("User6", "pass2"), EmotionalStates.SADNESS,
                        SocialSituations.SEVERAL_PEOPLE, "What the trigger"),

                new Mood(new UserProfile("User7", "pass1"), EmotionalStates.SHAME,
                        SocialSituations.SEVERAL_PEOPLE, "What the trigger"),

                new Mood(new UserProfile("User8", "pass2"), EmotionalStates.SURPRISE,
                        SocialSituations.ALONE, "What the trigger")
        };

        //create mood array

        moodArrayList = new ArrayList<>();
        moodArrayList.addAll(Arrays.asList(dummyMoods));


        moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList);
        moodListView.setAdapter(moodArrayAdapter);



        Query MoodsQuery = MoodDB;

        ArrayList<Mood> moodArrayList = new ArrayList<>();
        ArrayAdapter<Mood> moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList);
        moodListView.setAdapter(moodArrayAdapter);

        MoodsQuery.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {
                moodArrayList.clear();
                for (QueryDocumentSnapshot snapshot : value) {

                    //idk why this here but it won't run without
                    //probably needed to the casting below doesn't break
                    Log.e("NANCY", snapshot.toString());
                    Log.d("NANCY", snapshot.get("owner").toString() + " | " + snapshot.getClass());

                    HashMap<String, Object> ownerSnapshot = (HashMap<String, Object>) snapshot.getData().get("owner");

                    UserProfile user = new UserProfile( ownerSnapshot.get("username").toString(),
                            ownerSnapshot.get("password").toString());
                    EmotionalStates emotionalState = EmotionalStates.valueOf((String)snapshot.get("emotionalState"));
                    SocialSituations socialSituation = SocialSituations.valueOf((String) snapshot.get("socialSituation"));
                    String trigger = (String) snapshot.get("trigger");
                    List<String> followers = (List<String>) snapshot.get("followers");
                    Date postedDate = Objects.requireNonNull(snapshot.getTimestamp("postedDate")).toDate();
                    String reason = (String) snapshot.get("reason");

                    Mood mood = new Mood(user, emotionalState, socialSituation, trigger, followers, postedDate, reason);

                    mood.setDocRef(snapshot.getReference());

                    moodArrayList.add(mood);
                }
                moodArrayAdapter.notifyDataSetChanged();
            }
        });
    }

}