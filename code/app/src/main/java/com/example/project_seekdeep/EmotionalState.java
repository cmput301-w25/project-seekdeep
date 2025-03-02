package com.example.project_seekdeep;

/**
 * This is a class that represents the emotional state of the user.
 */
public enum EmotionalState {
    // these representations and colours can be changed later
    // the colours may change
    ANGER("\uD83D\uDE20", "#CD5C5C"),  // RED
    CONFUSION("\uD83E\uDD14", "#e59866"),  // ORANGE
    DISGUST("\uD83E\uDD22", "#52be80 "),  // GREEN
    FEAR("\uD83D\uDE28", "#aed6f1"),  // BLUE
    HAPPINESS("\uD83D\uDE04", "#f7dc6f"),  // YELLOW
    SHAME("\uD83D\uDE14", "#c39bd3"),  // PURPLE
    SURPRISE("\uD83D\uDE2F", "#f1948a");  // PINK

    private String emoticon;
    private String colour;
    EmotionalState(String emoticon, String colour) {
        this.emoticon = emoticon;
        this.colour = colour;
    }
    // getters and setters

    /**
     * This method fetches the emoticon associated with the mood
     * @return
     *      Returns the emoticon that represents the mood
     */
    public String getEmoticon() {
        return emoticon;
    }

    // setters might be removed later! //
    /**
     * This method sets the emoticon for the mood
     * @param emoticon
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
     * @param colour
     */
    public void setColour(String colour) {
        this.colour = colour;
    }
}