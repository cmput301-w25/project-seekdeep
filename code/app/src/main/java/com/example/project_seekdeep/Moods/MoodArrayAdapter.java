package com.example.project_seekdeep.Moods;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.project_seekdeep.Helpers.ImageProvider;
import com.example.project_seekdeep.Helpers.UserProfile;
import com.example.project_seekdeep.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

/**
 * This class is a custom array adapter for the Mood class.
 * @author Jachelle Chan and Nancy Lin
 */
public class MoodArrayAdapter extends ArrayAdapter<Mood> {

    private ImageProvider imageProvider;
    private OnUsernameClickListener listener;

    /**
     * This interface will be implemented by FeedFragment when the user clicks on a mood event's username
     */
    public interface OnUsernameClickListener {
        void onUsernameClick(UserProfile user);
    }


    /**
     * Mandatory constructor class for MoodArrayAdapter
     *
     * @param context   , type Context
     * @param moods     , type ArrayList<Mood>
     */
    public MoodArrayAdapter(Context context, ArrayList<Mood> moods, OnUsernameClickListener listener) {
        super(context, 0, moods);

        //Initialize an instance of movieProvide (so can add new mood to firestore)
        FirebaseStorage storage = FirebaseStorage.getInstance();
        imageProvider = ImageProvider.getInstance(storage);

        this.listener = listener;
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
            image.setVisibility(View.GONE);
            image.setImageDrawable(null);
        } else{
            ; //ToDo for images
            image.setVisibility(View.VISIBLE);
            StorageReference imageFire = imageProvider.getStorageRefFromLastPathSeg(
                    currentMood.getImage().getLastPathSegment());

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


        /**
         * This listens for when a user clicks on a mood event's username, which will trigger the listener implemented by FeedFragment.
         */
        //When the username clicks the listener, it calls the onUsernameClick method defined in FeedFragment.
        user.setOnClickListener(view1 -> {
            if (listener != null) {
                listener.onUsernameClick(currentMood.getOwner());
            }
        });
        /**
         * This listens for when a user clicks on a mood event's profile pic, which will trigger the listener implemented by FeedFragment.
         */
        pfp.setOnClickListener(view1 -> {
            if (listener != null) {
                listener.onUsernameClick(currentMood.getOwner());
            }
        });

        return view;
    }
}
