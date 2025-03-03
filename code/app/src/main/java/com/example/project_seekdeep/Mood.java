package com.example.project_seekdeep;

import android.media.Image;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Mood implements Serializable {

    private String trigger;
    private String socialSituation;
    private Image image;
    private Date postedDate;
    private String emotionalState;
    private List<String> followers = new ArrayList<>();
//    private User owner;
    public Mood(/*User owner,*/String emotionalState){
//        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
    }
    public Mood(/*User owner,*/String emotionalState, String socialSituation, String trigger){
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
}
