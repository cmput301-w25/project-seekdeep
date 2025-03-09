package com.example.project_seekdeep;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * https://stackoverflow.com/questions/30092158/set-button-onclick-in-listview-android
 */
public class MoodEventBaseAdapter extends BaseAdapter {

    ArrayList<Mood> moodArrayList;
    Context con;

    public MoodEventBaseAdapter(ArrayList<Mood> moodArrayList, Context con){
        this.moodArrayList = moodArrayList;
        this.con = con;

    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view;
        if (convertView == null) {
            LayoutInflater lif = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = lif.inflate(R.layout.layout_feed_mood_edit_delete_combo, null);
        }
        else {
            view = convertView;
        }


        Mood currentMood = (Mood) getItem(position);

        Log.d("NANCY", currentMood.getSocialSituation());

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












        Button editMoodButton = convertView.findViewById(R.id.edit_mood_button);

        editMoodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("NANCY", "Click button");
            }
        });


        return view;
    }
}
