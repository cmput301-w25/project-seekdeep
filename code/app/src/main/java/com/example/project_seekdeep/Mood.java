package com.example.project_seekdeep;

import android.media.Image;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Mood implements Serializable {

    private String trigger;
    private SocialSituations socialSituation;
    private Image image;
    private Date postedDate;
    private EmotionalStates emotionalState;
    private String reason;
//    private User owner;

    public Mood(/*User owner,*/EmotionalStates emotionalState){
//        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
    }

    public Mood(/*User owner,*/EmotionalStates emotionalState, SocialSituations socialSituation, String trigger){
//        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
        this.socialSituation = socialSituation;
        this.trigger = trigger;
    }
    public void setTrigger(String trigger){
        this.trigger = trigger;
    }
    public void setSocialSituation(SocialSituations socialSituation){
        this.socialSituation = socialSituation;
    }
    public void setEmotionalState(EmotionalStates emotionalState) {
        this.emotionalState = emotionalState;
    }
    public void setPostedDate(Date postedDate){
        this.postedDate = postedDate;
    }
    public void setImage(Image image){
        this.image = image;
    }

    public String getTrigger() {
        return trigger;
    }

    public SocialSituations getSocialSituation() {
        return socialSituation;
    }

    public Image getImage() {
        return image;
    }

    public Date getPostedDate() {
        return postedDate;
    }

    public EmotionalStates getEmotionalState() {
        return emotionalState;
    }


    public String getReason() {
        return reason;
    }
}
