package com.example.project_seekdeep;

import android.net.Uri;
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

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
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

    private UserProfile loggedInUser;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    CollectionReference MoodDB;

    /**
     * Require empty public constructor for the Feed Fragment
     */
    public FeedFragment() {
        // Required empty public constructor
    }

    /**
     * Modify On create 
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            loggedInUser = (UserProfile) getArguments().getSerializable("userProfile");
        }
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

        Log.d("NANCY", "ON VIEW CREATED OF FEED FRAGMENT");


        //set views
        moodListView = view.findViewById(R.id.list_view_mood);
        ArrayList<Mood> moodArrayList = new ArrayList<>();
        ArrayAdapter<Mood> moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList);
        moodListView.setAdapter(moodArrayAdapter);

        CollectionReference moods = db.collection("MoodDB");


        if (loggedInUser != null){
            Log.d("NANCY", "feed fragment logged in user |" + loggedInUser.getUsername());
            Query MoodsQuery = moods.whereNotEqualTo("owner.username", loggedInUser.getUsername());

            MoodsQuery.addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                }
                if (value != null) {
                    moodArrayList.clear();
                    for (QueryDocumentSnapshot snapshot : value) {



                        HashMap<String, Object> ownerSnapshot = (HashMap<String, Object>) snapshot.getData().get("owner");

                        UserProfile user = new UserProfile( ownerSnapshot.get("username").toString(),
                                ownerSnapshot.get("password").toString());
                        EmotionalStates emotionalState = EmotionalStates.valueOf((String)snapshot.get("emotionalState"));
                        SocialSituations socialSituation = SocialSituations.valueOf((String) snapshot.get("socialSituation"));

                        List<String> followers = (List<String>) snapshot.get("followers");
                        Date postedDate = Objects.requireNonNull(snapshot.getTimestamp("postedDate")).toDate();
                        String reason = (String) snapshot.get("reason");

                        String imageStr = (String) snapshot.get("image");
                        Uri image = null;
                        if (imageStr != null){
                            image = Uri.parse(imageStr);
                        }


                        Mood mood = new Mood(user, emotionalState, socialSituation, followers, postedDate, reason);

                        mood.setImage(image);
                        mood.setDocRef(snapshot.getReference());

                        moodArrayList.add(mood);
                    }

                    //taken from Jachelle

                    MoodFiltering.sortReverseChronological(moodArrayList);  // this will sort the array in place
                    moodArrayAdapter.notifyDataSetChanged();
                    moodListView.setAdapter(moodArrayAdapter);
                    moodArrayAdapter.notifyDataSetChanged();
                }
            });
        }

    }

}