package com.example.project_seekdeep;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
/**
 * This class is a custom array adapter for the Follow Requests class.
 * @author Deryk Fong
 */
public class FollowRequestArrayAdapter extends ArrayAdapter<FollowRequest> {
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private UserProvider userProvider;
    /**
     * Constructor for Follow Requests' Array Adapter
     * @param context
     * @param requests
     */
    public FollowRequestArrayAdapter(Context context, ArrayList<FollowRequest> requests, UserProvider userProvider) {
        super(context, 0, requests);
        this.storage = FirebaseStorage.getInstance();
        this.storageRef = this.storage.getReference();
        this.userProvider = userProvider;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view;
        FollowRequest request = this.getItem(position);
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.layout_follow_request, parent, false);
        }
        else {
            view = convertView;
        }
        TextView userName = view.findViewById(R.id.request_name);
        Button declineButton = view.findViewById(R.id.decline_button);
        Button acceptButton = view.findViewById(R.id.accept_button);

        userName.setText("@" + getItem(position).getFollower());
        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userProvider.unfollowThisUser(new UserProfile(request.getFollower(), "I really dont wanna do this") );
            }
        });
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                request.setStatusToFollowing();
            }
        });
        return view;
    }
}
