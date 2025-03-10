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
 */
public class DeleteMoodFragment extends DialogFragment {

    private DeleteMoodDialogListener listener;


    public static DeleteMoodFragment newInstance(Mood mood){
        Bundle args = new Bundle();
        args.putSerializable("Mood", mood);

        DeleteMoodFragment fragment = new DeleteMoodFragment();
        fragment.setArguments(args);
        return fragment;
    }

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
