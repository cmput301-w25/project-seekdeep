package com.example.project_seekdeep;

public enum EmotionalState {
    // these representations and colours can be changed later
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
    public String getEmoticon() {
        return emoticon;
    }

    public void setEmoticon(String emoticon) {
        this.emoticon = emoticon;
    }

    public String getColour() {
        return colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }
}