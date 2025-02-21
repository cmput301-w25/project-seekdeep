package com.example.code;

import android.media.Image;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Mood {

    private String trigger;
    private String socialSituation;
    private Image image;
    private Date postedDate;
    private String emotionalState;

    private String reasonWhy;

    private List<String> followers = new ArrayList<>();
//    private User owner;
    public Mood(/*User owner,*/String emotionalState){
//        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
        this.reasonWhy = "";
    }
    public Mood(/*User owner,*/String emotionalState, String socialSituation, String trigger){
//        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
        this.socialSituation = socialSituation;
        this.trigger = trigger;
        this.reasonWhy = "";
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

    public void setReasonWhy(String reasonWhy) {
        if (reasonWhy.length() <= 20 || reasonWhy.split(" ").length <= 3) {
        this.reasonWhy = reasonWhy;
        }
        else {
           throw new IllegalArgumentException("User's reason must be <=20 chars OR <=3 words!");
        }
    }

}
