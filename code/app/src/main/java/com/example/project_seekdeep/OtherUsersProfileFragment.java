package com.example.project_seekdeep;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.HashMap;
import java.util.Map;

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

        //Get instance of db
        db = FirebaseFirestore.getInstance();
        //Assign the collection reference (if DNE, it'll create a new one)
        followingsAndRequestsRef = db.collection("followings_and_requests");

        //Setup the username
        usernameTextView = view.findViewById(R.id.username_profile);
        //usernameTextView.setText(username);

        //Retrieve current logged-in user and userBeingViewd from bundle
        if (getArguments() != null) {
            loggedInUser = (UserProfile) getArguments().getSerializable("loggedInUser");
            userBeingViewed = (UserProfile) getArguments().getSerializable("userBeingViewed");
        }

        //Set up the username on the profile page
        String username = userBeingViewed.getUsername();
        usernameTextView = view.findViewById(R.id.username_profile);
        usernameTextView.setText("@" + username);

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
                                followButton.setText("Following");
                            }
                            if ("pending".equals(status)) {
                                isPending = true;
                                followButton.setText("Pending");
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
        //Create a new doc with a uniquely generated id
        DocumentReference newDocRef = followingsAndRequestsRef.document();

        Map<String, Object> followData = new HashMap<>();
        followData.put("follower", loggedInUser.getUsername());
        followData.put("followee", userBeingViewed.getUsername());
        followData.put("status", "pending");

        newDocRef.set(followData);

        //Update isPending
        isPending = true;

        followButton = getView().findViewById(R.id.follow_button);
        followButton.setText("Pending");
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