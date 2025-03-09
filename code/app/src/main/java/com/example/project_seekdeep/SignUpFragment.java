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

        passwordTrigger.setVisibility(view.INVISIBLE);
        passwordInput.setOnClickListener(view1 -> {
            passwordTrigger.setVisibility(view.VISIBLE);
        });

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

        goToLogIn.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LogInFragment())
                    .addToBackStack(null)
                    .commit();
        });
        signupButton.setOnClickListener(v -> signUpUser());
        return view;
    }

    /**
     * Handles the user sign-up and saves their credentials.
     */
    private void signUpUser() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a unique username and password in the fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8){
            Toast.makeText(getContext(), "Password needs to be 8 characters in length", Toast.LENGTH_SHORT).show();
            passwordInput.setText("");
            return;
        }
        DocumentReference userDoc = usersRef.document(username);
        userDoc.get()
                .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()){
                    Toast.makeText(getContext(), "Username already exists! Try a different username or Log in to existing account", Toast.LENGTH_LONG).show();
                }
                else {
                    UserProfile newUser = new UserProfile(username, password);
                    userDoc.set(newUser)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "New Account Created Successfully!", Toast.LENGTH_SHORT).show();

                                ((MainActivity) requireActivity()).setCurrentUsername(username);
                                // This method is implemented from over here: https://stackoverflow.com/questions/22197452/how-to-add-fragments-to-back-stack-in-android
                                //Go to main feed
//                                FeedFragment feedFragment = new FeedFragment();
//                                Bundle args = new Bundle();
//                                args.putString("username", username);
//                                FeedFragment.setArguments(args);

//                                getParentFragmentManager().beginTransaction()
//                                    .replace(R.id.fragment_container, feedFragment)
//                                    .commit();

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error Occurred: ", Toast.LENGTH_LONG).show();
                            });
                }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error checking username: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Firestore", "Error checking username", e);
                });
    }
}
