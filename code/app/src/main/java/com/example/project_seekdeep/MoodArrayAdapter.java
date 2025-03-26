package com.example.project_seekdeep;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This class is a custom array adapter for the Mood class.
 * @author Jachelle Chan and Nancy Lin
 */
public class MoodArrayAdapter extends ArrayAdapter<Mood> {

    private FirebaseStorage storage;
    private StorageReference storageRef;

    /**
     * Mandatory constructor class for MoodArrayAdapter
     *
     * @param context   , type Context
     * @param moods     , type ArrayList<Mood>
     */
    public MoodArrayAdapter(Context context, ArrayList<Mood> moods) {
        super(context, 0, moods);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    /**
     * Create and get the view for each item in a listView for moods
     *
     *
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return
     */
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

        TextView socialSit = view.findViewById(R.id.social_situation);
        TextView date = view.findViewById(R.id.date);
        ImageView image = view.findViewById(R.id.image);
        ImageView pfp = view.findViewById(R.id.profile_picture);

        // set the text for the mood event (layout_mood.xml)
        reason.setText(currentMood.getReason());
        emotion.setText(currentMood.getEmotionalState().toString());
        user.setText("@"+currentMood.getOwnerString());

        socialSit.setText("(" + currentMood.getSocialSituation().toString() + ")");
        date.setText(currentMood.getPostedDate().toString());


        // i don't know how to do the image and pfp one - jachelle



        // if no reason, hide it
        if(currentMood.getReason() == null){
            reason.setVisibility(View.GONE);
        }

        //fix bug where when you scroll past it hides all social situations
        socialSit.setVisibility(View.VISIBLE);
        //view.findViewById(R.id.social_situation_icon).setVisibility(View.VISIBLE);

        //if no social situation, hide it
        if(currentMood.getSocialSituation().toString().equals("Social Situations")){
            socialSit.setVisibility(View.GONE);
            //view.findViewById(R.id.social_situation_icon).setVisibility(View.GONE);
        }

        // if image DNE, then hide the image view?
        if (currentMood.getImage() == null){
            view.findViewById(R.id.image).setVisibility(View.GONE);
            image.setImageDrawable(null);

            Log.d("NANCY", "null image");
        } else{
            ; //ToDo for images

            view.findViewById(R.id.image).setVisibility(View.VISIBLE);
            //Log.d("NANCY", "non null image: " + splitString.toString());


            StorageReference pathReference = storageRef.child("Images/" +
                    currentMood.getImage().getLastPathSegment());

            Log.d("NANCY", "Non null Pathref/ " + pathReference);



            StorageReference imageFire = storage.getReference("Images/" +
                    currentMood.getImage().getLastPathSegment());


            Log.d("NANCY", "Non null image fire/ " + imageFire);


            //Glide.with(getContext())
            //       .load(imageFire)
            //      .into(image);


            imageFire.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // Got the download URL for 'users/me/profile.png'
                    Glide.with(getContext())
                            .load(uri)
                            .into(image);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
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
