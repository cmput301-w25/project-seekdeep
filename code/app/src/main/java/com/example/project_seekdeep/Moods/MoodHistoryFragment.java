package com.example.project_seekdeep.Moods;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.project_seekdeep.Followings.ManageFollowRequestsFragment;
import com.example.project_seekdeep.Helpers.EmotionalStates;
import com.example.project_seekdeep.Helpers.FilterMenuDialogFragment;
import com.example.project_seekdeep.Helpers.SocialSituations;
import com.example.project_seekdeep.Helpers.UserProfile;
import com.example.project_seekdeep.LoginAndSignup.LogInFragment;
import com.example.project_seekdeep.Calendar.*;
import com.example.project_seekdeep.MainActivity;
import com.example.project_seekdeep.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This fragment class is designed to display a list of posted moods by a given user.
 * @author Kevin Tu, Nancy Lin, modified by Jachelle Chan, Saurabh
 */

public class MoodHistoryFragment extends Fragment implements FilterMenuDialogFragment.OnFilterSelectedListener{
    // TODO: We need to figure out how to store userID to be used as primary key to access entry in the database.
    private UserProfile loggedInUser;
    private ArrayList<Mood> filteredMoodList;

    private ArrayList<Mood> moodArrayList;
    private ArrayAdapter<Mood> moodArrayAdapter;

    private FirebaseFirestore db;
    private CollectionReference moods;
    private CollectionReference users;
    private Button requestButton;


    /**
     * Constructor for the Fragment that makes the view from the xml layout
     */
    public MoodHistoryFragment() {
        super(R.layout.profile_feed);
    }

    // add username as variable

    /**
     * When this history fragment is created, instantiate moodArrayList once
     * AS well as the firebase
     * and the userprofile
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moodArrayList = new ArrayList<>();
        filteredMoodList = new ArrayList<>();

        // Get the logged in User

        loggedInUser = (UserProfile) getArguments().getSerializable("userProfile");

        //create the firestore
        db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("UserDB");
        CollectionReference moods = db.collection("MoodDB");
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
        ListView moodListView = view.findViewById(R.id.history_listview);

        // set Textview for username placeholder to be the logged in user's username
        TextView username = view.findViewById(R.id.username_profile);
        username.setText(loggedInUser.getUsername());

        if (loggedInUser.getUsername().equals("johnCena")){
            ImageView avatar = view.findViewById(R.id.profile_pic);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storage2 = storage.getReference("/Images/1000037614");
            storage2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png'
                    Glide.with(getContext())
                            .load(uri)
                            .into(avatar);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }

        // Instantiate database for usage
        db = FirebaseFirestore.getInstance();
        CollectionReference users = db.collection("UserDB");
        CollectionReference moods = db.collection("MoodDB");

        // Logout functionality
        TextView logoutText = view.findViewById(R.id.logout_text);
        logoutText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragManager = getParentFragmentManager();

                MainActivity mainActivity = (MainActivity) getActivity();
                assert mainActivity != null;

                BottomNavigationView navBar = mainActivity.findViewById(R.id.bottomNavigationView);
                navBar.setSelectedItemId(R.id.feed_bottom_nav);
                navBar.setVisibility(View.GONE);

                fragManager.beginTransaction()
                        .remove(mainActivity.getSelectedFragment())
                        .replace(R.id.frameLayout, new LogInFragment()).commit();
            }
        });

        //Calendar link
        Button calendarButton = view.findViewById(R.id.calendar_button);
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fragManager = getParentFragmentManager();

                //Create view fragment, send the current logged-in user
                CalendarFragment calendarFragment = new CalendarFragment();
                Bundle calendarArgs = new Bundle();
                calendarArgs.putSerializable("userProfile", loggedInUser);
                calendarFragment.setArguments(calendarArgs);

                fragManager.beginTransaction()
                        .replace(R.id.frameLayout, calendarFragment)
                        .setReorderingAllowed(true)
                        .addToBackStack("history")
                        .commit();
            }
        });


        //2. query collection to get all mood from user
        //https://firebase.google.com/docs/firestore/query-data/queries#java_2
        HashMap<String, Object> userMap = new HashMap<String, Object> ();
        userMap.put("password", loggedInUser.getPassword());
        userMap.put("username", loggedInUser.getUsername());
        Query loggedInUserMoodsQuery = moods.whereEqualTo("owner.username", loggedInUser.getUsername());

        moodArrayAdapter = new UserMoodArrayAdapter(view.getContext(), moodArrayList);

        Log.d("Nancy", "MOOD HISTORY ON VIEW CREATED");


        loggedInUserMoodsQuery.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {

                if (!(moodArrayList == null)) {
                    moodArrayList.clear();
                    Log.d("NANCY", "Clear arraylist");
                }

                for (QueryDocumentSnapshot snapshot : value) {
                    EmotionalStates emotionalState = EmotionalStates.valueOf((String) snapshot.get("emotionalState"));
                    SocialSituations socialSituation = SocialSituations.valueOf((String) snapshot.get("socialSituation"));

                    List<String> followers = (List<String>) snapshot.get("followers");
                    Date postedDate = Objects.requireNonNull(snapshot.getTimestamp("postedDate")).toDate();
                    String reason = (String) snapshot.get("reason");

                    String imageStr = (String) snapshot.get("image");
                    Uri image = null;
                    if (imageStr != null) {
                        image = Uri.parse(imageStr);
                    }


                    Mood mood = new Mood(loggedInUser, emotionalState, socialSituation, followers, postedDate, reason);
                    mood.setDocRef(snapshot.getReference());
                    mood.setImage(image);

                    moodArrayList.add(mood);
                }

                if (!(moodArrayList == null)) {
                    MoodFiltering.removeAllFilters();  // as a preventative to having other fragment's filters
                    MoodFiltering.sortReverseChronological(moodArrayList);  // this will sort the array in place
                    moodArrayAdapter.notifyDataSetChanged();
                    moodListView.setAdapter(moodArrayAdapter);

                    // save original for filters that might remove items from array
                    MoodFiltering.saveOriginal(moodArrayList);
                    // reverse chronological doesn't need to do this because it's not a filter
                    // and the user just wants to view it in reverse chronological

                    // click filter button to add and remove the filter. this will change when UI for the filter dialog fragment is added
                    ImageButton filterButton = view.findViewById(R.id.filter_button);
                    filterButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            new FilterMenuDialogFragment().show(getChildFragmentManager(), "profile");
                        }
                    });
                    moodListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            // Bundle up the original Mood object that was clicked on
                            Bundle moodAndUserBundle = new Bundle();
                            moodAndUserBundle.putSerializable("mood", moodArrayList.get(position));

                            UserProfile loggedInUser = (UserProfile) getArguments().getSerializable("userProfile");
                            moodAndUserBundle.putSerializable("userProfile", loggedInUser);

                            // This is used to navigate back and forth between a mood comment section and the feed or history
                            FragmentManager fragManager = getParentFragmentManager();

                            // Create new fragment and send Mood off into new fragment
                            ViewMoodDetailsFragment viewMoodDetailsFragment = new ViewMoodDetailsFragment();
                            viewMoodDetailsFragment.setArguments(moodAndUserBundle);
                            fragManager.beginTransaction()
                                    .replace(R.id.frameLayout, viewMoodDetailsFragment)
                                    .setReorderingAllowed(true)
                                    .addToBackStack("feed")
                                    .commit();
                        }
                    });
                    requestButton = view.findViewById(R.id.manage_requests_button);
                    requestButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ManageFollowRequestsFragment fragment = new ManageFollowRequestsFragment();
                            Bundle bundle = new Bundle();
                            bundle.putString("username", loggedInUser.getUsername());
                            bundle.putSerializable("userProfile", loggedInUser);
                            fragment.setArguments(bundle);
                            fragment.show(getChildFragmentManager(), "requests");
                            // Taken from Kevin's implementation of comments and viewing moods
                            // Taken by: Jachelle Chan on March 30, 2025
                            moodListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    // Bundle up the original Mood object that was clicked on
                                    Bundle moodAndUserBundle = new Bundle();
                                    moodAndUserBundle.putSerializable("mood", moodArrayList.get(position));

                                    UserProfile loggedInUser = (UserProfile) getArguments().getSerializable("userProfile");
                                    moodAndUserBundle.putSerializable("userProfile", loggedInUser);

                                    // This is used to navigate back and forth between a mood comment section and the feed or history
                                    FragmentManager fragManager = getParentFragmentManager();

                                    // Create new fragment and send Mood off into new fragment
                                    ViewMoodDetailsFragment viewMoodDetailsFragment = new ViewMoodDetailsFragment();
                                    viewMoodDetailsFragment.setArguments(moodAndUserBundle);
                                    fragManager.beginTransaction()
                                            .replace(R.id.frameLayout, viewMoodDetailsFragment)
                                            .setReorderingAllowed(true)
                                            .addToBackStack("feed")
                                            .commit();
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    /**
     * This method is called when filters are applied in the FilterMenuDialogFragment
     * @param selectedMoods: An arraylist of emotional state(s) that are selected by the user
     * @param selectedTimeline: A string of what the user wants to filter the timeline by in terms of the MoodFiltering class
     */
    @Override
    public void onFiltersApplied(ArrayList<EmotionalStates> selectedMoods, String selectedTimeline, List<String> keywords) {
        // apply the selected filters if they arent empty
        MoodFiltering.removeAllFilters();
        if(!selectedMoods.isEmpty()) {
            MoodFiltering.addStates(selectedMoods);
            MoodFiltering.applyFilter("states");
        }
        filteredMoodList = MoodFiltering.getFilteredMoods();
        moodArrayAdapter.clear();
        moodArrayAdapter.addAll(filteredMoodList);
        moodArrayAdapter.notifyDataSetChanged();

        if(!selectedTimeline.isBlank()) {
            MoodFiltering.applyFilter(selectedTimeline);
            filteredMoodList = MoodFiltering.getFilteredMoods();
            moodArrayAdapter.clear();
            moodArrayAdapter.addAll(filteredMoodList);
            moodArrayAdapter.notifyDataSetChanged();
        }

        if(!keywords.isEmpty()) {
            MoodFiltering.addKeyword(keywords);
            MoodFiltering.applyFilter("keyword");
            filteredMoodList = MoodFiltering.getFilteredMoods();
            moodArrayAdapter.clear();
            moodArrayAdapter.addAll(filteredMoodList);
            moodArrayAdapter.notifyDataSetChanged();
        }
    }

    /**
     * This method is called when filters are reset in the FilterMenuDialogFragment
     */
    @Override
    public void onFiltersReset() {
        MoodFiltering.removeAllFilters();
        filteredMoodList = MoodFiltering.getFilteredMoods();
        moodArrayAdapter.clear();
        moodArrayAdapter.addAll(filteredMoodList);
        moodArrayAdapter.notifyDataSetChanged();

        SharedPreferences sharedPref = getActivity().getSharedPreferences("MoodFilter",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("selected_moods", "");
        editor.putString("selected_timeline", "");
        editor.putString("keyword", "");
        editor.putString("filtered_mood_ids", "");
        editor.apply();
    }

    private void saveFilterState(ArrayList<EmotionalStates> selectedMoods, String selectedTimeline, String keyword) {
        SharedPreferences sharedPref = getActivity().getSharedPreferences("MoodFilter",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        String moodsString = selectedMoods.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
        editor.putString("selected_moods", moodsString);
        editor.putString("selected_timeline", selectedTimeline);
        editor.putString("keyword", keyword);

        String moodIdsString = filteredMoodList.stream()
                .map(mood -> mood.getDocRef().getId())
                .collect(Collectors.joining(","));
        editor.putString("filtered_mood_ids", moodIdsString);
        editor.apply();
    }
}