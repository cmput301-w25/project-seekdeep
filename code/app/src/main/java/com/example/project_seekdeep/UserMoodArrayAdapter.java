package com.example.project_seekdeep;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This class is a custom array adapter for the Mood class.
 * Copy of MoodArrayAdapter but modified for User's moods (add edit /delete functionality)
 * @author Jachelle Chan and Nancy Lin
 */
public class UserMoodArrayAdapter extends ArrayAdapter<Mood> {

    //private Context context;

    /**
     * Constructor for UserMoodArrayAdapter
     * @param context
     * @param moods
     *      arraylist of Mood s to display
     */
    public UserMoodArrayAdapter(Context context, ArrayList<Mood> moods) {
        super(context, 0, moods);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_feed_mood_edit_delete_combo, parent, false);
        }
        else {
            view = convertView;
        }

        Mood currentMood = getItem(position);

        // set up ids
        TextView reason = view.findViewById(R.id.reason);
        TextView emotion = view.findViewById(R.id.emotion);
        TextView user = view.findViewById(R.id.username);
        TextView trigger = view.findViewById(R.id.trigger);
        TextView socialSit = view.findViewById(R.id.social_situation);
        TextView date = view.findViewById(R.id.date);
        ImageView image = view.findViewById(R.id.image);
        ImageView pfp = view.findViewById(R.id.profile_picture);

        // set the text for the mood event (layout_mood.xml)
        reason.setText(currentMood.getReason());
        emotion.setText(currentMood.getEmotionalState().toString());
        user.setText(currentMood.getOwnerString());
        trigger.setText(currentMood.getTrigger());
        socialSit.setText(currentMood.getSocialSituation().toString());
        date.setText(currentMood.getPostedDate().toString());


        // i don't know how to do the image and pfp one - jachelle

        //if theres no trigger, hide it
        if (currentMood.getTrigger() == null || Objects.equals(currentMood.getTrigger(), "")){
            trigger.setVisibility(View.GONE);
            view.findViewById(R.id.trigger_icon).setVisibility(View.GONE);
        }

        // if no reason, hide it
        if(currentMood.getReason() == null){
            reason.setVisibility(View.GONE);
        }
        //if no social situation, hide it
        if(currentMood.getSocialSituation().toString().equals("Social Situations")){
            socialSit.setVisibility(View.GONE);
            view.findViewById(R.id.social_situation_icon).setVisibility(View.GONE);
        }

        // if image DNE, then hide the image view?
        if (currentMood.getImage() == null){
            image.setImageDrawable(null);
        } else{
            ; //ToDo for images
        }



        // color
        // https://stackoverflow.com/questions/4772537/i-need-to-change-the-stroke-color-to-a-user-defined-color-nothing-to-do-with-th

        //default
        //view.setBackgroundColor(Color.parseColor(currentMood.getEmotionalState().getColour()));

        //just the rectangle background
        //Drawable box_outline = DrawableCompat.wrap(view.findViewById(R.id.mood_details_box).getBackground()).mutate();
        //DrawableCompat.setTint(box_outline, Color.parseColor(currentMood.getEmotionalState().getColour()));

        //the outline change color
        GradientDrawable box_outline = (GradientDrawable) view.findViewById(R.id.mood_details_box).getBackground();
        box_outline.mutate();
        box_outline.setStroke(5, Color.parseColor(currentMood.getEmotionalState().getColour()));


        Button editMoodButton = view.findViewById(R.id.edit_mood_button);
        editMoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("NANCY", "Click button");
                EditMoodFragment editMoodFragment = EditMoodFragment.newInstance(currentMood);

                FragmentActivity activity =  (FragmentActivity) getContext();
                FragmentManager fm = activity.getSupportFragmentManager();

                editMoodFragment.show(fm, "Mood Details");

            }
        });


        return view;
    }
}
