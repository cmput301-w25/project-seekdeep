package com.example.project_seekdeep;

/**
 * This is a class that represents the emotional state of the user.
 */
public enum EmotionalState {
    // these representations and colours can be changed later
    // the colours may change
    // US 01.02.01 : As a participant, I want the emotional states to include at least: anger,
    //confusion, disgust, fear, happiness, sadness, shame, and surprise.
    ANGER("\uD83D\uDE20", "#D55353"),  // RED
    CONFUSION("\uD83E\uDD14", "#FFC6E8"),  // PINK
    DISGUST("\uD83E\uDD22", "#59C225 "),  // GREEN
    FEAR("\uD83D\uDE28", "#828282"),  // GREY
    HAPPINESS("\uD83D\uDE04", "#E6D600"),  // YELLOW
    SADNESS("☹️", "#0055FF"),  // BLUE
    SHAME("\uD83D\uDE14", "#AD2CED"),  // PURPLE
    SURPRISE("\uD83D\uDE2F", "#E4A21F");  // ORANGE
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