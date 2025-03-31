package com.example.project_seekdeep.Followings;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.project_seekdeep.Helpers.UserProfile;
import com.example.project_seekdeep.R;

import java.util.ArrayList;

public class UserArrayAdapter extends ArrayAdapter<UserProfile> {
    public UserArrayAdapter(Context context, ArrayList<UserProfile> users) {
        super(context, 0, users);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.search_for_user_content, parent, false);
        }
        else {
            view = convertView;
        }

        UserProfile currentUser = getItem(position);

        //Set ids
        TextView username = view.findViewById(R.id.username_users_tab);

        //populate the ui
        username.setText(currentUser.getUsername());

        Log.d("SearchForUsers","show show users now");
        return view;
    }
}
