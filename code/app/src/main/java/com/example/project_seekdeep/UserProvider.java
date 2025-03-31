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
 * @author Sarah Chang, Deryk Fong
 */
public class UserProvider {
    private static UserProvider instance;
    private final FirebaseFirestore db;
    private UserProfile currentUser;
//    private final CollectionReference followingsAndRequestsColRef; //final means that variable cannot be reassigned throughout MoodProvider's lifetime

    public UserProvider(UserProfile currentUser, FirebaseFirestore db) {
        this.db = db;
        this.currentUser = currentUser;
    }

    public static synchronized UserProvider getInstance(UserProfile currentUser, FirebaseFirestore db) {
        if (instance == null) {
            instance = new UserProvider( currentUser, db);
        }
        return instance;
    }

    /**
     * This method uploads a new follow request from the logged-in user to another user into firebase.
     * @param newFollowRequest
     */
    public void sendFollowRequestToDataBase(FollowRequest newFollowRequest) {
        //Create a new document for the followRequest. Keep the parameter empty in document() so that firestore generates a unique Key.
        DocumentReference followRequestDocRef = db.collection("followings_and_requests").document();
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
        db.collection("followings_and_requests")
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
                    Log.d("CUNAME", currentUser.getUsername());
                    Log.d("VIEWEDNAME", userBeingViewed.getUsername());
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
     * This method will edit the document where
     * followee==loggedInUser and follower==userBeingViewed
     * to change the pending request to a following one
     */
    public void acceptFollowRequest(UserProfile userBeingViewed) {
        db.collection("followings_and_requests")
                .whereEqualTo("follower", userBeingViewed.getUsername())
                .whereEqualTo("followee", currentUser.getUsername())
                .get()
                .addOnSuccessListener(queryDocSnapshot -> {
                    for (QueryDocumentSnapshot doc : queryDocSnapshot) {
                        doc.getReference().update("status", "following")
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Successfully allowed" + userBeingViewed.getUsername() + "to follow you!"))
                                .addOnFailureListener(e -> Log.w("Firestore", "Error allow user to follow you", e));
                    }

                    //Update following list in the UserProfile class
                    currentUser.addFollowing(userBeingViewed.getUsername());

                })
                .addOnFailureListener(e -> Log.w("Firestore", "Error finding follow document", e));
    }
    /**
     * This method will edit the document where
     * followee==loggedInUser and follower==userBeingViewed
     * to change the pending request to a following one
     */
    public void declineFollowRequest(UserProfile userBeingViewed) {
        db.collection("followings_and_requests")
                .whereEqualTo("follower", userBeingViewed.getUsername())
                .whereEqualTo("followee", currentUser.getUsername())
                .get()
                .addOnSuccessListener(queryDocSnapshot -> {
                    for (QueryDocumentSnapshot doc : queryDocSnapshot) {
                        doc.getReference().delete()
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Successfully declined " + userBeingViewed.getUsername()))
                                .addOnFailureListener(e -> Log.w("Firestore", "Error declining user", e));
                    }
                    //Update following list in the UserProfile class
                    currentUser.removeFollowing(userBeingViewed.getUsername());

                    //Update the followings list in firestore
                    removeFromFollowingsListInFirestore(userBeingViewed);
                })
                .addOnFailureListener(e -> Log.w("Firestore", "Error finding follow document", e));
    }
}
