package com.example.project_seekdeep.Followings;

import static android.view.View.VISIBLE;

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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.project_seekdeep.Helpers.EmotionalStates;
import com.example.project_seekdeep.Helpers.SocialSituations;
import com.example.project_seekdeep.Helpers.UserProfile;
import com.example.project_seekdeep.Helpers.UserProvider;
import com.example.project_seekdeep.Moods.Mood;
import com.example.project_seekdeep.Moods.MoodArrayAdapter;
import com.example.project_seekdeep.Moods.MoodFiltering;
import com.example.project_seekdeep.Moods.ViewMoodDetailsFragment;
import com.example.project_seekdeep.R;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * This activity displays another user's profile. Including their username, mood history, and a button to follow them.
 * <p></p>
 * Following features:
 * <pre>
 * - If the logged-in user doesn't follow the user's profile, there will be a "Follow" button.
 * - Once clicked, the button will change to a "Pending" button.
 * - Once userBeingViewed has accepted the follow request, then it will change to a "Following" button.
 * - If the logged-in user clicks on "Following", it will unfollow userBeingViewed.
 * </pre>
 * @see UserProfile
 * @see UserProvider
 * @see FollowRequest
 * @author Sarah Chang
 */
// TODO: get the number of followings and followers of userBeingViewed (might be uneccessary tho)
public class OtherUsersProfileFragment extends Fragment {
    private TextView usernameTextView;
    private FirebaseFirestore db; //Need access to the database to retrieve all the user's moods
    private CollectionReference moods; //Used to get the userBeingViewed's mood history
    private ArrayList<Mood> moodArrayList;
    private ArrayAdapter<Mood> moodArrayAdapter;
    private CollectionReference followingsAndRequestsRef;
    private ImageButton backButton;

    private Button followButton;
    private Button followingButton;

    //Fetch the state of the button
    boolean isFollowing;
    boolean isPending;
    private UserProfile loggedInUser;
    private UserProfile userBeingViewed;
    private UserProvider userProvider;
    private ProgressBar loadingCircle;


    //Constructor
    public OtherUsersProfileFragment() {
       //Require empty public constrictor
        //ERROR fixed: can't call super here or else loggedInUser and userBeingViewed are null (bundle not unpackaged)
    }

    /**
     * Modify On create. As soon as this fragment is created, unbundle and initialize loggedIsUser and userBeingViewed.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Retrieve current logged-in user and userBeingViewed from bundle
        if (getArguments() != null) {
            loggedInUser = (UserProfile) getArguments().getSerializable("loggedInUser");
            userBeingViewed = (UserProfile) getArguments().getSerializable("userBeingViewed");
            Log.d("OtherUsers", loggedInUser.getUsername()+"is ON VIEW CREATED OF OTHERUSERS FRAGMENT viewing: "+userBeingViewed.getUsername());
        }
        else {
            Log.e("OtherUsers", "getArguments() is NULL in onCreate");
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
        moods = db.collection("MoodDB");

        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.other_users_profile_page, container, false);
        return inflatedView;
    }

    /**
     * Upon creating this view, it will display the userBeingViewed's profile:
     *      - displays their username.
     *      - display Follow/Pending/Following button depending on the relationship between the loggedInUser and userBeingViewed.
     *      - Query the database and display all moods by userBeingViewed.
     *      - allows loggedInUser to send a follow request to userBeingViewed, and/or unfollow them.
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Get instance of db and the moods collection
        db = FirebaseFirestore.getInstance();
        moods = db.collection("MoodDB");

        //Instantiate userProvide (to use its firebase methods)
        //userProvider = new UserProvider(requireContext(), loggedInUser);
        userProvider = UserProvider.getInstance(loggedInUser, db); //use the same instance of userProvider that mainactivity uses

        //Display a loading symbol since the Follow/Pending/Following button delays when being initialized in initializeButtonStatus
        loadingCircle = view.findViewById(R.id.loading_spinner);
        loadingCircle.bringToFront(); //needed to add this so that it isn't hidden being the mood list
        loadingCircle.setVisibility(View.VISIBLE);

        //Retrieve current logged-in user and userBeingViewed from bundle (moved to OnCreate method)
//        if (getArguments() != null) {
//            loggedInUser = (UserProfile) getArguments().getSerializable("loggedInUser");
//            userBeingViewed = (UserProfile) getArguments().getSerializable("userBeingViewed");
//            Log.d("OtherUsers", "ON VIEW CREATED OF FEED FRAGMENT"+loggedInUser.getUsername());
//        }
        if ("johnCena".equals(userBeingViewed.getUsername())) {
            view.findViewById(R.id.profile_header).setBackground(ContextCompat.getDrawable(getContext(), R.drawable.john));
        }

        //Setup the username
        usernameTextView = view.findViewById(R.id.username_profile);
        //Set up the username on the profile page
        String username = userBeingViewed.getUsername();
        usernameTextView = view.findViewById(R.id.username_profile);
        usernameTextView.setText(username);

        //debugging: prevent further execution if userBeingViewed was not passed into this fragment
        if (userBeingViewed == null) {
            Log.e("OtherUsersProfileFragment", "Error: userBeingViewed is null!!!");
            return;
        }

        //Setup the mood list
        ListView moodListView = view.findViewById(R.id.history_listview); //UI ListView
        moodArrayList = new ArrayList<>();
        //Adapter tells how moodArrayList should display the items
        //1st arg: the context(fragment), 2nd arg: The list of mood objects to display, 3rd arg: ref to object that implements MoodArrayAdapter.OnUsernameClickListener
        //pass 'null' as the 3rd arg : moods in this fragment don't need to listen for username clicks
        moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList, null);
        //Set the adapter to the UI Listview
        moodListView.setAdapter(moodArrayAdapter);

        //Query the collection to get all moods for the userBeingView
        HashMap<String, Object> userMap = new HashMap<String, Object> ();
        userMap.put("password", userBeingViewed.getPassword());
        userMap.put("username", userBeingViewed.getUsername());
        Query userBeingViewedMoodsQuery = moods.whereEqualTo("owner.username", userBeingViewed.getUsername());

        //Display their mood history
        userBeingViewedMoodsQuery.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {

                if (!(moodArrayList == null)) {
                    moodArrayList.clear();
                }
                for (QueryDocumentSnapshot snapshot : value) {
                    Boolean isPrivate = (Boolean) snapshot.get("private");

                    // For moods that are currently existing, but does not have the "private" field.
                    // We'll allow these to be public for the sake of demo-ing.
                    if (isPrivate == null) {
                        isPrivate = false;
                    } else if (isPrivate) {
                        // This skips loading the mood into the feed since it is private and not owned by the currently logged in user.
                        continue;
                    }

                    //retreive emotionalState and socialSit from db (stored as strings) and conver to their Enum so can pass into ArrayAdapter
                    EmotionalStates emotionalState = EmotionalStates.valueOf((String) snapshot.get("emotionalState"));
                    SocialSituations socialSituation = SocialSituations.valueOf((String) snapshot.get("socialSituation"));
                    List<String> followers = (List<String>) snapshot.get("followers");
                    Date postedDate = Objects.requireNonNull(snapshot.getTimestamp("postedDate")).toDate();
                    String reason = (String) snapshot.get("reason");
                    //convert image from url (as represented in firestore) to URI (as represented in Mood class)
                    String imageStr = (String) snapshot.get("image");
                    Uri image = null;
                    if (imageStr != null) {
                        image = Uri.parse(imageStr);
                    }

                    //convert to mood object
                    String[] stringFields = {
                            reason,
                            socialSituation.toString()
                    };
                    Mood mood = new Mood(userBeingViewed, emotionalState, stringFields, followers, isPrivate, postedDate);

                    mood.setDocRef(snapshot.getReference());
                    mood.setImage(image);
                    //Store mood object into the list
                    moodArrayList.add(mood);
                }
                MoodFiltering.sortReverseChronological(moodArrayList);
                moodArrayAdapter.notifyDataSetChanged();
            }
            else {Log.d("Firestore", "no moods found for this user!");}
        });


        //Assign the collection reference (if DNE, it'll create a new one)
        followingsAndRequestsRef = db.collection("followings_and_requests");

        //Implement the back button
        backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragManager = getParentFragmentManager(); //parent manager is the FeedFragment
                fragManager.popBackStack();
            }
        });

        //Fetch the state of the button and initialize it
        initializeButtonStatus();

        //Implement the follow button.
        //When user clicks "Follow", it will create a new follow request being the current logged-in user and the user being viewed.
        followButton = view.findViewById(R.id.follow_button);
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFollowing && !isPending) {
                //sendFollowRequestToDataBase();
                FollowRequest followRequest = new FollowRequest(loggedInUser.getUsername(), userBeingViewed.getUsername());
                userProvider.sendFollowRequestToDataBase(followRequest);
                //userProvider.sendFollowRequestToDataBase(loggedInUser, userBeingViewed);
                changeButtonAndStatus("Pending");
            }
            }
        });

        //If user clicks on "following", if means user has unfollowed that profile
        followingButton = view.findViewById(R.id.following_button);
        followingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFollowing && !isPending) {
                    //Remove userBeingViewed from the UserProfile followings list and from firebase
                    userProvider.unfollowThisUser(userBeingViewed);
                    //Update the UI
                    changeButtonAndStatus("Unfollowing");
                }
            }
        });

        moodListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Bundle up the original Mood object that was clicked on
                Bundle moodAndUserBundle = new Bundle();
                moodAndUserBundle.putSerializable("mood", moodArrayList.get(position));

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


    //METHODS

    /**
     * This method is called whenever the loggedInUser goes into another user's profile page.
     * It will query firestore to see if the loggedInUser is already following them or has requested to follow them or neither.
     * It will then update the button to Follow or Pending or Following accordingly, using changeButtonAndStatus.
     * If:
     *  - loggedInUser doesn't follow userBeingViewed: display "Follow"
     *  - loggedInUser has requested to follow userBeingViewed: display "Pending"
     *  - loggedInUser is following userBeingViewed: display "Following"
     */
    private void initializeButtonStatus() {
        followingsAndRequestsRef
                .whereEqualTo("follower", loggedInUser.getUsername())
                .whereEqualTo("followee", userBeingViewed.getUsername())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            String status = doc.getString("status");
                            if ("following".equals(status)) {
                                changeButtonAndStatus("Following");
                            }
                            if ("pending".equals(status)) {
                                changeButtonAndStatus("Pending");
                            }
                        }
                        else {
                            //This just means the user doesn't follow userBeingViewed
                            changeButtonAndStatus("Unfollowing");
                        }
                    }
                });
    }

    /**
     * This method will update the UI to display the correct button according to the given status between loggedInUser and userBeingViewed.
     * @param newStatus The new status between loggedInUser and userBeingViewed.
     */
    private void changeButtonAndStatus(String newStatus) {
        if (newStatus.equals("Pending")) {
            getView().findViewById(R.id.follow_button).setVisibility(View.GONE);
            getView().findViewById(R.id.pending_button).setVisibility(View.VISIBLE);
            isFollowing = false;
            isPending = true;
        }
        if (newStatus.equals("Following")) {
            getView().findViewById(R.id.pending_button).setVisibility(View.GONE);
            getView().findViewById(R.id.following_button).setVisibility(View.VISIBLE);
            isFollowing = true;
            isPending = false;
        }
        if (newStatus.equals("Unfollowing")) {
            getView().findViewById(R.id.follow_button).setVisibility(VISIBLE);
            getView().findViewById(R.id.following_button).setVisibility(View.GONE);
            isFollowing = false;
            isPending = false;
        }
        //Remove the loading bar once the Follow/Pending/Following button it properly displayed on initialization
        loadingCircle.setVisibility(View.GONE);
    }


}