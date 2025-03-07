package com.example.project_seekdeep;


import android.media.Image;

import java.io.Serializable;
import com.example.project_seekdeep.EmotionalState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This is the Mood class I took off Jachelle's branch 8-us-010301.
 * @author Jachelle Chan?
 */

public class Mood implements Serializable {

    private String trigger;
    private String socialSituation;
    private Image image;
    private Date postedDate;
    private EmotionalState emotionalState;
    private List<String> followers = new ArrayList<>();
    private String reason;
    //    private User owner;
    public Mood(/*User owner,*/EmotionalState emotionalState){
//        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
    }
    public Mood(/*User owner,*/EmotionalState emotionalState, String socialSituation, String trigger){
//        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
        this.socialSituation = socialSituation;
        this.trigger = trigger;
    }
    public void setTrigger(String trigger){
        this.trigger = trigger;
    }
    public void setSocialSituation(String socialSituation){
        this.socialSituation = socialSituation;
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

    public String getSocialSituation() {
        return socialSituation;
    }

    public Image getImage() {
        return image;
    }

    public Date getPostedDate() {
        return postedDate;
    }

    public EmotionalState getEmotionalState() {
        return emotionalState;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public String getReason() {
        return reason;
    }
}