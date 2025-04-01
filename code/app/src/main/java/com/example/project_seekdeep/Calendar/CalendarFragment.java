package com.example.project_seekdeep.Calendar;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.project_seekdeep.Helpers.EmotionalStates;
import com.example.project_seekdeep.Helpers.UserProfile;
import com.example.project_seekdeep.Moods.Mood;


import com.example.project_seekdeep.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Calendar Fragment
 * This fragment is designed to display all creating a Month-view calendar
 * each day displays the most impactful/ frequent mood used on that day
 * Under the calendar is a tip / insight like "Your most common mood this week was happy, keep it up"
 *
 * Borrows Code from every other fragment in the project
 * @author Nancy Lin
 *
 */
public class CalendarFragment extends Fragment implements CalendarAdapter.OnItemListener{

    private boolean isQueryingFinished = false;
    private UserProfile loggedInUser;
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private Date selectedDate;
    Calendar selectedDateCalendar;
    private LocalDate selectedLocalDate;

    private TextView sticky;
    CalendarAdapter calendarAdapter;
    RecyclerView.LayoutManager layoutManager;

    ArrayList<String> moodsInMonth;


    public Boolean smallThreadFinished = false;

    /**
     * Constructor for the Fragment that makes the view from the xml layout
     */
    public CalendarFragment(){
        //Required Constructor
    }


    /**
     * Modify onCreate
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     *
     * Gets the logged in User from the savedInstanceState bundle
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            loggedInUser = (UserProfile) getArguments().getSerializable("userProfile");
        }


    }

    /**
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return inflated view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    /**
     * Set up the view
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        //calendar follows https://www.youtube.com/watch?v=Ba0Q-cK1fJo
        //init the views
        calendarRecyclerView = view.findViewById(R.id.calendar_recycler_view);
        monthYearText = view.findViewById(R.id.monthYearTV);
        sticky = view.findViewById(R.id.sticky);

        //get the date
        selectedDate = new Date();
        selectedDateCalendar = Calendar.getInstance();
        selectedDateCalendar.setTime(selectedDate);


        //get array to construct recycler
        CalendarAdapter.OnItemListener listener = this;


        //construct recycler view
        try {
            buildCalendar(this);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //add on click listener for buttons
        Button nextButton = view.findViewById(R.id.forwards_button_calendar);
        Button backButton = view.findViewById(R.id.back_button_calendar);

        nextButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
        backButton.setBackgroundColor(Color.parseColor("#FFFFFF"));

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDateCalendar.add(Calendar.MONTH, 1);
                try {
                    buildCalendar(listener);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDateCalendar.add(Calendar.MONTH, -1);
                try {
                    buildCalendar(listener);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });



    }

    public void buildCalendar(CalendarAdapter.OnItemListener onItemListener) throws InterruptedException {

        CollectionReference moods = FirebaseFirestore.getInstance().collection("MoodDB");

        monthYearText.setText(getMonthFromCalendar(  selectedDateCalendar ));
        Log.d("NANCY", "calendar time |" + selectedDateCalendar.getTime());
        ArrayList<String> daysInMonth = daysInMonthArray( selectedDateCalendar);

        moodsInMonth = new ArrayList<String>();

        Map<Integer, ArrayList<Mood>> calendarMoodHash = new HashMap<>();
        ArrayList<Mood> queryMoods = new ArrayList<Mood>();
        moods.whereEqualTo("owner.username", loggedInUser.getUsername())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            EmotionalStates emotionalState = EmotionalStates.valueOf((String) document.get("emotionalState"));
                            Date postedDate = Objects.requireNonNull(document.getTimestamp("postedDate")).toDate();

                            Calendar postedCalendar = Calendar.getInstance();
                            postedCalendar.setTime(postedDate);
                            int dayOfYear = postedCalendar.get(Calendar.DAY_OF_YEAR);

                            Mood mood = new Mood(loggedInUser, emotionalState);
                            mood.setPostedDate(postedDate);
                            queryMoods.add(mood);

                            if(calendarMoodHash.get(dayOfYear) == null){
                                ArrayList<Mood> moods1 = new ArrayList<Mood>();
                                moods1.add(mood);
                                calendarMoodHash.put(dayOfYear, moods1);
                            } else{
                                ArrayList<Mood> moods2 = calendarMoodHash.get(dayOfYear);
                                moods2.add(mood);
                                calendarMoodHash.put(dayOfYear, moods2);
                            }

                        }

                        Log.d("NANCY", calendarMoodHash.toString());

                        int j = daysInMonth.size();
                        for (int i = 0; i < j; i++) {


                            if (daysInMonth.get(i).isEmpty()) {
                                moodsInMonth.add("");

                            } else {

                                Calendar whatDay = (Calendar) selectedDateCalendar.clone();
                                whatDay.add(Calendar.DATE, -1 * selectedDateCalendar.get(Calendar.DATE));
                                String dateNumberString = daysInMonth.get(i);
                                //date of the cell
                                whatDay.add(Calendar.DATE, Integer.parseInt(dateNumberString));

                                int dateOfYearCell = whatDay.get(Calendar.DAY_OF_YEAR);

                                ArrayList<Mood> moods3 = calendarMoodHash.get(dateOfYearCell);
                                if(moods3 != null) {

                                    List<Integer> largestEmotion = Arrays.asList(0,0,0,0,0,0,0,0);
                                    for (int k = 0; k< moods3.size()-1; k++){
                                        EmotionalStates t = moods3.get(k).getEmotionalState();
                                        switch (t){
                                            case ANGER:
                                                largestEmotion.set(0, largestEmotion.get(0)+1);
                                                break;
                                            case CONFUSION:
                                                largestEmotion.set(1, largestEmotion.get(1)+1);
                                                break;
                                            case DISGUST:
                                                largestEmotion.set(2, largestEmotion.get(2)+1);
                                                break;
                                            case FEAR:
                                                largestEmotion.set(3, largestEmotion.get(3)+1);
                                                break;
                                            case HAPPINESS:
                                                largestEmotion.set(4, largestEmotion.get(4)+1);
                                                break;
                                            case SADNESS:
                                                largestEmotion.set(5, largestEmotion.get(5)+1);
                                                break;
                                            case SHAME:
                                                largestEmotion.set(6, largestEmotion.get(6)+1);
                                                break;
                                            case SURPRISE:
                                                largestEmotion.set(7, largestEmotion.get(7)+1);
                                                break;
                                        }
                                    }

                                    int maxValue = Integer.MIN_VALUE;
                                    for (Integer integer: largestEmotion){
                                        if (integer>maxValue)
                                            maxValue=integer;
                                    }
                                    int loca = 0;
                                    loca = largestEmotion.indexOf(maxValue);
                                    String emotion = locaToEmotion(loca);
                                    moodsInMonth.add(emotion);
                                } else{
                                    moodsInMonth.add("");
                                }


                            }

                        }

                        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, moodsInMonth, onItemListener);
                        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireContext(), 7);
                        calendarRecyclerView.setLayoutManager(layoutManager);
                        calendarRecyclerView.setAdapter(calendarAdapter);


                        //set tip based on today
                        Calendar todayCalendar = Calendar.getInstance();
                        todayCalendar.setTime(new Date());
                        int dayFromYearToday = todayCalendar.get(Calendar.DAY_OF_YEAR);
                        ArrayList<Mood> moods5 = calendarMoodHash.get(dayFromYearToday);
                        if(moods5 != null) {
                            int maxValue = Integer.MIN_VALUE;
                            for (Integer integer: largestEmotion(moods5)){
                                if (integer>maxValue)
                                    maxValue=integer;
                            }
                            int loca = 0;
                            loca = largestEmotion(moods5).indexOf(maxValue);
                            String emo = locaToEmotion(loca);

                            sticky.setText(tip(emo));
                        }
                    }
                });


    }


    public String getMonthFromCalendar(Calendar calendar){
        String month = null;
        switch (calendar.get(Calendar.MONTH)){
            case 0:
                month = "January";
                break;
            case 1:
                month = "February";
                break;

            case 2:
                month = "March";
                break;
            case 3:
                month = "April";
                break;

            case 4:
                month = "May";
                break;
            case 5:
                month = "June";
                break;
            case 6:
                month = "July";
                break;
            case 7:
                month = "August";
                break;

            case 8:
                month = "September";
                break;
            case 9:
                month = "October";
                break;
            case 10:
                month = "November";
                break;

            case 11:
                month = "December";
                break;

        }

        return month;
    }

    private ArrayList<String> daysInMonthArray(Calendar calendar) {
        ArrayList<String> daysInMonthArray = new ArrayList<>();

        Calendar calendar2 = (Calendar) calendar.clone();
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        calendar2.add(Calendar.DATE, -1*( calendar2.get( Calendar.DAY_OF_MONTH)-1));

        Log.d("NANCY", "daysinmonth array calendar 2" + calendar2.getTime() + "|" + calendar2.get(Calendar.DAY_OF_WEEK));

        int dayOfWeek = calendar2.get(Calendar.DAY_OF_WEEK) -1;

        for(int i = 1; i <= 42; i++) {
            if(i <= dayOfWeek || i > daysInMonth + dayOfWeek)
            {
                //if the cell # is over 36 and there's no days, don't add anything
                if (calendar2.get(Calendar.MONTH) == Calendar.FEBRUARY && i > 28){
                    continue;
                } else if (i < 36) {
                    daysInMonthArray.add("");
                }
            }
            else
            {
                daysInMonthArray.add(String.valueOf(i - dayOfWeek));
            }
        }

        //if the first row is blank, delete it
        if (daysInMonthArray.get(6).equals("")){
            daysInMonthArray.subList(0, 7).clear();
        }

        return  daysInMonthArray;
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


    public List<Integer> largestEmotion(ArrayList<Mood> moods3){
        List<Integer> largestEmotion = Arrays.asList(0,0,0,0,0,0,0,0);
        for (int k = 0; k< moods3.size()-1; k++){
            EmotionalStates t = moods3.get(k).getEmotionalState();
            switch (t){
                case ANGER:
                    largestEmotion.set(0, largestEmotion.get(0)+1);
                    break;
                case CONFUSION:
                    largestEmotion.set(1, largestEmotion.get(1)+1);
                    break;
                case DISGUST:
                    largestEmotion.set(2, largestEmotion.get(2)+1);
                    break;
                case FEAR:
                    largestEmotion.set(3, largestEmotion.get(3)+1);
                    break;
                case HAPPINESS:
                    largestEmotion.set(4, largestEmotion.get(4)+1);
                    break;
                case SADNESS:
                    largestEmotion.set(5, largestEmotion.get(5)+1);
                    break;
                case SHAME:
                    largestEmotion.set(6, largestEmotion.get(6)+1);
                    break;
                case SURPRISE:
                    largestEmotion.set(7, largestEmotion.get(7)+1);
                    break;
            }
        }

        return largestEmotion;
    }
    public String tip (String emoticon){
        String color = null;
        switch (emoticon){
            //anger
            case "\uD83D\uDE20":
                color = "You seem very angry lately...";

                break;

            //confusion
            case "\uD83E\uDD14":
                color = "Everything's always been confusing, no?";
                break;

            //disgust
            case "\uD83E\uDD22":
                color = "Everything's giving you digust? Might need to go to other places...";
                break;

            //fear
            case "\uD83D\uDE28":
                color = "Are you paranoid, or do you really need to watch out for serial killers?";
                break;

            //happiness
            case "\uD83D\uDE04":
                color = "All's happy go lucky here!";
                break;

            //sadness
            case "☹️":
                color = "Sadness isn't the end. It'll get better soon.";
                break;

            //shame
            case "\uD83D\uDE14":
                color = "Shame? You gotta stop embarrassing yourself bbg.";
                break;

            //surprise
            case "\uD83D\uDE2F":
                color = "Surprises are everywhere. Time to get used to them.";
                break;

            default:
                color = "Nothing much to say.";


        }
        return color;
    }


    @Override
    public void onItemClick(CharSequence mood) {
        mood = mood.toString();
        if(!(mood == null)) {
            sticky.setText(tip(mood.toString()));
        } else{
            sticky.setText("");
        }
    }





}
