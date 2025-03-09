package com.example.project_seekdeep;

import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
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
 * LogInFragment is the starting point of application and allows user to login to their account using Firebase
 */
public class LogInFragment extends Fragment {
    private FirebaseFirestore db;
    private EditText usernameInput, passwordInput;
    private MaterialButton logInButton;
    private CollectionReference usersRef;
    private TextView goToSignUp;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.log_in_fragment, container, false);

        usernameInput = view.findViewById(R.id.text_username);
        passwordInput = view.findViewById(R.id.text_password);
        passwordInput.setTransformationMethod(new MyPasswordTransformationMethod());
        logInButton = view.findViewById(R.id.log_in_button);
        goToSignUp = view.findViewById(R.id.go_to_sign_up);

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

        goToSignUp.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SignUpFragment())
                    .addToBackStack(null)
                    .commit();
        });

        logInButton.setOnClickListener(v -> logInUser());
        return view;
    }

    /**
     * Log-ins the user by authenticating their credentials.
     */
    private void logInUser() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a unique username and password in the fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userDoc = usersRef.document(username);
        userDoc.get()
                .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()){
                    UserProfile user = documentSnapshot.toObject(UserProfile.class);

                    if (user != null && user.getPassword().equals(password)) {
                        Toast.makeText(getContext(), "Login Successful!", Toast.LENGTH_SHORT).show();

                        ((MainActivity) requireActivity()).setCurrentUsername(username);
                        //Go to home screen
                        FeedFragment feedFragment = new FeedFragment();
                        Bundle args = new Bundle();
                        args.putString("username", username);
                        FeedFragment.setArguments(args);

                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, feedFragment)
                                .commit();
                    }
                    else {
                        Toast.makeText(getContext(), "Incorrect Password", Toast.LENGTH_SHORT).show();
                        passwordInput.setText("");
                    }
                }
                else {
                    Toast.makeText(getContext(), "Username not found!! Try Again", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error checking username: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Firestore", "Error checking username", e);
            });
    }

    // https://stackoverflow.com/questions/14137184/how-do-i-use-the-transformationmethod-interface/23110194#23110194
    public class MyPasswordTransformationMethod extends PasswordTransformationMethod {

        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {

            private CharSequence mSource;

            public PasswordCharSequence(CharSequence source) {
                mSource = source;
            }

            public char charAt(int index) {
                return '*';
            }

            public int length() {
                return mSource.length();
            }

            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
    };
}
