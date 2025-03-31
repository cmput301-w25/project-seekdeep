package com.example.project_seekdeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * This is a fragment which shows managed follow requests
 * @see FollowRequestArrayAdapter
 * @author Deryk Fong
 */

public class ManageFollowRequestsFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        UserProfile currentUser = (UserProfile) getArguments().getSerializable("userProfile");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Follow Requests");
        View view = getLayoutInflater().inflate(R.layout.fragment_manage_follow_requests,null);
        ArrayList<FollowRequest> requests = new ArrayList<>();
        CollectionReference followRequestsRef = db.collection("followings_and_requests");
        Query followRequestsQuery = followRequestsRef.whereEqualTo("status", "pending")
                .whereEqualTo("followee", currentUser.getUsername());
        ListView requestsList = view.findViewById(R.id.requests_list_view);
        UserProvider userProvider = UserProvider.getInstance(currentUser, db);
        ArrayAdapter<FollowRequest> requestArrayAdapter = new FollowRequestArrayAdapter(view.getContext(), requests, userProvider);
        requestsList.setAdapter(requestArrayAdapter);

        followRequestsQuery.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {
                requests.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    FollowRequest tempRequest = new FollowRequest(snapshot.get("follower").toString(),
                            snapshot.get("followee").toString(),
                            snapshot.get("status").toString());
                    tempRequest.setDocRef (snapshot.getReference());
                    requests.add(tempRequest);
                }
            }
            requestArrayAdapter.notifyDataSetChanged();
        });
    return builder.setView(view).create();
    }
}
