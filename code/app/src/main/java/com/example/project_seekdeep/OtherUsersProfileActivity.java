package com.example.project_seekdeep;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * This activity displays another user's profile. Including their username, mood history, number of followers & following, and a button to follow them.
 *
 * Following feature:
 * If the logged-in user doesn't follow the user's profile, there will be a "Follow" button.
 * Once clicked, the button will change to a "Pending" button.
 * Once the user has accepted the follow request, then it will change to a "Following" button.
 */
public class OtherUsersProfileActivity extends AppCompatActivity {
    private TextView usernameTextView;
    private FirebaseFirestore db; //Need access to the database to retrieve all the user's moods
    private ImageButton backButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_users_profile_page);

        //Setup the username
        String username = getIntent().getStringExtra("USERNAME");
        usernameTextView = findViewById(R.id.username_profile);
        usernameTextView.setText(username);

        //Get instance of db
        db = FirebaseFirestore.getInstance();

        //Implement the back button
        //https://stackoverflow.com/questions/72634225/onbackpressed-is-deprecated-what-is-the-alternative
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

}
