package com.example.project_seekdeep;


import android.net.Uri;


import com.google.firebase.firestore.DocumentReference;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * This is a class that represents every mood post the user creates.
 * @author Deryk Fong and Jachelle Chan
 *
 */

public class Mood implements Serializable {

    private SocialSituations socialSituation;
    private Uri image;
    private Date postedDate;
    private EmotionalStates emotionalState;
    private List<String> followers = new ArrayList<>();
    private String reason;
    private UserProfile owner;
    private DocumentReference docRef;

    /**
     * This method is a constructor for Mood which takes a User and emotionalState.
     * This method also sets the date to the current date
     * @param emotionalState
     *  New String for emotionalState
     * @param owner
     *  New user which owns this mood
     */
    public Mood(UserProfile owner, EmotionalStates emotionalState){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
    }
    /**
     * This method is a constructor for Mood which takes user, emotionalState, socialSituation.
     * This method also sets the date to the current date
     * @param emotionalState
     *  New String for emotionalState
     * @param socialSituation
     *  New String for socialSituation

     */
    public Mood(UserProfile owner,EmotionalStates emotionalState, SocialSituations socialSituation){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
        this.socialSituation = socialSituation;

    }

    /**
     * This is a constructor for Mood.
     * @param owner
     *  The user that posted the mood event (UserProfile)
     * @param emotionalState
     *  The emotional state of the user/mood event (EmotionalState)
     * @param stringFields
     *  A string array in the form [reason, social situation] (String[])
     */
    public Mood(UserProfile owner, EmotionalStates emotionalState, String[] stringFields) {
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
        // position 0 is reason, position 1 is social situation

        this.reason = stringFields[0];
        switch (stringFields[1]) {
            case "Social Situations":
                this.socialSituation = SocialSituations.TITLE;
                break;
            case "Alone":
                this.socialSituation = SocialSituations.ALONE;
                break;
            case "With Another Person":
                this.socialSituation = SocialSituations.WITH_ANOTHER;
                break;
            case "Two or Several People":
                this.socialSituation = SocialSituations.SEVERAL_PEOPLE;
                break;

            case "With a Crowd":
                this.socialSituation = SocialSituations.CROWD;
                break;
        }

    }

    // this is used for testing ***
    public Mood(UserProfile owner, EmotionalStates emotionalState, Date date){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = date;
    }

    // this is used for testing ***
    public Mood(UserProfile owner, EmotionalStates emotionalState, SocialSituations socialSituation, Date date){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.socialSituation = socialSituation;
        this.postedDate = date;
    }



    public Mood(UserProfile owner, EmotionalStates emotionalState, SocialSituations socialSituation, List<String> followers, Date postedDate){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = postedDate;
        this.socialSituation = socialSituation;

    }

    public Mood(UserProfile owner, EmotionalStates emotionalState, SocialSituations socialSituation, List<String> followers, Date postedDate, String reason){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = postedDate;
        this.socialSituation = socialSituation;
        this.reason = reason;
        this.followers = followers;
    }


    public Mood(EmotionalStates emotionalState, List<String> followers, UserProfile owner, Date postedDate, SocialSituations socialSituation){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = postedDate;
        this.socialSituation = socialSituation;
    }


    /**
     * This function sets the socialSituation of the mood
     * @param socialSituation
     *  New socialSituation for this mood
     */
    public void setSocialSituation(SocialSituations socialSituation){
        this.socialSituation = socialSituation;
    }
    /**
     * This function sets the emotionalState of the mood
     * @param emotionalState
     *  New emotionalState for this mood
     */
    public void setEmotionalState(EmotionalStates emotionalState) {
        this.emotionalState = emotionalState;
    }
    /**
     * This function sets the postedDate of the mood
     * @param postedDate
     *  New postedDate for this mood
     */
    public void setPostedDate(Date postedDate){
        this.postedDate = postedDate;
    }

    public void setOwner(UserProfile owner){
        this.owner = owner;
    }

    public void setReason(String reason){
        this.reason = reason;
    }

    public void setDocRef(DocumentReference docRef){
        this.docRef = docRef;
    }



    /**
     * This function gets the socialSituation of the mood
     * @return
     *  Current socialSituation of this mood
     */
    public SocialSituations getSocialSituation() {
        return socialSituation;
    }
    /**
     * This function gets the image of the mood
     * @return
     *  Current image of this mood
     */
    public Uri getImage() {
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
    public EmotionalStates getEmotionalState() {
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
    public void setImage(Uri image){


        /*

        if (image.getAllocationByteCount()<65536){
            this.image = image;
            return true;
        }
        return false;

         */

        this.image = image;
    }

    public String getOwnerString() {
        return owner.getUsername();
    }

    public UserProfile getOwner() {
        return owner;
    }

    public DocumentReference getDocRef(){
        return docRef;
    }
}
