package com.example.project_seekdeep;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * This fragment class displays all the moods from users that the logged-in user is following.
 * @see UserProfile
 * @author Sarah Chang
 */
// TODO: Implement filtering (by emotion/recent/last 3/text)
public class FollowingFragment extends Fragment implements  MoodArrayAdapter.OnUsernameClickListener, FilterMenuDialogFragment.OnFilterSelectedListener {
    private ListView moodListView;

    private ArrayList<Mood> moodArrayList;
    private ArrayList<Mood> filteredMoodList;
    private ArrayAdapter<Mood> moodArrayAdapter;

    private UserProfile loggedInUser;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    CollectionReference MoodDB;



    /**
     * Require empty public constructor for the Feed Fragment
     */
    public FollowingFragment() {
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
        View inflatedView = inflater.inflate(R.layout.layout_following, container, false);
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
        moodListView = view.findViewById(R.id.listview_following);
        moodArrayList = new ArrayList<>();
        filteredMoodList = new ArrayList<>();
        moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList, this); //"this" passes current instance of followingFragment
        moodListView.setAdapter(moodArrayAdapter);

        CollectionReference moods = db.collection("MoodDB");

        // When a user is added/removed from the following list, must leave this fragment and then re-enter to see the change.
        // Resource: https://firebase.google.com/docs/firestore/query-data/queries#in_not-in_and_array-contains-any
        // POSSIBLE PROBLEM: if the user's following list has over 30 names, then .whereIn might fail
        if (loggedInUser != null && !loggedInUser.getFollowings().isEmpty()){
            Query MoodsQuery = moods.whereIn("ownerString", loggedInUser.getFollowings());

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

                        Boolean isPrivate = (Boolean) snapshot.get("private");

                        // For moods that are currently existing, but does not have the "private" field.
                        // We'll allow these to be public for the sake of demo-ing.
                        if (isPrivate == null) {
                            isPrivate = false;
                        } else if (isPrivate || loggedInUser.equals(user)) {
                            // This skips loading the mood into the feed since it is private and not owned by the currently logged in user.
                            continue;
                        }

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

                        String[] stringFields = {
                                reason,
                                socialSituation.toString()
                        };
                        Mood mood = new Mood(user, emotionalState, stringFields, followers, isPrivate, postedDate);

                        mood.setImage(image);
                        mood.setDocRef(snapshot.getReference());

                        moodArrayList.add(mood);
                    }
                    MoodFiltering.removeAllFilters();  // as a preventative to having other fragment's filters
                    MoodFiltering.sortReverseChronological(moodArrayList);  // this will sort the array in place
                    moodArrayAdapter.notifyDataSetChanged();
                    moodListView.setAdapter(moodArrayAdapter);                moodArrayAdapter.notifyDataSetChanged();

                    MoodFiltering.saveOriginal(moodArrayList);
                    ImageButton filterButton = view.findViewById(R.id.following_filter_button);
                    EditText searchBar = view.findViewById(R.id.search_bar);
                    filterButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            searchBar.setText("");  // clear the search bar
                            FilterMenuDialogFragment filterDialog = new FilterMenuDialogFragment();
                            filterDialog.keywordMapSearch(false);
                            filterDialog.show(getChildFragmentManager(), "following");
                        }
                    });

                    //Implement the search bar
                    //Need to disable the enter key in the searchBar so that newlines aren't added into the keywords
                    // setOnEditorActionListener listens for when an action key is pressed (ie. enter key)
                    searchBar.setOnEditorActionListener((v, actionId, event) -> {
                        Log.d("Search","enter key was pressed! nothing should happen");
                        return true; // return w/o doing anything, so to disable the enter key
                    });
                    final TextWatcher txtWatcher = new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        }
                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            //get the keyword from search bar
                            //String keywords = searchBar.getText().toString();
                            List<String> keywords = Arrays.asList(searchBar.getText().toString().split(" "));
                            applySearchBarKeyword(keywords);
                        }
                        @Override
                        public void afterTextChanged(Editable editable) {
                        }
                    };
                    searchBar.addTextChangedListener(txtWatcher);

                }
            });
        }
        EditText searchBar = view.findViewById(R.id.search_bar);
        // From lab 3, and fragment manager documentation
        // https://developer.android.com/guide/fragments/fragmentmanager
        // Ideas for the solution was adapted from the link below, surprisingly from the question itself and not an answer (lol)
        // https://stackoverflow.com/questions/46148117/listview-and-onitemclick-create-transparent-fragment?rq=1
        // Taken by: Kevin Tu on 2025-03-19
        moodListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchBar.setText("");  // clear the search bar
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
    /**
     * Implementation of the OnUsernameClickListener from the MoodArrayAdapter's interface
     * @param clickUsername the username that was clicked on
     */
    @Override
    public void onUsernameClick(UserProfile clickUsername) {
        //Check if the clickedUsername is the same as the logged-in user.
        EditText searchBar = getView().findViewById(R.id.search_bar);
        searchBar.setText("");  // clear the search bar
        String clickedUsernameString = clickUsername.getUsername();
        String loggedInUser = (String) getArguments().getString("username");

        //Only open a user's profile if it is not the user's own username
        if (!clickedUsernameString.equals(loggedInUser)) {
            openUserProfile(clickUsername); }
    }
    /**
     * This opens the profile of another user. Happens when a user clicks on someone else's username.
     * @param user
     */
    public void openUserProfile(UserProfile user) {
        //Create a new bundle, and put the user being viewed in it
        Bundle loggedInUserAndOtherUser = new Bundle();
        loggedInUserAndOtherUser.putSerializable("userBeingViewed", user);

        //Get the logged-in user from the bundle passed from MainActivity, put it in loggedInUserAndOtherUser
        UserProfile loggedInUser = (UserProfile) getArguments().getSerializable("userProfile");
        loggedInUserAndOtherUser.putSerializable("loggedInUser", loggedInUser);

        FragmentManager fragManager = getParentFragmentManager();

        //Create view fragment, send the user being viewed and the current logged-in user
        OtherUsersProfileFragment userProfileFragment = new OtherUsersProfileFragment();
        userProfileFragment.setArguments(loggedInUserAndOtherUser);

        fragManager.beginTransaction()
                .replace(R.id.frameLayout, userProfileFragment)
                .setReorderingAllowed(true)
                .addToBackStack("feed")
                .commit();
    }

    @Override
    public void onFiltersApplied(ArrayList<EmotionalStates> selectedMoods, String selectedTimeline, List<String> keywords) {
        Log.d("Following","onFiltersApplied here");

        //Same implementation as on MoodHistoryFragment
        MoodFiltering.removeAllFilters();
        //Check for selected emotional filters
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

        Log.d("Following","filters should be applied now");
    }

    @Override
    public void onFiltersReset() {
        MoodFiltering.removeAllFilters();
        EditText searchBar = getView().findViewById(R.id.search_bar);
        searchBar.setText("");  // clear the search bar
        filteredMoodList = MoodFiltering.getFilteredMoods();
        moodArrayAdapter.clear();
        moodArrayAdapter.addAll(filteredMoodList);
        moodArrayAdapter.notifyDataSetChanged();
    }

    public void applySearchBarKeyword(List<String> keywords) {
        MoodFiltering.addKeyword(keywords);
        MoodFiltering.applyFilter("keyword");
        filteredMoodList = MoodFiltering.getFilteredMoods();
        moodArrayAdapter.clear();
        moodArrayAdapter.addAll(filteredMoodList);
        moodArrayAdapter.notifyDataSetChanged();
        Log.d("Following","search bar applied!!!");
    }

}
