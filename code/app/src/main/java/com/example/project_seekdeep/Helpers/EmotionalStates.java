package com.example.project_seekdeep.Helpers;

import androidx.annotation.NonNull;

/**
 * This is a class that represents the emotional state of the user.
 * @author Jachelle Chan, modified by Saurabh
 *
 */
public enum EmotionalStates {
    ANGER("\uD83D\uDE20", "#D55353"),  // RED
    CONFUSION("\uD83E\uDD14", "#FFC6E8"),  // PINK
    DISGUST("\uD83E\uDD22", "#59C225"),  // GREEN
    FEAR("\uD83D\uDE28", "#828282"),  // GREY
    HAPPINESS("\uD83D\uDE04", "#E6D600"),  // YELLOW
    SADNESS("☹️", "#0055FF"),  // BLUE
    SHAME("\uD83D\uDE14", "#AD2CED"),  // PURPLE
    SURPRISE("\uD83D\uDE2F", "#E4A21F");  // ORANGE
    private String emoticon;
    private String colour;
    EmotionalStates(String emoticon, String colour) {
        this.emoticon = emoticon;
        this.colour = colour;
    }

    /**
     * This method makes the string representation the EmotionalState class be first letter capitalized and the rest lowercase
     * As well as adds the emoticon representation before the string itself.
     * Eg: SADNESS -> ☹️ Sadness
     * @return
     *      The string representation of this class.
     * Reference:
     *      Taken from: https://stackoverflow.com/a/26768694
     *      Authored by: Jorgesys
     *      Taken by: Jachelle Chan
     *      Taken on: March 2, 2025
     */
    @NonNull
    @Override
    public String toString() {
        return this.emoticon + " " + this.name().substring(0,1).toUpperCase() + name().substring(1).toLowerCase();
    }

    // getters and setters which may be removed later?? //
    /**
     * This method fetches the emoticon associated with the mood
     * @return
     *      Returns the emoticon that represents the mood
     */
    public String getEmoticon() {
        return emoticon;
    }

    /**
     * This method sets the emoticon for the mood
     * @param emoticon the emoticon of the associated emotional state
     */
    public void setEmoticon(String emoticon) {
        this.emoticon = emoticon;
    }

    /**
     * This method fetches the colour for the mood
     * @return
     *      Returns the colour of the mood
     */
    public String getColour() {
        return colour;
    }

    /**
     * This method sets the colour for the mood
     * @param colour the colour of the associated emotional state
     */
    public void setColour(String colour) {
        this.colour = colour;
    }

    /**
     * This method returns just the uppercase name of the state.
     * @return
     */
    public String getStateName() {
        return this.name();
    }

    /**
     * This method returns the EmotionalState instance of the string title for marker in mapsFragment
     * @param stateName: The emotional state enum
     * @return Returns the Emotional State
     */
    public static EmotionalStates fromStateName(String stateName) {
        return EmotionalStates.valueOf(stateName);
    }
}