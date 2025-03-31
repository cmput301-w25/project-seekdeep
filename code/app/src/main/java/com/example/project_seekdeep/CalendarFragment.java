package com.example.project_seekdeep;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    private UserProfile loggedInUser;
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private Date selectedDate;
    Calendar selectedDateCalendar;
    private LocalDate selectedLocalDate;

    private OnGetQueryDataListener queryDataListener;


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

        //get the date
        selectedDate = new Date();
        selectedDateCalendar = Calendar.getInstance();
        selectedDateCalendar.setTime(selectedDate);


        //get array to construct recycler
        CalendarAdapter.OnItemListener listener = this;


        //construct recycler view
        buildCalendar(this);

        //add on click listener for buttons
        Button nextButton = view.findViewById(R.id.forwards_button_calendar);
        Button backButton = view.findViewById(R.id.back_button_calendar);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDateCalendar.add(Calendar.MONTH, 1);
                buildCalendar(listener);
            }
        });


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDateCalendar.add(Calendar.MONTH, -1);
                buildCalendar(listener);
            }
        });



    }

    public void buildCalendar(CalendarAdapter.OnItemListener onItemListener){



        monthYearText.setText(getMonthFromCalendar(  selectedDateCalendar ));
        Log.d("NANCY", "calendar time |" + selectedDateCalendar.getTime());
        ArrayList<String> daysInMonth = daysInMonthArray( selectedDateCalendar);
        //ArrayList<String> moodsInMonth = createMoodsInMonthArray( daysInMonth, queryDataListener);

        new CalendarQueries().createMoodsInMonthArray(daysInMonth, selectedDateCalendar, loggedInUser, new OnGetQueryDataListener() {
            @Override
            public void onStart() {
                //nothing
            }

            @Override
            public void onSuccess(ArrayList<String> moodsInMonth) {
                CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, moodsInMonth, onItemListener);
                RecyclerView.LayoutManager layoutManager = new GridLayoutManager(requireContext(), 7);
                calendarRecyclerView.setLayoutManager(layoutManager);
                calendarRecyclerView.setAdapter(calendarAdapter);

                Log.d("NANCY", "On success");
                calendarAdapter.notifyDataSetChanged();

            }

            @Override
            public void onFailed(DatabaseError databaseError) {

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





    @Override
    public void onItemClick(int position, String dayText) {
        if(!dayText.equals("")) {
            //you can probably change this later
            String message = "Selected Date " + dayText + " " + selectedDateCalendar.get(Calendar.MONTH);
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }





}
