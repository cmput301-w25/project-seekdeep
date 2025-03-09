package com.example.project_seekdeep;

import android.graphics.Bitmap;

import java.io.Serializable;
import com.example.project_seekdeep.EmotionalState;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * This is a class that represents every mood post the user creates.
 * @author Deryk Fong and Jachelle Chan
 *
 */

public class Mood implements Serializable {

    private String trigger;
    private String socialSituation;
    private Bitmap image;
    private Date postedDate;
    private EmotionalState emotionalState;
    private List<String> followers = new ArrayList<>();
    private String reason;
    private UserProfile owner;

    /**
     * This method is a constructor for Mood which takes a User and emotionalState.
     * This method also sets the date to the current date
     * @param emotionalState
     *  New String for emotionalState
     * @param owner
     *  New user which owns this mood
     */
    public Mood(UserProfile owner,EmotionalState emotionalState){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
    }
    /**
     * This method is a constructor for Mood which takes user, emotionalState, socialSituation, and trigger.
     * This method also sets the date to the current date
     * @param emotionalState
     *  New String for emotionalState
     * @param socialSituation
     *  New String for socialSituation
     * @param trigger
     *  New String for trigger of the situation
     */
    public Mood(UserProfile owner,EmotionalState emotionalState, String socialSituation, String trigger){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
        this.socialSituation = socialSituation;
        this.trigger = trigger;
    }

    /**
     * This function sets the trigger of the mood
     * @param trigger
     *  New trigger for this mood
     */

    public Mood(UserProfile owner, EmotionalState emotionalState, String socialSituation, String trigger, List<String> followers, Date postedDate){
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
        this.socialSituation = mapMood.get("socialSituation").toString();
        this.trigger = mapMood.get("trigger").toString();
    }
    public void setTrigger(String trigger){
        this.trigger = trigger;
    }
    /**
     * This function sets the socialSituation of the mood
     * @param socialSituation
     *  New socialSituation for this mood
     */
    public void setSocialSituation(String socialSituation){
        this.socialSituation = socialSituation;
    }
    /**
     * This function sets the postedDate of the mood
     * @param postedDate
     *  New postedDate for this mood
     */
    public void setPostedDate(Date postedDate){
        this.postedDate = postedDate;
    }

    /**
     * This function gets the trigger of the mood
     * @return
     *  Current trigger of this mood
     */
    public void setOwner(UserProfile owner){
        this.owner = owner;
    }

    public String getTrigger() {
        return trigger;
    }

    /**
     * This function gets the socialSituation of the mood
     * @return
     *  Current socialSituation of this mood
     */
    public String getSocialSituation() {
        return socialSituation;
    }
    /**
     * This function gets the image of the mood
     * @return
     *  Current image of this mood
     */
    public Bitmap getImage() {
        return image;
    }
    /**
     * This function gets the postedDate of the mood
     * @return
     *  Current postedDate of this mood
     */
    public Date getPostedDate() {
        return postedDate;
    }
    /**
     * This function gets the emotionalState of the mood
     *
     * @return Current emotionalState of this mood
     */
    public EmotionalState getEmotionalState() {
        return emotionalState;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public String getReason() {
        return reason;
    }

    /**
     * This function sets the image of the mood if the image is valid
     * @param image
     *  New image for this mood
     * @return
     *  True if the image is not rejected
     *  False if the image is rejected
     */
    public boolean setImage(Bitmap image){
        if (image.getAllocationByteCount()<65536){
            this.image = image;
            return true;
        }
        return false;
    }

    public String getOwnerString() {
        return owner.getUsername();
    }

    public UserProfile getOwner() {
        return owner;
    }
}