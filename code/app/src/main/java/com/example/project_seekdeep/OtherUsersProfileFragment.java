package com.example.project_seekdeep;

import static android.view.View.VISIBLE;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This activity displays another user's profile. Including their username, mood history, number of followers & following, and a button to follow them.
 *
 * Following feature:
 * If the logged-in user doesn't follow the user's profile, there will be a "Follow" button.
 * Once clicked, the button will change to a "Pending" button.
 * Once the user has accepted the follow request, then it will change to a "Following" button.
 */
public class OtherUsersProfileFragment extends Fragment {
    private TextView usernameTextView;
    private FirebaseFirestore db; //Need access to the database to retrieve all the user's moods
    private ArrayList<Mood> moodArrayList;
    private ArrayAdapter<Mood> moodArrayAdapter;
    private CollectionReference followingsAndRequestsRef;
    private ImageButton backButton;

    private Button followButton;

    //Fetch the state of the button
    boolean isFollowing;
    boolean isPending;
    private UserProfile loggedInUser;
    private UserProfile userBeingViewed;


    //Constructor
    public OtherUsersProfileFragment() {
        super(R.layout.other_users_profile_page);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Retrieve current logged-in user and userBeingViewed from bundle
        if (getArguments() != null) {
            loggedInUser = (UserProfile) getArguments().getSerializable("loggedInUser");
            userBeingViewed = (UserProfile) getArguments().getSerializable("userBeingViewed");
        }

        //Setup the username
        usernameTextView = view.findViewById(R.id.username_profile);
        //Set up the username on the profile page
        String username = userBeingViewed.getUsername();
        usernameTextView = view.findViewById(R.id.username_profile);
        usernameTextView.setText("@" + username);

        //debugging: prevent further execution if userBeingViewed was not passed into this fragment
        if (userBeingViewed == null) {
            Log.e("OtherUsersProfileFragment", "Error: userBeingViewed is null!!!");
            return;
        }

        //Setup the mood list
        ListView moodListView = view.findViewById(R.id.history_listview); //UI ListView
        moodArrayList = new ArrayList<>();
        //Adapter tells how moodArrayList should display the items
        moodArrayAdapter = new UserMoodArrayAdapter(view.getContext(), moodArrayList);
        //Set the adapter to the UI Listview
        moodListView.setAdapter(moodArrayAdapter);

        //Get instance of db and the moods collection
        db = FirebaseFirestore.getInstance();
        CollectionReference moods = db.collection("MoodDB");

        //Query the collection to get all moods for the userBeingView
        HashMap<String, Object> userMap = new HashMap<String, Object> ();
        userMap.put("password", userBeingViewed.getPassword());
        userMap.put("username", userBeingViewed.getUsername());
        Query userBeingViewedMoodsQuery = moods.whereEqualTo("owner.username", userBeingViewed.getUsername());


        userBeingViewedMoodsQuery.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {

                if (!(moodArrayList == null)) {
                    moodArrayList.clear();
                }
                for (QueryDocumentSnapshot snapshot : value) {
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
                    Mood mood = new Mood(userBeingViewed, emotionalState, socialSituation, followers, postedDate, reason);

                    mood.setDocRef(snapshot.getReference());
                    mood.setImage(image);
                    //Store mood object into the list
                    moodArrayList.add(mood);
                }
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

        //Fetch the state of the button
        isFollowing = false;
        isPending = false;
        updateButtonStatus();

        //Implement the follow button
        followButton = view.findViewById(R.id.follow_button);
        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isFollowing && !isPending) {
                sendFollowRequestToDataBase();
            }
            }
        });

        //TO DO: Constantly listen to the database to see if a user accepts another user's requests

    }

    private void updateButtonStatus() {
        followButton = getView().findViewById(R.id.follow_button);

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
                                isFollowing = true;
                                changeButton("Following");
                            }
                            if ("pending".equals(status)) {
                                isPending = true;
                                changeButton("Pending");
                            }
                        }
                        else {
                            isFollowing = false;
                            isPending = false;
                        }
                    }
                });
    }

    private void sendFollowRequestToDataBase() {
        //Change the button to "Pending"
        changeButton("Pending");

        //Create a new doc with a uniquely generated id
        DocumentReference newDocRef = followingsAndRequestsRef.document();

        Map<String, Object> followData = new HashMap<>();
        followData.put("follower", loggedInUser.getUsername());
        followData.put("followee", userBeingViewed.getUsername());
        followData.put("status", "pending");

        newDocRef.set(followData);

        //Update isPending
        isFollowing = false;
        isPending = true;

    }

    private void changeButton(String newStatus) {
        followButton = getView().findViewById(R.id.follow_button);
        followButton.setText(newStatus);
        if (newStatus.equals("Pending")) {
            getView().findViewById(R.id.follow_button).setVisibility(View.GONE);
            getView().findViewById(R.id.pending_button).setVisibility(View.VISIBLE);
        }
        if (newStatus.equals("Following")) {
            getView().findViewById(R.id.pending_button).setVisibility(View.GONE);
            getView().findViewById(R.id.following_button).setVisibility(View.VISIBLE);
        }
    }


//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.other_users_profile_page);
//
//        //Setup the username
//        String username = getIntent().getStringExtra("USERNAME");
//        usernameTextView = findViewById(R.id.username_profile);
//        usernameTextView.setText(username);
//
//        //Get instance of db
//        db = FirebaseFirestore.getInstance();
//
//        //Implement the back button
//        //https://stackoverflow.com/questions/72634225/onbackpressed-is-deprecated-what-is-the-alternative
//        backButton = findViewById(R.id.back_button);
//        backButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getOnBackPressedDispatcher().onBackPressed();
//            }
//        });
//
//        //Fetch the state of the button
//        // TO DO: see if user is following or already requested this user from database
//
//        /*
//        The button has 3 states:
//        1. "Follow" : isFollowing == true, isPending == false
//        2. "Pending" : isFollowing == false, isPending == true
//        3. "Following" : both are false
//         */
//        isFollowing = false;
//        isPending = false;
//
//        //Implement the follow button
//        followButton = findViewById(R.id.follow_button);
//        //Change the state of the follow button (in the current view) depending on the current state
//        followButton.setOnClickListener(view -> {
//            //If user is neither following nor pending, then they will see a Follow button that they can click
//            if (!isFollowing && !isPending) {
//                isPending = true;
//                updateFollowButton(followButton, isFollowing, isPending);
//                sendFollowRequestToDataBase();
//            }
//        });
//    }
//
//    /**
//     * This method changes the state of the follow button on the UI of profile that the user is viewing.
//     * @param followButton is the UI element that will be changed to either "Follow", "Pending", or "Following"
//     * @param isFollowing declares if the user is already following this profile
//     * @param isPending declares if the user has already requested to follow this profile
//     */
//    public void updateFollowButton(Button followButton, boolean isFollowing, boolean isPending) {
//        if (isFollowing && !isPending) {
//            followButton.setText("Following");
//        }
//        else if (isPending && !isFollowing) {
//            followButton.setText("Pending");
//        }
//        else {
//            followButton.setText("Follow");
//        }
//    }
//
//    private void sendFollowRequestToDataBase() {
//
//    }
//
//}
}