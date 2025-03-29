package com.example.project_seekdeep;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationHandler {
    private UserProfile currentUser;
    private final FirebaseFirestore db;
    private Context context;
    private boolean initializingFollowings;

    public NotificationHandler(Context mainActivityContext, UserProfile currentUser, FirebaseFirestore db) {
        this.db = db;
        this.currentUser = currentUser;
        this.context = mainActivityContext.getApplicationContext();
        this.initializingFollowings = true;
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
     * This method fetches all users that currentUser is following
     */
    public void initializeFollowingsList() {
        db.collection("followings_and_requests")
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
