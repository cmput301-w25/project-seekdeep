package com.example.project_seekdeep;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/**
 * This fragment class is used to show the full details of a mood that the user has tapped on
 * in a ListView. This fragment will display the Mood's original card as seen on the list view,
 * the original details of who posted, when, etc, and the comments associated with the mood.
 * @author Kevin Tu
 */
public class MoodFullDetailsFragment extends Fragment {
    private ImageButton backButton;
    private TextView headerText;
    private RecyclerView commentView;
    private ArrayList<Comment> comments;
    private Mood clickedOnMood;
    private MoodArrayAdapter moodArrayAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mood_details_and_comments, container, false);
        backButton = view.findViewById(R.id.back_button);
        headerText = view.findViewById(R.id.whose_mood_text);
        commentView = view.findViewById(R.id.comments_recycler_view);

        // Get passed data from previous fragment
        assert savedInstanceState != null;
        Bundle commentsBundle =  savedInstanceState.getBundle("comments");
        Bundle clickedOnMoodBundle = savedInstanceState.getBundle("mood");
        comments = (ArrayList<Comment>) commentsBundle.get("comments");
        clickedOnMood = (Mood) clickedOnMoodBundle.get("mood");



        return view;
    }
}
