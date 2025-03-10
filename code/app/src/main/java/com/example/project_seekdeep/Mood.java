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

    private String trigger;
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
     *
     * @param owner
     *      owner of the damn mood
     * @param emotionalState
     *  New EmotionalState for emotionalState
     * @param socialSituation
     *  New SocialSituation for socialSituation
     * @param trigger
     *  New String for trigger of the situation
     */
    public Mood(UserProfile owner,EmotionalStates emotionalState, SocialSituations socialSituation, String trigger){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
        this.socialSituation = socialSituation;
        this.trigger = trigger;
    }

    /**
     * This is a constructor for Mood.
     * @param owner
     *  The user that posted the mood event (UserProfile)
     * @param emotionalState
     *  The emotional state of the user/mood event (EmotionalState)
     * @param stringFields
     *  A string array in the form [trigger, reason, social situation] (String[])
     */
    public Mood(UserProfile owner, EmotionalStates emotionalState, String[] stringFields) {
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = new Date();
        // position 0 is trigger, position 1 is reason, position 2 is social situation
        this.trigger = stringFields[0];
        this.reason = stringFields[1];
        switch (stringFields[2]) {
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


    /**
     * This is constructor for Mood
     *
     * @param owner
     *      The user that posted the mood event (UserProfile)
     * @param emotionalState
     *      The emotional state of the user/mood event (EmotionalState)
     * @param socialSituation
     *      The social situation of the user/mood event (SocialSituation)
     * @param trigger
     *      The string that describes a trigger the user experienced for the mood
     * @param followers
     *      A list of strings that correspond with the users that are
     * @param postedDate
     *      A Date that corresponds with when the mood was created/posted
     */
    public Mood(UserProfile owner, EmotionalStates emotionalState, SocialSituations socialSituation, String trigger, List<String> followers, Date postedDate){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = postedDate;
        this.socialSituation = socialSituation;
        this.trigger = trigger;
    }

    /**
     * This is constructor for Mood
     *
     * @param owner
     *      The user that posted the mood event (UserProfile)
     * @param emotionalState
     *      The emotional state of the user/mood event (EmotionalState)
     * @param socialSituation
     *      The social situation of the user/mood event (SocialSituation)
     * @param trigger
     *      The string that describes a trigger the user experienced for the mood
     * @param followers
     *      A list of strings that correspond with the users that are
     * @param postedDate
     *      A Date that corresponds with when the mood was created/posted
     * @param reason
     *      the reason with mood
     *
     *
     */
    public Mood(UserProfile owner, EmotionalStates emotionalState, SocialSituations socialSituation, String trigger, List<String> followers, Date postedDate, String reason){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = postedDate;
        this.socialSituation = socialSituation;
        this.trigger = trigger;
        this.reason = reason;
        this.followers = followers;
    }


    /**
     *
     * @param emotionalState
     *      The emotional state of the user/mood event (EmotionalState)
     * @param followers
     *      A list of strings that correspond with the users that are
     * @param owner
     *      The user that posted the mood event (UserProfile)
     * @param postedDate
     *      A Date that corresponds with when the mood was created/posted
     * @param socialSituation
     *      The social situation of the user/mood event (SocialSituation)
     * @param trigger
     *       The string that describes a trigger the user experienced for the mood
     */
    public Mood(EmotionalStates emotionalState, List<String> followers, UserProfile owner, Date postedDate, SocialSituations socialSituation, String trigger){
        this.owner = owner;
        this.emotionalState = emotionalState;
        this.postedDate = postedDate;
        this.socialSituation = socialSituation;
        this.trigger = trigger;
    }

    /**
     * This function sets the trigger of the mood
     * @param trigger
     *  New trigger for this mood
     */
    public void setTrigger(String trigger){
        this.trigger = trigger;
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

    /**
     * Sets the mood
     * @param owner
     *      owner to be set
     */
    public void setOwner(UserProfile owner){
        this.owner = owner;
    }


    /**
     * This function sets the reason of the mood
     * @param reason
     *      new reason for the mood
     */
    public void setReason(String reason){
        this.reason = reason;
    }

    /**
     * This function sets the id of the mood's individual document reference
     * @param docRef
     *      new document Reference for the mood
     */
    public void setDocRef(DocumentReference docRef){
        this.docRef = docRef;
    }

    /**
     * This function gets the trigger of the mood
     * @return
     *      current trigger as String
     */
    public String getTrigger() {
        return trigger;
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

    /**
     * This function gets the list of followers that are following a mood
     * @return
     *      List of followers as List string
     */
    public List<String> getFollowers() {
        return followers;
    }

    /**
     * This function gets the current reason attached to the mood
     * @return
     *      current reason as String
     */
    public String getReason() {
        return reason;
    }

    /**
     * This function sets the image of the mood if the image is valid
     * @param image
     *  New image for this mood
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

    /**
     * This function gets the username of the userProfile attached to the mood
     * @return
     *      String username of userProfile of mood
     */
    public String getOwnerString() {
        return owner.getUsername();
    }

    /**
     * This function gets the user / creator of the mood
     * @return
     *      owner as UserProfile
     */
    public UserProfile getOwner() {
        return owner;
    }

    /**
     * This function gets the firebase document reference of where the mood is stored
     * @return
     *      location of mood in firebase db -- docRef as DocumentReference
     */
    public DocumentReference getDocRef(){
        return docRef;
    }
}
