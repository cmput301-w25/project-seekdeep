package com.example.project_seekdeep;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public enum SocialSituations {
    ALONE("Alone"),
    WITH_ANOTHER("With Another Person"),
    SEVERAL_PEOPLE("Two or Several People"),
    CROWD("With a Crowd");

    private final String formattedName;
    private SocialSituations(String formattedName) {
        this.formattedName = formattedName;
    }

    /**
     * Override toString to provide access to the string value of an enum.
     * @return
     *      String representation of a given enum.
     */
    @NonNull
    @Override
    public String toString() {
        return formattedName;
    }

    /**
     * Primarily used to port this enum to be used in an adapter of some sort. E.g, ArrayAdapter, Spinner.
     * @return
     *      A list of string values corresponding to each Social Situation enum.
     */
    public List<String> getStringValues() {
        List<String> ret = new ArrayList<>();
        for (SocialSituations s : SocialSituations.values()) {
            ret.add(s.formattedName);
        }
        return ret;
    }
}
