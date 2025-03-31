package com.example.project_seekdeep;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This fragment class is used to show the full details of a mood that the user has tapped on
 * in a ListView. This fragment will display the Mood's original card as seen in the list view,
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;

        BottomNavigationView navBar = mainActivity.findViewById(R.id.bottomNavigationView);
        navBar.setVisibility(View.GONE);

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
                newComment.put("date", Timestamp.now().toDate());
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
                navBar.setVisibility(View.VISIBLE);
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.popBackStack();
            }
        });

        // Get comments for the clicked on mood
        assert clickedOnMood != null;
        Query CommentsQuery = Comments.whereEqualTo("mood", clickedOnMood.getDocRef());
        Log.d("Mood clicked on", clickedOnMood.getDocRef().getPath());


        // Set adapter for the comments view
        commentView.setHasFixedSize(false);
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
                    Date date = Objects.requireNonNull(snapshot.getTimestamp("date")).toDate();

                    Comment currentComment = new Comment(moodRef, username, comment, date);
                    comments.add(currentComment);
                }
            }
            Collections.sort(comments);
            commentAdapter.notifyDataSetChanged();
        });


        //Set top navigation Bar color





        // Set mood details
        TextView user = view.findViewById(R.id.username);
        user.setText(clickedOnMood.getOwnerString());

        //the outline change color
        GradientDrawable box_outline = (GradientDrawable) view.findViewById(R.id.mood_details_box).getBackground();
        box_outline.mutate();
        box_outline.setStroke(5, Color.parseColor(clickedOnMood.getEmotionalState().getColour()));

        //Change toolbar color
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(Color.parseColor(clickedOnMood.getEmotionalState().getColour()));
        switch (clickedOnMood.getEmotionalState()){
            case ANGER:
                headerText.setTextColor(Color.parseColor("#120505"));
                backButton.setColorFilter(Color.parseColor("#120505"));
                break;
            case SADNESS:
                headerText.setTextColor(Color.parseColor("#FFFFFA"));
                backButton.setColorFilter(Color.parseColor("#FFFFFA"));
                break;

            case SHAME:
                headerText.setTextColor(Color.parseColor("#F9F9F0"));
                backButton.setColorFilter(Color.parseColor("#F9F9F0"));
                break;

            case SURPRISE:
                headerText.setTextColor(Color.parseColor("#2B2B2B"));
                backButton.setColorFilter(Color.parseColor("#2B2B2B"));
                break;
        }



        TextView reason = (TextView) view.findViewById(R.id.reason);
        reason.setText(clickedOnMood.getReason());
        if (clickedOnMood.getReason() == null || clickedOnMood.getReason().isEmpty()) {
            reason.setText("(Reason: N/A)");
            reason.setTypeface(null, Typeface.ITALIC);
            reason.setTextColor(ContextCompat.getColor(getContext(), R.color.fear_grey));
            reason.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        TextView emotion = (TextView) view.findViewById(R.id.emotion);
        emotion.setText(clickedOnMood.getEmotionalState().toString());


        TextView social = (TextView) view.findViewById(R.id.social_situation);
        social.setText(clickedOnMood.getSocialSituation().toString());
        if (clickedOnMood.getSocialSituation().toString().equals("Social Situations")){
            social.setText("(Social Situation: N/A)");
            social.setTypeface(null, Typeface.ITALIC);
        }

        TextView date = (TextView) view.findViewById(R.id.date_text);
        date.setText(clickedOnMood.getPostedDate().toString());

        ImageView image = (ImageView) view.findViewById(R.id.mood_image);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        headerText.setText(clickedOnMood.getOwnerString() + "'s" + " Mood");

        image.setVisibility(View.VISIBLE);
        Uri moodImage = clickedOnMood.getImage();

        if (moodImage != null) {
            StorageReference imageFire = storage.getReference("Images/" + clickedOnMood.getImage().getLastPathSegment());
            imageFire.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png'
                    Glide.with(requireContext())
                            .load(uri)
                            .into(image);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        } else {
            // No image to show
            image.setImageDrawable(null);
            image.setVisibility(View.GONE);
        }


        ImageView locationToggle = view.findViewById(R.id.location_image);
        locationToggle.setVisibility(View.GONE);
        //THIS CONTROLS THE TOGGLES FOR GEOLOCATION AND PRIVACY:
        // Set location toggle by checking if a location exists in the locations collection
        db.collection("locations")
                .whereEqualTo("moodID", clickedOnMood.getDocRef().getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()){
                        locationToggle.setImageResource(R.drawable.location_on);
                    } else{
                        locationToggle.setImageResource(R.drawable.location_off);
                    }
                    locationToggle.setVisibility(View.VISIBLE);

                });





    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
