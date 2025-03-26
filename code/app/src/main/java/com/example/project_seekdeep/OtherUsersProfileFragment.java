package com.example.project_seekdeep;

import static android.view.View.VISIBLE;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    //private FollowRequest followRequest; //possible followRequest between loggedInUser and userBeingViewed
    private ProgressBar loadingCircle;


    //Constructor
    public OtherUsersProfileFragment() {
        super(R.layout.other_users_profile_page);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Display a loading symbol since the Follow/Pending/Following button delays when being initialized in initializeButtonStatus
        loadingCircle = view.findViewById(R.id.loading_spinner);
        loadingCircle.bringToFront(); //needed to add this so that it isn't hidden being the mood list
        loadingCircle.setVisibility(View.VISIBLE);

        //Retrieve current logged-in user and userBeingViewed from bundle
        if (getArguments() != null) {
            loggedInUser = (UserProfile) getArguments().getSerializable("loggedInUser");
            userBeingViewed = (UserProfile) getArguments().getSerializable("userBeingViewed");
        }

        //Instantiate userProvide (to use its firebase methods)
        //userProvider = new UserProvider(requireContext(), loggedInUser);
        userProvider = UserProvider.getInstance(requireContext(), loggedInUser); //use the same instance of userProvider that mainactivity uses

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
        //1st arg: the context(fragment), 2nd arg: The list of mood objects to display, 3rd arg: ref to object that implements MoodArrayAdapter.OnUsernameClickListener
        //pass 'null' as the 3rd arg : moods in this fragment don't need to listen for username clicks
        moodArrayAdapter = new MoodArrayAdapter(view.getContext(), moodArrayList, null);
        //Set the adapter to the UI Listview
        moodListView.setAdapter(moodArrayAdapter);

        //Get instance of db and the moods collection
        db = FirebaseFirestore.getInstance();
        moods = db.collection("MoodDB");

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
        isFollowing = false;
        isPending = false;
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

    }

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

//    private void sendFollowRequestToDataBase() {
//        //Change the button to "Pending"
//        changeButtonAndStatus("Pending");
//
//        //Create a new doc with a uniquely generated id
//        DocumentReference newDocRef = followingsAndRequestsRef.document();
//
//        Map<String, Object> followData = new HashMap<>();
//        followData.put("follower", loggedInUser.getUsername());
//        followData.put("followee", userBeingViewed.getUsername());
//        followData.put("status", "pending");
//
//        newDocRef.set(followData);
//    }

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

//    /**
//     * This method will delete the document where
//     * follower==loggedInUser and followee==userBeingViewed and status=="following"
//     */
//    private void unfollowThisUser() {
//        followingsAndRequestsRef
//                .whereEqualTo("follower", loggedInUser.getUsername())
//                .whereEqualTo("followee", userBeingViewed.getUsername())
//                .whereEqualTo("status","following")
//                .get()
//                .addOnSuccessListener(queryDocSnapshot -> {
//                    for (QueryDocumentSnapshot doc : queryDocSnapshot) {
//                        doc.getReference().delete()
//                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Successfully unfollowed " + userBeingViewed.getUsername()))
//                                .addOnFailureListener(e -> Log.w("Firestore", "Error unfollowing user", e));
//                    }
//
//                    //Update the UserProfile class
//                    loggedInUser.removeFollowing(userBeingViewed.getUsername());
//
//                    //Update the followings list in firestore
//                    removeFromFollowingsListInFirestore();
//                })
//                .addOnFailureListener(e -> Log.w("Firestore", "Error finding follow document", e));
//    }
//    /**
//     * This is a helper method for unfollowThisUser
//     * This will remove userBeingViewed from the followings-array-field in the user's document
//     */
//    private void removeFromFollowingsListInFirestore() {
//        DocumentReference userDocRef = db.collection("users").document(loggedInUser.getUsername());
//        userDocRef.update("followings", FieldValue.arrayRemove(userBeingViewed.getUsername()))
//                .addOnSuccessListener(aVoid -> Log.d("Firestore", "(unfollow) Followings list updated successfully"))
//                .addOnFailureListener(e -> Log.w("Firestore", "(unfollow) Error updating followings list", e));
//    }


    //THIS METHOD IS USED WHEN ANOTHER USER HAS ACCEPTED THE LOGGED-IN USERS FOLLOW REQUESTS
    //THIS METHOD MIGHT BE BETTER USED IN SOME OTHER CLASS
    private void addToFollowingsListInFirestore() {
        DocumentReference userDocRef = db.collection("users").document(loggedInUser.getUsername());
        userDocRef.update("followings", FieldValue.arrayUnion(userBeingViewed.getUsername()))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "(unfollow) Followings list updated successfully"))
                .addOnFailureListener(e -> Log.w("Firestore", "(unfollow) Error updating followings list", e));
    }

    public void listenForAcceptedRequests() {
        db.collection("followings_and_requests")
                .whereEqualTo("follower", loggedInUser.getUsername())
                .whereEqualTo("status", "following")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.d("listenForFollowRequests", "Listener failed!!");
                            return;
                        }

                        for (QueryDocumentSnapshot doc : value) {
                            String followee = doc.getString("followee");
                            loggedInUser.addFollowing(followee);
                            Log.d("Following","currentUser now follows "+followee);
                            // Debug: Print out the updated following list after adding the followee
                            //Log.d("Following List", "Updated following list: " + (currentUser.getFollowings()).get(0));
                            //if (change.getType() == DocumentChange.Type.ADDED) {}
                        }
                        loggedInUser.addFollowing(userBeingViewed.getUsername());
                        addToFollowingsListInFirestore();
                        changeButtonAndStatus("Following");
                    }
                });
    }


}