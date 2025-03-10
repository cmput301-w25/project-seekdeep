package com.example.project_seekdeep;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private FragmentManager fragManager;

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

        // Set default fragment to be feed upon login
        fragManager = getSupportFragmentManager();
        fragManager.beginTransaction().replace(R.id.fragment_container, new TestFragment()).commit();

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
            // TODO: Replace "feed_bottom_nav" with "Feed" so it's simple and consistent with "History"
        } else if (itemPressed == R.id.feed_bottom_nav) {
            selectedFragment = new TestFragment();
        } else if (itemPressed == R.id.create_mood_bottom_nav) {
            selectedFragment = new CreateMoodScreen();
        }

        // Display selected fragment to screen
        if (selectedFragment != null) {
            fragManager.beginTransaction().replace(R.id.frameLayout, selectedFragment).commit();
        }
        return true;
    };
}