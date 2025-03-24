package com.example.project_seekdeep;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * MainActivity is the entry point for the Little Blue Notebook app which launches the initial Login page and initializes Firebase Firestore
 * It also sets up the navigation and fragment management for the app's main UI flow.
 */

public class MainActivity extends AppCompatActivity {
    private FragmentManager fragManager;
    private UserProfile currentUser;


    private ListView moodListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView navBar = findViewById(R.id.bottomNavigationView);
        navBar.setOnItemSelectedListener(navListener);

        // Initially hide the navigation bar until a successful log-in
        navBar.setVisibility(View.GONE);

        // Set default fragment to be feed upon login
        fragManager = getSupportFragmentManager();
        fragManager.beginTransaction().replace(R.id.frameLayout, new LogInFragment()).commit();
    }

    // The following code for Navigation Bar was adapted from GeeksForGeeks' guide on "BottomNavigationView in Android"
    // https://www.geeksforgeeks.org/bottomnavigationview-inandroid/
    // Taken on: 2025-03-03
    // Taken by: Kevin Tu
    /**
     * Use this listener to specify which fragment to create when a specific button on the
     * navigation bar is pressed. To do this, you can add another else if statement checking
     * if the item that was pressed on was "R.id.(Fragment_ID)". Then, create a new Fragment
     * object and store it in selectedFragment as shown for the "History" fragment.
     */
    private final NavigationBarView.OnItemSelectedListener navListener = item -> {
        Fragment selectedFragment = null;
        int itemPressed = item.getItemId();

        // Check which fragment the user clicked on
        if (itemPressed == R.id.History) {
            selectedFragment = new MoodHistoryFragment();
            //add logged in user's UserProfile to bundle to pass to mood history
            Bundle bundle = new Bundle();
            bundle.putString("username", getCurrentUsername().getUsername());
            bundle.putSerializable("userProfile", currentUser);
            selectedFragment.setArguments(bundle);

            // TODO: Replace "feed_bottom_nav" with "Feed" so it's simple and consistent with "History"
        } else if (itemPressed == R.id.feed_bottom_nav) {
            selectedFragment = new FeedFragment();

            Log.d("NANCY", "feed nav button pressed");

            //Bundle the logged-in user's UserProfile & pass to CreateMoodEventFragment()
            Bundle bundle = new Bundle();
            bundle.putSerializable("userProfile", currentUser); //make currentUser Serializable, with key "userProfile"
            selectedFragment.setArguments(bundle);  //attach bundle to the fragment
        } else if (itemPressed == R.id.create_mood_bottom_nav) {
            selectedFragment = new CreateMoodEventFragment();
            //Bundle the logged-in user's UserProfile & pass to CreateMoodEventFragment()
            Bundle bundle = new Bundle();
            bundle.putSerializable("userProfile", currentUser); //make currentUser Serializbale, with key "userProfile"
//            bundle.putString("username", getCurrentUsername()); //username stored as string in the bundle
            selectedFragment.setArguments(bundle);  //attach bundle to the fragment
        }

        // Display selected fragment to screen
        if (selectedFragment != null) {
            fragManager.beginTransaction().replace(R.id.frameLayout, selectedFragment).commit();
        }
        return true;
    };

    /**
     * Sets the current username, which will be used by the fragments.
     * @param user : he username of the currently logged-in user.
     */
    public void setCurrentUser(UserProfile user) {
        this.currentUser = user;
    }

    /**
     * Retrieves the current username which can be used by any fragment to access the logged-in user's data.
     * @return : The username of the current user.
     */
    public UserProfile getCurrentUsername() {
        return currentUser;
    }


    /**
     * This method is called after a successful login to make the navigation bar visible and indicate that the user has logged in successfully.
     */
    public void successful_login() {
        BottomNavigationView navBar = findViewById(R.id.bottomNavigationView);
        navBar.setVisibility(View.VISIBLE);

        Fragment selectedFragment = new FeedFragment();
        //Bundle the logged-in user's UserProfile & pass to CreateMoodEventFragment()
        Bundle bundle = new Bundle();
        bundle.putSerializable("userProfile", currentUser); //make currentUser Serializable, with key "userProfile"
        selectedFragment.setArguments(bundle);  //attach bundle to the fragment

        fragManager.beginTransaction().replace(R.id.frameLayout, selectedFragment).commit();

        // A placeholder for future functionality
        // This method is implemented from: https://stackoverflow.com/questions/22197452/how-to-add-fragments-to-back-stack-in-android
//      getParentFragmentManager().beginTransaction()
//          .replace(R.id.frameLayout, feedFragment)
//          .commit();
    }
}