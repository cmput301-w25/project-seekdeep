package com.example.project_seekdeep;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p> This class is in charge of handling the following's list of the logged-in user through firestore. </p>
 * <pre>
 * <ul>
 *     Classes that will use UserProvider (sharing the same instance of it):
 *      <li>MainActivity: to initialize a user's following's list after login, and listen for new follow requests to the user. </li>
 *      <li>OtherUsersProfileFragment: to send follow requests, and unfollow users. </li>
 *      <li>ManageRequestsFragment: to accept and decline follow requests from other users: </li>
 *          <ul>
 *              <li>when the logged-in user accepts a request: find the doc where follower==requester && followee==loggedInUser && status=="pending", and change "pending"-->"following"</li>
 *              <li>when loggedInUser denies a request, find the doc where follower==requester && followee==loggedInUser && status=="pending", and delete the doc</li>
 *          </ul>
 * </ul>
 * </pre>
 * @see MainActivity
 * @see FollowRequest
 * @see OtherUsersProfileFragment
 * @see UserProfile
 * @author Sarah Chang
 */
public class UserProvider {
    private static UserProvider instance;
    private final FirebaseFirestore db;
    private CollectionReference followingsAndRequestsCollectionRef;
    private CollectionReference usersCollectionRef;
    private UserProfile currentUser;
    private Context context;
    private boolean initializingFollowings;
//    private final CollectionReference followingsAndRequestsColRef; //final means that variable cannot be reassigned throughout MoodProvider's lifetime

    public UserProvider(Context mainActivityContext, UserProfile currentUser, FirebaseFirestore db) {
        //Get an instance of firebase (.getInstance only needs to be called once per class)
//        this.db = FirebaseFirestore.getInstance();
        this.db = db;
        this.followingsAndRequestsCollectionRef = db.collection("followings_and_requests");
        this.usersCollectionRef = db.collection("users");
        this.currentUser = currentUser;
        this.context = mainActivityContext.getApplicationContext(); //getApplicationContext is the global app context
        this.initializingFollowings = true;
    }

    public static synchronized UserProvider getInstance(Context context, UserProfile currentUser) {
        if (instance == null) {
            instance = new UserProvider(context, currentUser, FirebaseFirestore.getInstance());
        }
        return instance;
    }

    /**
     * This method uploads a new follow request from the logged-in user to another user into firebase.
     * @param newFollowRequest
     */
    public void sendFollowRequestToDataBase(FollowRequest newFollowRequest) {
        //Create a new document for the followRequest. Keep the parameter empty in document() so that firestore generates a unique Key.
        DocumentReference followRequestDocRef = followingsAndRequestsCollectionRef.document();
        //Set the docRef in the newFollowRequest object (incase need to look for it in the future)
        newFollowRequest.setDocRef(followRequestDocRef);
        //Populate the new document with the attributes from newFollowRequest
        followRequestDocRef.set(newFollowRequest);
    }

    /**
     * This method will delete the document where
     *      follower==loggedInUser and followee==userBeingViewed and status=="following"
     * This method also removed the userBeingViewed from the UserProfile object's followings-list
     */
    public void unfollowThisUser(UserProfile userBeingViewed) {
        followingsAndRequestsCollectionRef
                .whereEqualTo("follower", currentUser.getUsername())
                .whereEqualTo("followee", userBeingViewed.getUsername())
                .whereEqualTo("status","following")
                .get()
                .addOnSuccessListener(queryDocSnapshot -> {
                    for (QueryDocumentSnapshot doc : queryDocSnapshot) {
                        doc.getReference().delete()
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Successfully unfollowed " + userBeingViewed.getUsername()))
                                .addOnFailureListener(e -> Log.w("Firestore", "Error unfollowing user", e));
                    }

                    //Update following list in the UserProfile class
                    currentUser.removeFollowing(userBeingViewed.getUsername());

                    //Update the followings list in firestore
                    removeFromFollowingsListInFirestore(userBeingViewed);
                })
                .addOnFailureListener(e -> Log.w("Firestore", "Error finding follow document", e));
    }
    /**
     * This is a helper method for unfollowThisUser
     * This will remove userBeingViewed from the followings-array-field in the user's document
     */
    private void removeFromFollowingsListInFirestore(UserProfile userBeingViewed) {
        DocumentReference userDocRef = db.collection("users").document(currentUser.getUsername());
        userDocRef.update("followings", FieldValue.arrayRemove(userBeingViewed.getUsername()))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "(unfollow) Followings list updated successfully"))
                .addOnFailureListener(e -> Log.w("Firestore", "(unfollow) Error updating followings list", e));
    }

    /**
     * This method will listening for other users who have accepted the logged-in user's follow request
     *
     * Note:
     * - this method should only listen for new requests that come in while the user is using the app
     */
    //Reference: https://firebase.google.com/docs/firestore/query-data/listen#listen_to_multiple_documents_in_a_collection
    public void listenForAcceptedRequests() {
        db.collection("followings_and_requests")
                .whereEqualTo("follower", currentUser.getUsername())
                .whereEqualTo("status", "following")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.d("firestore", "listenForAcceptedRequests failed!!");
                            return;
                        }

                        for (QueryDocumentSnapshot doc : value) {
                            String followee = doc.getString("followee");
                            //Add to following list in the UserProfile object
                            currentUser.addFollowing(followee);
                            //Add to following list in firestore
                            addToFollowingsListInFirestore(followee);
                            Log.d("firestore","listenForAcceptedRequests: currentUser now follows "+followee);
                            // Debug: Print out the updated following list after adding the followee
                            //Log.d("Following List", "Updated following list: " + (currentUser.getFollowings()).get(0));
                            //if (change.getType() == DocumentChange.Type.ADDED) {}
                        }
                    }
                });
    }

    /**
     * This is a helper method for listenForAcceptedRequests. It takes the user's username that has accepted loggedInUser's request,
     *    and adds them to the loggedInUser's following list in firestore.
     * @param newFollowing The username of the user that has accepted loggedInUser's follow request.
     */
    private void addToFollowingsListInFirestore(String newFollowing) {
        DocumentReference userDocRef = db.collection("users").document(currentUser.getUsername());
        userDocRef.update("followings", FieldValue.arrayUnion(newFollowing))
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "addToFollowingsListInFirestore: Followings list updated successfully"))
                .addOnFailureListener(e -> Log.w("Firestore", "addToFollowingsListInFirestore: Error updating followings list", e));
    }


    /**
     * This listens for when the currentUser unfollows another user.
     * In firebase, this means:
     *      a document where follower==currentUser and status=="following" has been deleted.
     */
    public void listenForUnfollowings() {
        db.collection("followings_and_requests")
                .whereEqualTo("follower", currentUser.getUsername())
                .whereEqualTo("status", "following")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Firestore", "Error in declined request listener!", error);
                        return;
                    }
                    for (DocumentChange change : value.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.REMOVED) {
                            String unfollowedUser = change.getDocument().getString("followee");
                            Log.d("Firestore", "currentUser has unfollowed "+unfollowedUser);
                            currentUser.removeFollowing(unfollowedUser);
                        }
                    }
                    //Update the new followingList to firestore
                    addFollowingListToFirebase();
                });
    }


    /**
     * This method listens for a new follow request to the logged-in user, and displays a toast message, notifying the user.
     * Note: the message only shows up if a follow request has been sent to the user while they are using the app.
     *
     * In firestore this means:
     *      A new document is added into the "followings_and_requests" collection,
     *      where followee == loggedInUser.getUsername()
     *      and status == "pending"
     */
    public void listenForNewFollowRequests() {
        Log.d("listenForNewFollowRequests", "listenForNewFollowRequests started!");

        db.collection("followings_and_requests")
                .whereEqualTo("followee", currentUser.getUsername())
                .whereEqualTo("status", "pending")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("listenForNewFollowRequests", "Error in new request listener!", error);
                        return;
                    }

                    Log.d("listenForNewFollowRequests", "Snapshot received!");

                    if (initializingFollowings) {
                        initializingFollowings = false;
                        // Ignore the first snapshot (which is taken upon login), to prevent spamming you with toasts after logging in
                        return;
                    }

                    for (DocumentChange change : value.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.ADDED) {
                            String requester = change.getDocument().getString("follower");
                            Log.d("listenForNewFollowRequests", "New follow request by " + requester);

                            // Show notification in the form of a  Toast message
                            Toast.makeText(context, "You have a new follow request from " + requester, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    //This might be an unneccessary listener
    public void listenForDeniedRequests() {
        db.collection("followings_and_requests")
                .whereEqualTo("follower", currentUser.getUsername())
                .whereEqualTo("status", "pending")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w("Firestore", "Error in declined request listener!", error);
                        return;
                    }
                    for (DocumentChange change : value.getDocumentChanges()) {
                        if (change.getType() == DocumentChange.Type.REMOVED) {
                            String unfollowedUser = change.getDocument().getString("followee");
                            Log.d("listenForDeniedRequests", "currentUser has been denied by "+unfollowedUser);

                            //Firebase doesnt need to be updated cuz pending requests were never in the followings list
                        }
                    }
                });
    }

    /**
     * This method fetches all users that currentUser is following
     */
    public void initializeFollowingsList() {
        followingsAndRequestsCollectionRef
            .whereEqualTo("follower", currentUser.getUsername())
            .whereEqualTo("status", "following")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> followingList = new ArrayList<>();
                //Add each user into the following list
                for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                    String followee = doc.getString("followee");
                    if (followee != null) {
                        followingList.add(followee);
                    }
                }
                //Set the currentUser's followingList
                currentUser.setFollowings(followingList);
                //Upload this list to firestore (as a followings array in the user's document)
                addFollowingListToFirebase();
                Log.d("Firestore", "Followings list initialized: " + followingList);
            })
            .addOnFailureListener(e -> {
                Log.e("Firestore", "Error initializing followings list");
            });
    }
    /**
     * This is a helper method for initializeFollowingsList.
     * This method adds the current user's followings list (taken from the UserProfile followings attribute) into firestore
     */
    public void addFollowingListToFirebase() {
        List<String> followingList = currentUser.getFollowings();
        DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("users").document(currentUser.getUsername());

        userDocRef.update("followings", followingList)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "Followings list updated successfully");
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error updating followings list", e);
                });
    }

}
