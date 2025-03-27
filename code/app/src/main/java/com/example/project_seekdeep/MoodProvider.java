package com.example.project_seekdeep;

import android.graphics.Movie;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 *  This class is in charge of reflecting new/updated/deleted data between the app and the database
 *  @author Sarah Chang
 */

public class MoodProvider {
    //Attributes:
    //note: the FirestoreFirebase instance will no be an attribute, but be passed to MovieProvider (so there's no accidental mix up of possible db instances)
    private static MoodProvider moodProvider;
    private final CollectionReference moodCollection; //final means that moodCollection cannot be reassigned throughout MoodProvider's lifetime
    private ArrayList<Mood> moodEventList;


    //Use constructor to initialize the attributes to the firestore database:

    /**
     * This is a constructor for MoodProvider
     * @param db
     */
    public MoodProvider(FirebaseFirestore db) {
        //Initialize moodEventList with an empty ArrayList (will be populated after reading from firestore)
        moodEventList = new ArrayList<>();
        //Connect moodCollection to the firestore remote collection
        moodCollection = db.collection("MoodDB");
    }


    //All methods in this interface must be implemented by the caller of MoodProvider
    public interface DataStatus {
        void onDataUpdated();
        void OnError(String strin);
    }

    /**
     * This checks for any changes in the database
     * @param dataStatus
     */
    public void listenForUpdates(final DataStatus dataStatus) {
        moodCollection.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                dataStatus.OnError(error.getMessage());
                return;
            }
            moodEventList.clear();
            if (snapshot != null) {
                for (QueryDocumentSnapshot item : snapshot) {
                    moodEventList.add(item.toObject(Mood.class));
                }
                dataStatus.onDataUpdated();
            }
        });
    }


    /**
     * This checks if a MoodProvider already exists. If not, make a new instance of MoodProvider
     * @param db
     * @return MoodProvider (either an existing instance or a new instance)
     */
    public static MoodProvider getInstance(FirebaseFirestore db) {
        if (moodProvider == null)
            moodProvider = new MoodProvider(db);
        return moodProvider;
    }

    //Getter
    public ArrayList<Mood> getMoodEventList() {
        return moodEventList;
    }


    //METHODS:

    /**
     * This adds a new Mood object into the MoodDB collection in the firebase database
     * @param mood
     *          This is the mood to be added to firebase
     */
    public void addMoodEvent(Mood mood) {
        //Create a new document for the mood.  Keep the parameter empty in document() so that firestore generates a unique Key
        DocumentReference moodDocRef = moodCollection.document();

        //Populate the new document with mood
        moodDocRef.set(mood);
    }

    /**
     * This method accesses firestore and edits the mood
     * @param mood
     */
    public void updateMood(Mood mood) {
        DocumentReference documentReference = mood.getDocRef();
        documentReference.set(mood);
    }

    /**
     * This method accesses firestore and deletes the mood
     * @param mood
     */
    public void deleteMood(Mood mood) {
        DocumentReference docRef = mood.getDocRef();
        docRef.delete();
    }

    public static void setInstanceForTesting(FirebaseFirestore firestore) {
        moodProvider = new MoodProvider(firestore);
    }


}
