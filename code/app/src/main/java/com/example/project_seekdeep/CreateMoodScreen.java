package com.example.project_seekdeep;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

//CURRENTLY: THIS CLASS IS NOT BEING USED, INSTEAD IS REPLACED BY SelectMoodDialogFragment

public class CreateMoodScreen extends Fragment {

    private TextView selectedMood;
    private EmotionalStates mood;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View createMoodScreenLayout = inflater.inflate(R.layout.mood_wheel, container, false);

        return createMoodScreenLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize fragManager (used to switch to a different fragment)
        FragmentManager fragManager = getParentFragmentManager();

        //Initialize the TextView to the UI element (to show the currently selected text
        //In case users are not sure what each emoji represents, they can read this text.
        selectedMood = view.findViewById(R.id.currently_selected_mood);


        //Define actions for each mood button
        view.findViewById(R.id.buttonSurprise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //filled in later
                selectedMood.setText("SURPRISED");
                mood = EmotionalStates.SURPRISE;
//                fragManager.beginTransaction().replace(R.id.fragment_container, new CreateMoodEventFragment()).commit();
            }
        });
        view.findViewById(R.id.buttonShame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("SHAME");
                mood = EmotionalStates.SHAME;
            }
        });
        view.findViewById(R.id.buttonSad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("SAD");
                mood = EmotionalStates.SADNESS;
            }
        });
        view.findViewById(R.id.buttonHappy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("HAPPY");
                mood = EmotionalStates.HAPPINESS;
            }
        });
        view.findViewById(R.id.buttonFear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("FEAR");
                mood = EmotionalStates.FEAR;
            }
        });
        view.findViewById(R.id.buttonDisgusted).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("DISGUSTED");
                mood = EmotionalStates.DISGUST;
            }
        });
        view.findViewById(R.id.buttonConfused).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("CONFUSED");
                mood = EmotionalStates.CONFUSION;
            }
        });
        view.findViewById(R.id.buttonAngry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectedMood.setText("ANGRY");
                mood = EmotionalStates.ANGER;
            }
        });

        //Go to the CreateMoodEventFragment once a mood has been selected
//        view.findViewById(R.id.confirm_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                fragManager.beginTransaction().replace(R.id.frameLayout, new CreateMoodEventFragment()).commit();
//            }
//        });
    }
}
