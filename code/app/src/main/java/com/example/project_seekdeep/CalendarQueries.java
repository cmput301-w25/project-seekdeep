package com.example.project_seekdeep;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarQueries {
    public CalendarQueries(){
        //
    }
    public void createMoodsInMonthArray(ArrayList<String> daysInMonthArray, Calendar selectedDateCalendar,
                                        UserProfile loggedInUser, OnGetQueryDataListener onQueryDataListener){

        onQueryDataListener.onStart();
        ArrayList<String> moodsInMonth = new ArrayList<>();
        CollectionReference moods = FirebaseFirestore.getInstance().collection("MoodDB");

        int j = daysInMonthArray.size();
        for (int i = 0; i < j; i++){
            if (daysInMonthArray.get(i).isEmpty()){
                moodsInMonth.add("");

            } else{
                /* Todo     query firebase for the mood
                            Handle the empty days
                          handle the ties in queries

                 */

                //Find  Date based on moodsInMonth

                Calendar whatDay = (Calendar) selectedDateCalendar.clone();
                whatDay.add(Calendar.DATE, -1*selectedDateCalendar.get(Calendar.DATE));
                String dateNumberString = daysInMonthArray.get(i);
                //date of the cell
                whatDay.add(Calendar.DATE, Integer.parseInt(dateNumberString));
                Date cellDate = whatDay.getTime();

                ArrayList<DocumentSnapshot> queryReturn = new ArrayList<>();
                Log.d("fuck",loggedInUser.getUsername());
                moods.whereEqualTo("owner.username", loggedInUser.getUsername())
                        //.whereLessThan("postedDate", new Date(cellDate.getTime()+86400000))
                        //.whereGreaterThan("postedDate", cellDate)
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                for(QueryDocumentSnapshot document : task.getResult()){
                                    queryReturn.add(document);
                                }
                            }
                        });

                List<Integer> largestEmotion = Arrays.asList(0,0,0,0,0,0,0,0);
                for (int k = 0; k< queryReturn.size()-1; k++) {
                    String t = (String) queryReturn.get(k).get("emotionalState");
                }

                int maxValue = Integer.MIN_VALUE;
                for (Integer integer: largestEmotion){
                    if (integer>maxValue)
                        maxValue=integer;
                }
                int loca = largestEmotion.indexOf(maxValue);
                String emotion = locaToEmotion(loca);

                moodsInMonth.add(emotion);

            }


        }

        Log.d("NANCY", "NMooods in moth| " +moodsInMonth);
        onQueryDataListener.onSuccess(moodsInMonth);

    }

    public String locaToEmotion(int loca){
        String emotion = null;
        switch (loca) {
            //anger
            case 1:
                emotion = EmotionalStates.ANGER.getEmoticon();
                break;

            //confusion
            case 2:
                emotion = EmotionalStates.CONFUSION.getEmoticon();
                break;

            //disgust
            case 3:
                emotion = EmotionalStates.DISGUST.getEmoticon();
                break;

            //fear
            case 4:
                emotion = EmotionalStates.FEAR.getEmoticon();
                break;

            //happiness
            case 5:
                emotion = EmotionalStates.HAPPINESS.getEmoticon();
                break;

            //sadness
            case 6:
                emotion = EmotionalStates.SADNESS.getEmoticon();
                break;

            //shame
            case 7:
                emotion = EmotionalStates.SHAME.getEmoticon();
                break;

            //surprise
            case 8:
                emotion = EmotionalStates.SURPRISE.getEmoticon();
                break;
            default:
                emotion = "";
                break;
        }

        return  emotion;
    }
}
