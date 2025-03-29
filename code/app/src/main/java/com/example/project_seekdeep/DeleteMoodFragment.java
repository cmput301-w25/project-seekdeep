package com.example.project_seekdeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * This class is used for deleting moods
 *
 * @author Nancy Lin
 * reuses code from Lab 7
 */
public class DeleteMoodFragment extends DialogFragment {

    //private DeleteMoodDialogListener listener;
    private MoodProvider moodProvider = MoodProvider.getInstance(FirebaseFirestore.getInstance());
    private FirebaseFirestore db = FirebaseFirestore.getInstance();


    /**
     * Mandotory constructor that creates a delete mood fragment with its corresponding mood
     *
     * @param mood
     * @return      fragment
     */
    public static DeleteMoodFragment newInstance(Mood mood){
        Bundle args = new Bundle();
        args.putSerializable("Mood", mood);

        DeleteMoodFragment fragment = new DeleteMoodFragment();
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * On creation, create an alert dialog
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        Mood mood;

        // Handle data validation
        if (bundle != null)
            mood = (Mood) bundle.getSerializable("Mood");
        else
            throw new RuntimeException("Bundle was not present!");
        if (mood == null)
            throw new RuntimeException("Mood was not in bundle!");

        return new AlertDialog.Builder(requireContext())
                .setMessage("Are you sure you want to delete this mood?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    moodProvider.deleteMood(mood);

                    // Delete location as well
                    db.collection("locations")
                            .whereEqualTo("moodID", mood.getDocRef().getId())
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                    document.getReference().delete();
                                }
                            });
                })
                .create();
    }
}
