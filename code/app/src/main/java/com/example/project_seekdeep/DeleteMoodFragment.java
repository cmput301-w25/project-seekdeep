package com.example.project_seekdeep;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * This class is used for deleting moods
 *
 * @author Nancy Lin
 * reuses code from Lab 7
 *
 * This code adds onAttach listener that instanciates a Delete mood fragment
 * Also creates a dialog that takes care of deleting moods
 */
public class DeleteMoodFragment extends DialogFragment {

    /**
     * required constructor for javadoc
     */
    public DeleteMoodFragment(){
        ;
    }

    private DeleteMoodDialogListener listener;


    /**
     * Mandotory constructor that creates a delete mood fragment with its corresponding mood
     *
     * @param mood
     *  Mood to use for delete
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
     * On attach, implement the deleteMood dialog listener to list for deleteMood()
     * @param context
     *      context of the fragment
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DeleteMoodDialogListener){
            listener = (DeleteMoodDialogListener) context;
        }
        else {
            throw new RuntimeException("Implement listener");
        }
    }

    /**
     * On creation, create an alert dialog
     *
     * @param savedInstanceState The last saved instance state of the Fragment,
     * or null if this is a freshly created Fragment.
     *
     * @return
     *      returns a dialog
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
                    listener.deleteMood(mood);
                })
                .create();
    }
}
