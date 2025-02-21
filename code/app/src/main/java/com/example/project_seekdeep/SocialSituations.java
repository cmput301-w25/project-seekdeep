package com.example.project_seekdeep;

import androidx.annotation.NonNull;

public enum SocialSituations {
    ALONE("Alone"),
    SEVERAL_PEOPLE("Two or Several People"),
    CROWD("With a Crowd");

    private String formattedName;
    private SocialSituations(String formattedName) {
        this.formattedName = formattedName;
    }

    @NonNull
    @Override
    public String toString() {
        return formattedName;
    }
}
