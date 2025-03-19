package com.example.project_seekdeep;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * This fragment class is used to show the full details of a mood that the user has tapped on
 * in a ListView. This fragment will display the Mood's original card as seen on the list view,
 * the original details of who posted, when, etc, and the comments associated with the mood.
 * @author Kevin Tu
 */
public class ViewMoodDetailsFragment extends Fragment {
    private ImageButton backButton;
    private TextView headerText;
    private RecyclerView commentView;
    private ArrayList<Comment> comments;
    private Mood clickedOnMood;
    private FirebaseFirestore db;
    CollectionReference Comments;
    CollectionReference Users;



    @SuppressLint("NotifyDataSetChanged")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set up db
        db = FirebaseFirestore.getInstance();
        Comments = db.collection("comments");
        Users = db.collection("users");
        comments = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_mood_details_and_comments, container, false);
        backButton = view.findViewById(R.id.back_button);
        headerText = view.findViewById(R.id.whose_mood_text);
        commentView = view.findViewById(R.id.comments_recycler_view);

        // Get passed data from previous fragment
        Bundle clickedOnMoodBundle = getArguments();
        assert clickedOnMoodBundle != null;
        clickedOnMood = (Mood) clickedOnMoodBundle.get("mood");

        // Get comments for the clicked on mood
        assert clickedOnMood != null;
        Query CommentsQuery = Comments.whereEqualTo("mood", "MoodDB/" + clickedOnMood.getDocRef().getId());
        Log.d("DocRef", clickedOnMood.getDocRef().getId());

        // Set adapter for the comments view
        commentView.setLayoutManager(new LinearLayoutManager(requireContext()));
        CommentAdapter commentAdapter = new CommentAdapter(comments);
        commentView.setAdapter(commentAdapter);

        // TODO: Need to fix this so it accounts for the user's profile picture (which has yet to be added).
        Comments.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
            }
            if (value != null) {
                comments.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    DocumentReference moodRef = snapshot.getDocumentReference("mood");
                    String username = snapshot.getString("username");
                    String comment = snapshot.getString("comment");

                    Log.d("Comment", comment);

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

        TextView trigger = (TextView) view.findViewById(R.id.trigger);
        trigger.setText(clickedOnMood.getTrigger());

        TextView social = (TextView) view.findViewById(R.id.social_situation);
        social.setText(clickedOnMood.getSocialSituation().toString());

        TextView date = (TextView) view.findViewById(R.id.date_text);
        date.setText(clickedOnMood.getPostedDate().toString());

        Log.d("Comment count", String.valueOf(comments.size()));
        return view;
    }
}
