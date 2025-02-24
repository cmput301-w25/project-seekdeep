package com.example.project_seekdeep;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class createMoodScreen extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View createMoodScreenLayout = inflater.inflate(R.layout.create_mood_screen, container, false);

        return createMoodScreenLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.buttonAngry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //filled in later
            }
        });
    }
}
