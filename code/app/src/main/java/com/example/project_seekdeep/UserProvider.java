package com.example.project_seekdeep;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class UserProvider {
    private FirebaseFirestore db;
    private UserProfile currentUser;

    public UserProvider(UserProfile currentUser) {
        //Get an instance of firebase (.getInstance only needs to be called once per class)
        this.db = FirebaseFirestore.getInstance();
        this.currentUser = currentUser;
    }

    /**
     * This method creates the user's following list whenever a user logs in,
     * while also listening for users who have accepted the logged-in user's follow request
     *
     * TO DO:
     * - if the user logged-in, then don't need to recreate the list from scratch, just get the initial list from firebase
     * - how to update this list in firebase?
     * - this method does not handle unfollowings!!
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
                            Log.d("listenForFollowRequests", "Listener failed!!");
                            return;
                        }

                        for (QueryDocumentSnapshot doc : value) {
                            String followee = doc.getString("followee");
                            currentUser.addFollowing(followee);
                            Log.d("Following","currentUser now follows "+followee);
                            // Debug: Print out the updated following list after adding the followee
                            //Log.d("Following List", "Updated following list: " + (currentUser.getFollowings()).get(0));
                            //if (change.getType() == DocumentChange.Type.ADDED) {}
                        }
                        addFollowingListToFirebase();
                    }
                });
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
                            Log.d("Following", "currentUser has been denied by "+unfollowedUser);

                            //Firebase doesnt need to be updated cuz pending requests were never in the followings list
                        }
                    }
                });
    }

    /**
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
