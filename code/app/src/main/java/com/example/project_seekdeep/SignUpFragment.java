package com.example.project_seekdeep;

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

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * SignUpFragment handles creating a new account and storing the users credentials in Firebase Database.
 */
public class SignUpFragment extends Fragment {
    private FirebaseFirestore db;
    private EditText usernameInput, passwordInput;
    private MaterialButton signupButton;
    private CollectionReference usersRef;
    private TextView goToLogIn;
    private TextView passwordTrigger;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.sign_up_fragment, container, false);

        usernameInput = view.findViewById(R.id.text_username);
        passwordInput = view.findViewById(R.id.text_password);
        signupButton = view.findViewById(R.id.sign_up_button);
        goToLogIn = view.findViewById(R.id.go_to_log_in);
        passwordTrigger = view.findViewById(R.id.password_toggle);

        // Show password message when the password field is clicked
        passwordTrigger.setVisibility(view.INVISIBLE);
        passwordInput.setOnClickListener(view1 -> {
            passwordTrigger.setVisibility(view.VISIBLE);
        });

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

        // Navigate to LogInFragment when the "Go to Log In" link is clicked
        goToLogIn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frameLayout, new LogInFragment())
                    .addToBackStack(null)
                    .commit();
        });
        // Click listener for sign up button
        signupButton.setOnClickListener(v -> signUpUser());
        return view;
    }

    /**
     * Handles the user sign-up and saves their credentials.
     * If the username already exists, an error is shown. Otherwise, a new user profile is created and saved.
     */
    private void signUpUser() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        // Check if username or password fields are empty
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a unique username and password in the fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the minimum length requirement for password
        if (password.length() < 8){
            Toast.makeText(getContext(), "Password needs to be 8 characters in length", Toast.LENGTH_SHORT).show();
            passwordInput.setText("");
            return;
        }
        // Get reference to the user's document based on the entered username
        DocumentReference userDoc = usersRef.document(username);
        userDoc.get()
                .addOnSuccessListener(documentSnapshot -> {
                    // If the username is taken, display an error
                    if (documentSnapshot.exists()){
                        Toast.makeText(getContext(), "Username already exists! Try a different username or Log in to existing account", Toast.LENGTH_LONG).show();
                    }
                    // If the user doesn't exist, create a new user profile
                    else {
                        UserProfile newUser = new UserProfile(username, password);
                        userDoc.set(newUser)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "New Account Created Successfully!", Toast.LENGTH_SHORT).show();

                                // Update the username in MainActivity to be used as Primary Key
                                ((MainActivity) requireActivity()).setCurrentUser(newUser);

                                // Call the successful_login() method to show BottomNavigationView and navigate to FeedFragment
                                ((MainActivity) requireActivity()).successful_login();

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error Occurred: ", Toast.LENGTH_LONG).show();
                            });
                    }
                })
                // Display failure in retrieving the data
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking username: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
