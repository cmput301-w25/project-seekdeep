package com.example.project_seekdeep;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This fragment class is used to show the full details of a mood that the user has tapped on
 * in a ListView. This fragment will display the Mood's original card as seen on the list view,
 * the original details of who posted, when, etc, and the comments associated with the mood.
 * @author Kevin Tu
 */
public class ViewMoodDetailsFragment extends Fragment {
    private ArrayList<Comment> comments;
    CollectionReference Comments;
    CollectionReference Users;

    // Used to prevent double clicking.
    // https://stackoverflow.com/questions/5608720/android-preventing-double-click-on-a-button
    // Taken by: Kevin Tu on 2025-03-23
    private long lastClickedTime = 0;

    ViewMoodDetailsFragment() {
        super(R.layout.fragment_mood_details_and_comments);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set up db
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Comments = db.collection("comments");
        Users = db.collection("users");
        comments = new ArrayList<>();
        // Get passed data from previous fragment

        assert getArguments() != null;
        Mood clickedOnMood = (Mood) getArguments().getSerializable("mood");
        UserProfile loggedInUser = (UserProfile) getArguments().getSerializable("userProfile");

        ImageButton backButton = view.findViewById(R.id.back_button);
        TextView headerText = view.findViewById(R.id.whose_mood_text);
        RecyclerView commentView = view.findViewById(R.id.comments_recycler_view);

        // Cool solution for the TextInput by Gabriele Mariotti:
        // https://stackoverflow.com/questions/57689127/how-to-put-a-button-in-an-edittext
        // Taken by: Kevin Tu on 2025-03-19
        TextInputLayout addCommentLayout = view.findViewById(R.id.add_comment_layout);
        TextInputEditText addCommentInput = view.findViewById(R.id.add_comment_input);
        addCommentLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Prevent double clicking
                if (SystemClock.elapsedRealtime() - lastClickedTime < 1500) {
                    return;
                }
                lastClickedTime = SystemClock.elapsedRealtime();

                String comment = Objects.requireNonNull(addCommentInput.getText()).toString();
                if (comment.isBlank() || comment.isEmpty()) {
                    Toast usageToast = Toast.makeText(getContext(), "Type in something to comment!", Toast.LENGTH_LONG);
                    usageToast.show();
                    return;
                }
                // Refer to Firebase's guide on adding a document:
                // https://firebase.google.com/docs/firestore/manage-data/add-data
                Map<String, Object> newComment = new HashMap<>();
                newComment.put("comment", comment);
                newComment.put("mood", clickedOnMood.getDocRef());
                newComment.put("username",  loggedInUser.getUsername());
                Comments.add(newComment)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("Added to comments DB", "DocumentSnapshot written with ID: " + documentReference.getId());
                                addCommentInput.setText("");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("Failed to add to comments DB", "Error adding document", e);
                                addCommentInput.setText("");
                            }
                        });
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        // Get comments for the clicked on mood
        assert clickedOnMood != null;
        Query CommentsQuery = Comments.whereEqualTo("mood", clickedOnMood.getDocRef());
        Log.d("Mood clicked on", clickedOnMood.getDocRef().getPath());

        // Set adapter for the comments view
        commentView.setLayoutManager(new LinearLayoutManager(requireContext()));
        CommentAdapter commentAdapter = new CommentAdapter(comments);
        commentView.setAdapter(commentAdapter);

        // TODO: Need to fix this so it accounts for the user's profile picture (which has yet to be added).
        CommentsQuery.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {
                comments.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    DocumentReference moodRef = snapshot.getDocumentReference("mood");
                    String username = snapshot.getString("username");
                    String comment = snapshot.getString("comment");

                    Comment currentComment = new Comment(moodRef, username, comment);


                    comments.add(currentComment);
                }
            }
            commentAdapter.notifyDataSetChanged();
        });

        // Set mood details
        // Owner of mood currently does not store their pfp :(
//        ImageView moodPfp = (ImageView) view.findViewById(R.id.profile_picture);
//        moodPfp.setImageURI(clickedOnMood.getOwner());

        TextView reason = (TextView) view.findViewById(R.id.reason);
        reason.setText(clickedOnMood.getReason());

        TextView emotion = (TextView) view.findViewById(R.id.emotion);
        emotion.setText(clickedOnMood.getEmotionalState().toString());

        //NOTE: THIS WAS COMMENTED OUT SINCE TRIGGER HAS BEEN REMOVED FROM MOOD AND THE NEW UI
        //TextView trigger = (TextView) view.findViewById(R.id.trigger);
        //trigger.setText(clickedOnMood.getTrigger());

        TextView social = (TextView) view.findViewById(R.id.social_situation);
        social.setText(clickedOnMood.getSocialSituation().toString());

        TextView date = (TextView) view.findViewById(R.id.date_text);
        date.setText(clickedOnMood.getPostedDate().toString());

        headerText.setText(clickedOnMood.getOwnerString() + "'s" + " Mood");

    }

}
