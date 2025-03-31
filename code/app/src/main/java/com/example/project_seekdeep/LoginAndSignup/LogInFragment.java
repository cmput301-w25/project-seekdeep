package com.example.project_seekdeep.LoginAndSignup;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.project_seekdeep.Helpers.UserProfile;
import com.example.project_seekdeep.MainActivity;
import com.example.project_seekdeep.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * LogInFragment is the starting point of application and allows user to login to their account using Firebase
 * @author Saurabh
 */
public class LogInFragment extends Fragment {
    private FirebaseFirestore db;
    private EditText usernameInput, passwordInput;
    private MaterialButton logInButton;
    private CollectionReference usersRef;
    private TextView goToSignUp;

    // Sound effects for login button and create new account button (if successful)
    // https://www.youtube.com/watch?v=iMAJRHcC2dQ
    // Taken by: Kevin Tu
    // Taken on: 2025-03-30
    private MediaPlayer soundEffect;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.log_in_fragment, container, false);

        usernameInput = view.findViewById(R.id.text_username);
        passwordInput = view.findViewById(R.id.text_password);
        logInButton = view.findViewById(R.id.log_in_button);
        goToSignUp = view.findViewById(R.id.go_to_sign_up);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");

        usersRef.addSnapshotListener((value, error) -> {
            if (error != null){
                Log.e("Firestore", error.toString());
            }
            if(value != null && !value.isEmpty()){
                for (QueryDocumentSnapshot snapshot : value) {
                    Log.d("Firestore", "User: " + snapshot.getId());
                }
            }
        });

        // Navigate to "Go to Sign Up" page
        goToSignUp.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, new SignUpFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Create sound effect player
        soundEffect = MediaPlayer.create(getContext(), R.raw.xiaohongshu);

        // Click listener for login button
        logInButton.setOnClickListener(v -> logInUser());
        return view;
    }

    /**
     * Log-ins the user by authenticating their credentials.
     * This method checks if the username exists in the Firestore database and compares the entered password with the stored password.
     * If successful, it updates the username in MainActivity and navigates to the feed.
     */
    private void logInUser() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a unique username and password in the fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get reference to the user's document based on the entered username
        DocumentReference userDoc = usersRef.document(username);
        userDoc.get()
                .addOnSuccessListener(documentSnapshot -> {
                    // If the document exists, check the password
                    if (documentSnapshot.exists()){
                        UserProfile user = documentSnapshot.toObject(UserProfile.class);

                        // If the user exists and the password matches, log in the user
                        if (user != null && user.getPassword().equals(password)) {
                            Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_SHORT).show();

                            // Update the user object in MainActivity to be used in other fragments
                            ((MainActivity) requireActivity()).setCurrentUser(user);

                            // Call the successful_login() method to show BottomNavigationView and navigate to FeedFragment
                            ((MainActivity) requireActivity()).successful_login();
                            soundEffect.start();

                        }
                        // If the password is incorrect, display an error
                        else {
                            Toast.makeText(getContext(), "Incorrect Password", Toast.LENGTH_SHORT).show();
                            passwordInput.setText("");
                        }
                    }
                    // If the username does not exist, show a error message
                    else {
                        Toast.makeText(getContext(), "Username not found!! Try Again", Toast.LENGTH_SHORT).show();
                    }
                })
                // Display failure in retrieving the data
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking username: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
