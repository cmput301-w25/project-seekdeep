package com.example.project_seekdeep;

import android.media.Image;

import java.io.Serializable;
import com.example.project_seekdeep.EmotionalState;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This is the Mood class I took off Jachelle's branch 8-us-010301. and edited
 * @author Jachelle Chan?
 */

public class Mood implements Serializable {

    private String trigger;
    private SocialSituations socialSituation;
    private Image image;
    private Date postedDate;
    private EmotionalStates emotionalState;
    private String reason;
    private UserProfile owner;


    public Mood(UserProfile owner,EmotionalStates emotionalState){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
    }

    public Mood(UserProfile owner, EmotionalStates emotionalState, SocialSituations socialSituation, String trigger){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
        this.socialSituation = socialSituation;
        this.trigger = trigger;
    }

    public Mood(EmotionalStates emotionalState, List<String> followers, UserProfile owner, Date postedDate, SocialSituations socialSituation, String trigger){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = postedDate;
        this.socialSituation = socialSituation;
        this.trigger = trigger;
    }

    public Mood(Map<String, Object> mapMood){

        this.setOwner( (UserProfile) mapMood.get("owner"));
        this.emotionalState = emotionalState.valueOf(mapMood.get("emotionalState").toString());
        this.postedDate = (Date) mapMood.get("postedDate");
        this.socialSituation =  socialSituation.valueOf( mapMood.get("socialSituation").toString());
        this.trigger = mapMood.get("trigger").toString();


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

    public void setOwner(UserProfile owner){
        this.owner = owner;
    }



    public String getOwnerString() {
        return owner.getUsername();
    }

    public UserProfile getOwner() {
        return owner;
    }
}