package com.example.project_seekdeep;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import android.Manifest;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * MAP fragments uses google maps API to show the user's current location and mood based emoji markers.
 * It retrieves locations from the firebase database and displays them on the map with specified mood details.
 * @author Saurabh
 */
// Resources used:
// https://stackoverflow.com/questions/62111235/how-do-you-use-a-google-mapview-inside-of-a-fragment
// https://stackoverflow.com/questions/74367916/how-can-i-create-location-request-in-android-locationrequest-create-is-depr
// https://youtube.com/playlist?list=PLHQRWugvckFrWppucVnQ6XhiJyDbaCU79&si=LXVl0HjJwen_ij05
// https://developers.google.com/maps/documentation/android-sdk/reference/com/google/android/libraries/maps/model/BitmapDescriptorFactory#HUE_YELLOW
// https://stackoverflow.com/questions/17839388/creating-a-scaled-bitmap-with-createscaledbitmap-in-android
// https://stackoverflow.com/questions/47807621/draw-emoji-on-bitmap-with-drawtextonpath?utm_source=chatgpt.com

public class MapsFragment extends Fragment implements OnMapReadyCallback, FilterMenuDialogFragment.OnFilterSelectedListener {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;       // API to request location permission

    // Image buttons that are selectable
    private ImageButton filterMoodHistoryButton;
    private ImageButton filterMoodFollowingButton;
    private ImageButton filter5KmRadiusButton;

    // Toggle button map display
    private ToggleButton displayToggle;

    private FirebaseFirestore db;

    /**
     * Empty constructor required by database
     */
    public MapsFragment() {
    }

    /**
     * Inflates the layout and initializes map, buttons and location
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     *    The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return
     */
    @SuppressLint("MissingPermission")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        // Initialize instance of database
        db = FirebaseFirestore.getInstance();

        // Initialize the map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);

        // Prepare the map
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }
        // Used fused location client to get location from google services
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        // Location request for high accuracy every 5 secs
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        // Initialize the permission launcher
        requestLocationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Toast.makeText(requireContext(), "Location permission granted", Toast.LENGTH_SHORT).show();
                        // If map is ready show, enable the blue dot and fetch location
                        if (mMap != null) {
                            enableLocationAndFetch();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // The button click listeners
        filterMoodHistoryButton = view.findViewById(R.id.filter_mood_history);
        filterMoodFollowingButton = view.findViewById(R.id.filter_mood_following);
        filter5KmRadiusButton = view.findViewById(R.id.filter_5_km_radius);
        displayToggle = view.findViewById(R.id.display_toggle);

        // Only use mood filter when showing mood history markers
        filterMoodHistoryButton.setOnClickListener(v -> {
            if (!displayToggle.isChecked()) {
                new FilterMenuDialogFragment().show(getChildFragmentManager(), "profile");
            } else {
                Toast.makeText(requireContext(), "History filter unavailable in Mood Following mode", Toast.LENGTH_SHORT).show();
            }
        });

        // Only use following filter when showing mood following markers
        filterMoodFollowingButton.setOnClickListener(v -> {
            if (displayToggle.isChecked()) {
                new FilterMenuDialogFragment().show(getChildFragmentManager(), "following");
            } else {
                Toast.makeText(requireContext(), "Following filter unavailable in Mood History mode", Toast.LENGTH_SHORT).show();
            }

        });

        filter5KmRadiusButton.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "View is in development", Toast.LENGTH_SHORT).show();
        });

        displayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mMap.clear();
            // Display the respective view and enable the filters
            if (displayToggle.isChecked()) {
                loadFollowingLocations();
                filterMoodFollowingButton.setEnabled(displayToggle.isChecked());
            } else {
                loadMoodHistoryLocations();
                filterMoodHistoryButton.setEnabled(!displayToggle.isChecked());
            }
            filterMoodHistoryButton.setSelected(false);
            filterMoodFollowingButton.setSelected(false);
        });

        return view;
    }

    /**
     * Call when map is ready to be used and creates and instance of google map to be used.
     * Sets up the map with mood markers and the user's location.
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Attempt to enable location
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableLocationAndFetch();
        } else {
            // If permission isn’t granted, request it
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        // Load the following displays as toggle switches
        mMap.clear();
        if (displayToggle.isChecked()) {
            loadFollowingLocations();
        } else {
            loadMoodHistoryLocations();
        }
    }

    /**
     * Enables the blue dot and fetches user location and move the camera to that location
     */
    private void enableLocationAndFetch() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        // Request last known location from client
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Create the latlng object and move camera to this retrieved location
                        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                    } else {
                        requestLocationUpdates();
                    }
                }).addOnFailureListener(e -> {
                    Log.e("MapsFragment", "Error fetching last location", e);
                    requestLocationUpdates();
                });
    }

    /**
     * Requests regular location updates when the last location isn’t available or the user moves
     * Moves the map camera to the first valid location received and stops updates.
     */
    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    LatLng myLocation = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                    fusedLocationProviderClient.removeLocationUpdates(this);
                }
            }
        }, Looper.getMainLooper()).addOnFailureListener(e -> {
            Log.e("MapsFragment", "Failed to request location updates", e);
            Toast.makeText(requireContext(), "Unable to get location updates", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Loads the mood based location markers of user that are emojis of the specified mood.
     */
    private void loadMoodHistoryLocations(){
        // Current user's profile
        UserProfile currentUserProfile = ((MainActivity) requireActivity()).getCurrentUsername();
        String userName = currentUserProfile.getUsername();
        loadMoodLocations(userName);
    }

    /**
     * The method that actually query the database and set the marker that is to be
     * displayed on the map.
     * @param user
     */
    private void loadMoodLocations(String user) {
        // Current user's profile
        String userName = user;
        // Match the database locations with current user
        db.collection("locations")
                .whereEqualTo("userId", userName)
                .get()
                .addOnSuccessListener(queryLocationSnapshots -> {
                    // Iterate throught each item in the database collection 'locations'
                    for (QueryDocumentSnapshot document : queryLocationSnapshots) {
                        UserLocation location = document.toObject(UserLocation.class);
                        EmotionalStates emotionalState = location.getEmotionalState();
                        Double latitude = location.getLatitude();
                        Double longitude = location.getLongitude();
                        if (latitude != null && longitude != null && emotionalState != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            String markerTitle = emotionalState.getStateName();
                            mMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title(markerTitle)
                                    .icon(getEmotionalLocation(emotionalState)));
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load your mood locations", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Loads the followings' mood based location markers that are emojis of the specified mood.
     */
    private void loadFollowingLocations(){
        // Current user's profile
        UserProfile currentUserProfile = ((MainActivity) requireActivity()).getCurrentUsername();
        String userName = currentUserProfile.getUsername();

        db.collection("followings_and_requests")
                .whereEqualTo("follower", userName)
                .whereEqualTo("status", "following")
                .get()
                .addOnSuccessListener(queryFollowingSnapshots -> {
                    for (QueryDocumentSnapshot document : queryFollowingSnapshots) {
                        FollowRequest follow = document.toObject(FollowRequest.class);
                        String followee = follow.getFollowee();
                        loadMoodLocations(followee);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load your following mood locations", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Return the bitmap descriptor of custom marker based on mood
     * @param emotionalState: The emotional state enum
     * @return
     */
    // BitmapDescriptor is class of google maps API, vs Bitmap is class of android graphics
    private BitmapDescriptor getEmotionalLocation(EmotionalStates emotionalState) {
        String emoji = emotionalState.getEmoticon();
        // Create a 100 x 100 bitmap and a canvas to display upon
        Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // Set up the paint for the emoji
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(64);
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Draw the emoji at the center of the canvas
        float x = canvas.getWidth() / 2f;
        float y = (canvas.getHeight() / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);        // Align the emoji vertically
        canvas.drawText(emoji, x, y, textPaint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);         // Convert bitmap to bitmapDescriptor
    }

    /**
     * This method apply filters on the map markers based on Mood History filters and displays the filtered markers.
     *
     * @param selectedMoods:    The filter based on state of the mood
     * @param selectedTimeline: The filter based on timeline
     * @param keyword:          The filter based on a keyword in the mood event
     */
    // Taken from MoodHistoryFragment.java
    @Override
    public void onFiltersApplied(ArrayList<EmotionalStates> selectedMoods, String selectedTimeline, String keyword) {
        // Current user's profile
        UserProfile currentUserProfile = ((MainActivity) requireActivity()).getCurrentUsername();
        String userName = currentUserProfile.getUsername();

        // Filter the Mood History
        if (!displayToggle.isChecked()) {
            // Match the database locations with current user
            db.collection("MoodDB")
                    .whereEqualTo("owner.username", userName)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        ArrayList<Mood> moodList = new ArrayList<>();
                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                            moodList.add(createMoodFromSnapshot(snapshot, currentUserProfile));
                        }

                        applyFilters(moodList, selectedMoods, selectedTimeline, keyword);
                        // Create Arraylist for filtered moods
                        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();

                        List<String> moodIds = filteredMoods.stream()
                                .map(mood -> mood.getDocRef().getId())
                                .collect(Collectors.toList());
                        mapFilteredMoods(moodIds, userName, filterMoodHistoryButton, true);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to load moods", Toast.LENGTH_SHORT).show();
                    });
        }
        // Filter the Mood Following
        else {
            db.collection("followings_and_requests")
                    .whereEqualTo("follower", userName)
                    .whereEqualTo("status", "following")
                    .get()
                    .addOnSuccessListener(queryFollowingSnapshots -> {
                        if (queryFollowingSnapshots.isEmpty()) {
                            mMap.clear();
                            Toast.makeText(requireContext(), "You are not following anyone", Toast.LENGTH_SHORT).show();
                            filterMoodFollowingButton.setSelected(false);
                            return;
                        }
                        List<String> allFilteredMoodIds = new ArrayList<>();

                        for (QueryDocumentSnapshot document : queryFollowingSnapshots) {
                            FollowRequest follow = document.toObject(FollowRequest.class);
                            String followee = follow.getFollowee();
                            if (followee != null) {
                                db.collection("MoodDB")
                                        .whereEqualTo("owner.username", followee)
                                        .get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            ArrayList<Mood> moodList = new ArrayList<>();
                                            for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                                UserProfile user = new UserProfile(followee);
                                                moodList.add(createMoodFromSnapshot(snapshot, user));
                                            }

                                            applyFilters(moodList, selectedMoods, selectedTimeline, keyword);
                                            // Create Arraylist for filtered moods
                                            ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
                                            for (Mood mood : filteredMoods) {
                                                allFilteredMoodIds.add(mood.getDocRef().getId());
                                            }
                                            mapFilteredMoods(allFilteredMoodIds,null, filterMoodFollowingButton, false );
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(requireContext(), "Failed to load moods", Toast.LENGTH_SHORT).show();

                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to load moods following", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Apllies the filters required on the mood display based on the History and Following fragments
     * @param moodList
     * @param selectedMoods
     * @param selectedTimeline
     * @param keyword
     */
    private void applyFilters(ArrayList<Mood> moodList, ArrayList<EmotionalStates> selectedMoods, String selectedTimeline, String keyword) {
        // Apply the selected filters if they aren't empty
        MoodFiltering.removeAllFilters();
        MoodFiltering.saveOriginal(moodList);
        if (!selectedMoods.isEmpty()) {
            MoodFiltering.addStates(selectedMoods);
            MoodFiltering.applyFilter("states");
        }
        if (!selectedTimeline.isBlank()) {
            MoodFiltering.applyFilter(selectedTimeline);
        }
        if (!keyword.isEmpty()) {
            MoodFiltering.addKeyword(keyword);
            MoodFiltering.applyFilter("keyword");
        }
    }

    private void mapFilteredMoods(List<String> moodIds, String userId, ImageButton filterButton, boolean filterByUserId){
        if (moodIds.isEmpty()) {
            mMap.clear();
            filterButton.setSelected(false);
            return;
        }
        // Since firebase has a limit of 30 for whereIn argument, we are gonna turn it into chunks.
        // https://stackoverflow.com/questions/61354866/is-there-a-workaround-for-the-firebase-query-in-limit-to-10
        List<UserLocation> locationCollection = new ArrayList<>();
        int[] completedQueries = {0};
        int totalQueries = (int) Math.ceil(moodIds.size() / 30.0);

        for (int i = 0; i < moodIds.size(); i += 30) {
            int j = Math.min(i + 30, moodIds.size());
            List<String> chunk = moodIds.subList(i,j);

            Query query = db.collection("locations")
                    .whereIn("moodID", chunk);
            // Filter by userId for personal mood history
            if (filterByUserId) {
                query = query.whereEqualTo("userId", userId);
            }
            query.get()
                    .addOnSuccessListener(locationSnapshots -> {
                        for (QueryDocumentSnapshot document : locationSnapshots) {
                            UserLocation location = document.toObject(UserLocation.class);
                            locationCollection.add(location);
                        }
                        completedQueries[0]++;
                        if (completedQueries[0] == totalQueries) {
                            displayLocationsOnMap(locationCollection, filterButton);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to load filtered locations", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void displayLocationsOnMap(List<UserLocation> locationCollection, ImageButton filterButton){
        mMap.clear();
        for (UserLocation location : locationCollection) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            EmotionalStates emotionalState = location.getEmotionalState();
            String markerTitle = emotionalState.getStateName();
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(markerTitle)
                    .icon(getEmotionalLocation(emotionalState)));
        }
        filterButton.setSelected(!locationCollection.isEmpty());

    }
    /**
     * This method resets the filters and displays the map with all mood events.
     */
    @Override
    public void onFiltersReset() {
        if (displayToggle.isChecked()) {
            filterMoodFollowingButton.setSelected(false);
            loadFollowingLocations();
        } else {
            filterMoodHistoryButton.setSelected(false);
            loadMoodHistoryLocations();
        }

    }

    /**
     * Creates a Mood object from a Firestore document snapshot
     * @param snapshot
     * @param currentUserProfile
     * @return
     */
    // Taken from MoodHistoryFragment.java
    private Mood createMoodFromSnapshot(QueryDocumentSnapshot snapshot, UserProfile currentUserProfile){
        EmotionalStates emotionalState = EmotionalStates.valueOf((String) snapshot.get("emotionalState"));
        SocialSituations socialSituation = SocialSituations.valueOf((String) snapshot.get("socialSituation"));
        List<String> followers = (List<String>) snapshot.get("followers");
        Date postedDate = Objects.requireNonNull(snapshot.getTimestamp("postedDate")).toDate();
        String reason = (String) snapshot.get("reason");

        String imageStr = (String) snapshot.get("image");
        Uri image = null;
        if (imageStr != null) {
            image = Uri.parse(imageStr);
        }
        Mood mood = new Mood(currentUserProfile, emotionalState, socialSituation, followers, postedDate, reason);
        mood.setDocRef(snapshot.getReference());
        mood.setImage(image);

        return mood;
    }
}