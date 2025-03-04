package com.example.project_seekdeep;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class UserArrayAdapter extends ArrayAdapter<UserProfile> {
    private ArrayList<UserProfile> users;
    private Context context;

    public UserArrayAdapter(Context context, ArrayList<UserProfile> users){
        super(context, 0, users);
        this.users = users;
        this.context =context;
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.sign_up_fragment, parent, false);
        }
        UserProfile user = users.get(position);
        TextView userName = view.findViewById(R.id.text_username);
        TextView userPass = view.findViewById(R.id.text_password);

        userName.setText(user.getUsername());
        userPass.setText(user.getPassword());
        return view;
    }
}
