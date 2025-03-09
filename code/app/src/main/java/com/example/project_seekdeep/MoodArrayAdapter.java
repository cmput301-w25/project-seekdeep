package com.example.project_seekdeep;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * This class is a custom array adapter for the Mood class.
 * @author Jachelle Chan and Nancy Lin
 */
public class MoodArrayAdapter extends ArrayAdapter<Mood> {

    public MoodArrayAdapter(Context context, ArrayList<Mood> moods) {
        super(context, 0, moods);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_feed_mood, parent, false);
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
        socialSit.setText(currentMood.getSocialSituation());
        date.setText(currentMood.getPostedDate().toString());


        // i don't know how to do the image and pfp one - jachelle

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


        return view;
    }
}
