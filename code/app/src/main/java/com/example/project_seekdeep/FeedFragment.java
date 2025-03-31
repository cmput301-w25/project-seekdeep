package com.example.project_seekdeep;

import android.graphics.Typeface;
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
import android.widget.ListView;
import android.widget.TextView;

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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * This fragment class is designed to display all the moods (except for the logged-in user's moods) in the database.
 * It also displays a Users tab, where you can search for other users
 * @author Kevin Tu, Nancy Lin
 */
public class FeedFragment extends Fragment implements MoodArrayAdapter.OnUsernameClickListener {

    private ListView moodListView;
    private ArrayList<Mood> moodArrayList;
    private ArrayAdapter<Mood> moodArrayAdapter;
    private ArrayList<Mood> filteredMoodArrayList;

    private UserProfile loggedInUser;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    CollectionReference MoodDB;
    private TextView moodsTab;
    private TextView usersTab;
    private ListView userListView;
    private ArrayList<UserProfile> userArrayList;
    private ArrayList<UserProfile> originalUsersArrayList;
    private ArrayAdapter<UserProfile> userArrayAdapter;
    private CollectionReference usersRef;
    private EditText feedSearchEditText;
    private boolean onUsersTab = false;



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
        moodListView = view.findViewById(R.id.list_view_moods);
//        ArrayList<Mood> moodArrayList = new ArrayList<>();
        moodArrayList = new ArrayList<>();
        filteredMoodArrayList = new ArrayList<>();
//        ArrayAdapter<Mood> moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList, this); //"this" passes current instance of feedFragment
        moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList, this); //"this" passes current instance of feedFragment
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
                    if (imageStr != null) {
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

                //taken from Jachelle
                MoodFiltering.removeAllFilters();  // as a preventative to having other fragment's filters
                MoodFiltering.sortReverseChronological(moodArrayList);  // this will sort the array in place
                moodArrayAdapter.notifyDataSetChanged();
                //Everytime the feed is displayed/updated, must save the original in MoodFiltering incase filters get applied (ie. search bar is used)
                MoodFiltering.saveOriginal(moodArrayList);

                //Implement the search bar
                feedSearchEditText = view.findViewById(R.id.feed_search_bar);
                //Prevent the Enter key from intputting new lines into the search bar
                feedSearchEditText.setOnEditorActionListener((v, actionId, event) -> {
                    Log.d("Search","enter key was pressed! nothing should happen");
                    return true; // return w/o doing anything, so to disable the enter key
                });
                final TextWatcher txtWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }
                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        if (!onUsersTab) {
                            Log.d("Feed", "now apply search bar to moods!");
                            //get the keyword(s) from search bar
                            List<String> keywords = Arrays.asList(feedSearchEditText.getText().toString().split(" "));
                            applySearchBarToMoods(keywords);
                        }
                        else {
                            Log.d("Feed", "apply search bar to Users!!");
                            //Searching for users only needs one keyword
                            List<String> keyword = Arrays.asList(feedSearchEditText.getText().toString().split(" "));
                            Log.d("Feed", "apply search bar to Users!!"+keyword);
                            applySearchBarToUser(keyword);
                        }
                    }
                    @Override
                    public void afterTextChanged(Editable editable) {
                    }
                };
                feedSearchEditText.addTextChangedListener(txtWatcher);
        }
        });
        }

        //POPULATE THE list_view_users LISTVIEW UI (default hidden) - only display this listview when the Users tab is clicked
        userListView = view.findViewById(R.id.list_view_users); //Set view to ui
        usersRef = db.collection("users");
        userArrayList = new ArrayList<>();
        //originalUsersArrayList is not linked to the adapter. It just constantly keeps the orig list of users without any filters applied.
        originalUsersArrayList = new ArrayList<>();
        userArrayAdapter = new UserArrayAdapter(view.getContext(), userArrayList); //link userArrayList and adapter
        userListView.setAdapter(userArrayAdapter); //link the UI Listview to the array adapter
        //Query firebase to fetch all users (except the logged-in user)
        if (loggedInUser != null) { //if statement to prevent app crashing
            Query usersQuery = usersRef.whereNotEqualTo("username", loggedInUser.getUsername());
            usersQuery.addSnapshotListener((value, error) -> {
                Log.d("SearchForUser", "user list should populate now!");
                //Check for errors
                if (error != null) {
                    Log.e("SearchForUsers", error.toString());
                    return; //exit if theres an error
                }
                //If a snapshot was taken
                if (value != null) {
                    userArrayList.clear();
                    originalUsersArrayList.clear();
                    for (QueryDocumentSnapshot snapshot : value) {
                        //Convert snapshots to UserProfile objects
                        UserProfile user = snapshot.toObject(UserProfile.class);
                        originalUsersArrayList.add(user);
                        userArrayList.add(user);
                    }
                    //notify adapter that data has changed
                    userArrayAdapter.notifyDataSetChanged();
                }
            });
        }

        // From lab 3, and fragment manager documentation
        // https://developer.android.com/guide/fragments/fragmentmanager
        // Ideas for the solution was adapted from the link below, surprisingly from the question itself and not an answer (lol)
        // https://stackoverflow.com/questions/46148117/listview-and-onitemclick-create-transparent-fragment?rq=1
        // Taken by: Kevin Tu on 2025-03-19
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
        //Use same logic as above for when a user is clicked on the users tab
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Bundle up the original user object that was clicked on
                Bundle usersBundle = new Bundle();
                usersBundle.putSerializable("userBeingViewed", userArrayList.get(i));
                usersBundle.putSerializable("loggedInUser", loggedInUser);

                // This is used to navigate back and forth between a user's profile fragment and the feed fragment
                FragmentManager fragManager = getParentFragmentManager();

                //Create new fragment and send the userBeingViewed and loggedInUser to the new fragment
                OtherUsersProfileFragment otherUsersProfileFragment = new OtherUsersProfileFragment();
                otherUsersProfileFragment.setArguments(usersBundle);

                fragManager.beginTransaction()
                        .replace(R.id.frameLayout, otherUsersProfileFragment)
                        .setReorderingAllowed(true)
                        .addToBackStack("feed")
                        .commit();
            }
        });

        //Setup tab switching logic
        moodsTab = view.findViewById(R.id.moods_tab);
        usersTab = view.findViewById(R.id.users_tab);
        //If moodsTab is clicked, show the moods list
        moodsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //update the tabs ui
                moodsTab.setTextSize(17);
                moodsTab.setTypeface(null,Typeface.BOLD);
                usersTab.setTextSize(15);
                usersTab.setTypeface(null, Typeface.NORMAL);
                //display the list of all moods (except for logged-in user's)
                moodListView.setVisibility(View.VISIBLE);
                userListView.setVisibility(View.GONE);
                //Update tab tracker
                onUsersTab = false;
            }
        });
        //If the usersTab is clicked, show the users list
        usersTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //update the tabs ui
                usersTab.setTextSize(17);
                usersTab.setTypeface(null, Typeface.BOLD);
                moodsTab.setTextSize(15);
                moodsTab.setTypeface(null,Typeface.NORMAL);
                //display the list of all users (except for the logged-in user)
                moodListView.setVisibility(View.GONE);
                userListView.setVisibility(View.VISIBLE);
                // Update tab tracker
                onUsersTab = true;
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
        String clickedUsernameString = clickUsername.getUsername();
        String loggedInUser = (String) getArguments().getString("username");

        //Only open a user's profile if it is not the user's own username
        if (!clickedUsernameString.equals(loggedInUser)) {
            openUserProfile(clickUsername); }
    }
    /**
     * This opens the profile of another user. Happens when a user clicks on someone else's username.
     * @param user the user that was clicked on
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

    /**
     * This method is used when the user types in the search bar while on the Moods tab in the Feed page.
     * It filters the entire feed to find moods that contain at least one of the given keywords in the search bar.
     * @param keywords The words typed into the search bar (keywords are separated by spaces)
     */
    public void applySearchBarToMoods(List<String> keywords) {
        MoodFiltering.addKeyword(keywords);
        MoodFiltering.applyFilter("keyword");
        filteredMoodArrayList = MoodFiltering.getFilteredMoods();
        moodArrayAdapter.clear();
        moodArrayAdapter.addAll(filteredMoodArrayList);
        moodArrayAdapter.notifyDataSetChanged();
        Log.d("Feed","search bar applied to moods!!!");
    }

    /**
     * This method is used when the user types in the search bar while on the Users tab in Feed page.
     * It filters the entire list of users and displays usernames that contain the given keyword in the search bar.
     * @param keywords The words typed into the search bar (keywords are separated by space)
     */
    public void applySearchBarToUser(List<String> keywords) {
        Log.d("Feed","applySearchBarToUsers");

        // ensure userArrayList is not null
        if (userArrayList == null || userArrayAdapter == null) {
            Log.e("Feed", "userArrayList or userArrayAdapter is null!");
            return;
        }
        //1. Restore userArrayList with the original unfiltered list of users (so that the keywords aren't applied to a previously filtered list)
        userArrayList.clear();
        userArrayList.addAll(originalUsersArrayList);

        //2. Create the filtered list
        List<UserProfile> filteredUsers = new ArrayList<>();
        for (UserProfile user : userArrayList) {
            String username = user.getUsername().toLowerCase();
            //Check if the keywords match
            for (String keyword : keywords) {
                if (username.contains(keyword.toLowerCase())) {
                    filteredUsers.add(user);
                    break;
                }
            }
        }
        //3. add the filtered to to userArrayAdapter
        //Update the adapter to populate the listview with the filtered user
        userArrayAdapter.clear();
        userArrayAdapter.addAll(filteredUsers);
        userArrayAdapter.notifyDataSetChanged();
    }

}