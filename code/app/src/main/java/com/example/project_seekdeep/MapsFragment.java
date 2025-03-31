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
import android.location.Location;
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
import android.widget.TextView;
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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
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
 * MAP fragments uses google maps API to show the user's current location and mood based emoji markers
 * for the mood history of user and also the moods for the people user is following.
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
// https://stackoverflow.com/questions/31315873/android-how-to-draw-a-circle-on-google-map
// https://developers.google.com/maps/documentation/android-sdk/infowindows?_gl=1*1chw39e*_up*MQ..*_ga*MTA5NzM3NzMzMy4xNzQzMTE4Mjk3*_ga_NRWSTWS78N*MTc0MzExODI5Ny4xLjEuMTc0MzExODMwMS4wLjAuMA..#maps_android_info_windows_add-java

public class MapsFragment extends Fragment implements OnMapReadyCallback, FilterMenuDialogFragment.OnFilterSelectedListener, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;       // API to request location permission
    private LocationCallback locationCallback;
    // Image buttons that are selectable
    private ImageButton filterMoodHistoryButton;
    private ImageButton filterMoodFollowingButton;
    private ImageButton filter5KmRadiusButton;

    // Toggle button map display
    private ToggleButton displayToggle;

    private FirebaseFirestore db;
    private Location currentLocation;
    private Circle radiusCircle;
    private View infoWindowView;

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
            }
        });

        // Only use following filter when showing mood following markers
        filterMoodFollowingButton.setOnClickListener(v -> {
            if (displayToggle.isChecked()) {
                new FilterMenuDialogFragment().show(getChildFragmentManager(), "following");
            }
        });

        filter5KmRadiusButton.setOnClickListener(v -> {
            if (displayToggle.isChecked()) {
                boolean isSelected = filter5KmRadiusButton.isSelected();
                filter5KmRadiusButton.setSelected(!isSelected);
                if (!isSelected) {          // If the button is now selected then load the new map with 5km filter
                    loadFollowingLocationsIn5kmRadius();
                } else {                    // If the button in now not selected then load the initial following map itself
                    if (radiusCircle != null) {
                        radiusCircle.remove();
                        radiusCircle = null;
                    }
                    loadFollowingLocations();
                }
            }
        });

        // Enable the filters as the toggle changes
        displayToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mMap.clear();
            // Remove the circle for 5 KM
            if (radiusCircle != null) {
                radiusCircle.remove();
                radiusCircle = null;
            }
            // Display the respective view and enable the filters
            if (displayToggle.isChecked()) {
                loadFollowingLocations();
                filterMoodFollowingButton.setEnabled(true);
                filter5KmRadiusButton.setEnabled(true);
                filterMoodHistoryButton.setEnabled(false);
            } else {
                loadMoodHistoryLocations();
                filterMoodHistoryButton.setEnabled(true);
                filterMoodFollowingButton.setEnabled(false);
                filter5KmRadiusButton.setEnabled(false);
            }
            // Remove all seleection
            filterMoodHistoryButton.setSelected(false);
            filterMoodFollowingButton.setSelected(false);
            filter5KmRadiusButton.setSelected(false);
        });

        return view;
    }

    /**
     * Call when map is ready to be used and creates and instance of google map to be used.
     * Sets up the map with mood markers and the user's location.
     * Also initialize the custom infoWindowView for markers display.
     * @param googleMap
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Initialize the info window view using the fragment's inflater
        infoWindowView = getLayoutInflater().inflate(R.layout.custom_info_window, null);

        // Set the custom info window adapter and click listener
        mMap.setInfoWindowAdapter(this);
        mMap.setOnInfoWindowClickListener(this);

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
     * Enables the blue dot and fetches user location and move the camera to that location.
     * Creates the location call back to update the map as user moves for the radius circle.
     */
    private void enableLocationAndFetch() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);

        // Request last known location from client and save it to callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult.getLastLocation() != null) {
                    currentLocation = locationResult.getLastLocation();
                    // Update the 5 km filter as user moves
                    if (filter5KmRadiusButton.isSelected() && displayToggle.isChecked()) {
                        loadFollowingLocationsIn5kmRadius();
                    }
                }
            }
        };
        // Use the location provider client to get last location of user
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        // Create the latlng object and move camera to this retrieved location
                        LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));
                        // Update the current location and request frequent location updates
                        currentLocation = location;
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
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
                    currentLocation = locationResult.getLastLocation();
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
     * Stops the location updates when fragment not in use so that it doesn't run in background
     */
    @Override
    public void onPause() {
        super.onPause();
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    /**
     * Resumes location updates when the fragment is in use.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mMap != null && currentLocation != null) {
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    /**
     * Loads the mood based location markers of user that are emojis of the specified mood.
     */
    private void loadMoodHistoryLocations(){
        // Current user's profile
        UserProfile currentUserProfile = ((MainActivity) requireActivity()).getCurrentUsername();
        String userName = currentUserProfile.getUsername();
        loadMoodLocations(userName, null);
    }

    /**
     * This method actually queries the database and set the marker that is to be
     * displayed on the map. The marker title is set to the Emotional State with
     * it's respective color. For the following moods markers it displays a snippet for username.
     * @param user: The username of the mood marker owner
     * @param display: The mood or the followings map display
     */
    private void loadMoodLocations(String user, String display) {
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
                            String snippet = "Username: " + location.getUserId();
                            // Add snippet if markers for following display
                            if (display == "following"){
                                mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(markerTitle)
                                        .snippet(snippet)
                                        .icon(getEmotionalLocation(emotionalState)));
                            }
                            // Markers for the mood history display
                            else{
                                mMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title(markerTitle)
                                        .icon(getEmotionalLocation(emotionalState)));
                            }

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

        // Query the database for following display
        db.collection("followings_and_requests")
                .whereEqualTo("follower", userName)
                .whereEqualTo("status", "following")
                .get()
                .addOnSuccessListener(queryFollowingSnapshots -> {
                    for (QueryDocumentSnapshot document : queryFollowingSnapshots) {
                        FollowRequest follow = document.toObject(FollowRequest.class);
                        String followee = follow.getFollowee();
                        loadMoodLocations(followee, "following");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load your following mood locations", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * This function Loads the mood events from the followings and filter them to be
     * in a radius of 5 kilometres from the current location od user and displays the most recent
     * mood event of the users being followed by the owner.
     * Also displays a 5 KM radius circle being updated as the location changes.
     */
    private void loadFollowingLocationsIn5kmRadius(){
        // Current user's profile
        UserProfile currentUserProfile = ((MainActivity) requireActivity()).getCurrentUsername();
        String userName = currentUserProfile.getUsername();

        mMap.clear();
        // Set the radius circle for 5 kilometres
        radiusCircle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .radius(5000)
                .strokeColor(Color.BLACK)
                .strokeWidth(2f)
                .fillColor(Color.argb(50,0,255,0)));

        // Query the followings and requests collection to access all the users who's follower is current user
        db.collection("followings_and_requests")
                .whereEqualTo("follower", userName)
                .whereEqualTo("status", "following")
                .get()
                .addOnSuccessListener(queryFollowingSnapshots -> {
                    // List to save the locations to be displayed
                    List<UserLocation> locationsToDisplay = new ArrayList<>();
                    int followings = queryFollowingSnapshots.size();

                    // Initialize a query counter and iterate over each document snapshot
                    int[] completedQueries = {0};
                    for (QueryDocumentSnapshot document : queryFollowingSnapshots) {
                        FollowRequest follow = document.toObject(FollowRequest.class);
                        String followee = follow.getFollowee();
                        // Query the MoodDB database to get the most recent mood event
                        // Got some errors then logcat gave a link which created indexes in firebase database
                        db.collection("MoodDB")
                                .whereEqualTo("owner.username", followee)
                                .orderBy("postedDate", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener(queryMoodSnapshots -> {
                                    if (!queryMoodSnapshots.isEmpty()) {
                                        DocumentSnapshot moodSnapshot = queryMoodSnapshots.getDocuments().get(0);
                                        String moodId = moodSnapshot.getId();

                                        // Query the locations collection to see if the location exist for such a mood event
                                        db.collection("locations")
                                                .whereEqualTo("moodID", moodId)
                                                .get()
                                                .addOnSuccessListener(queryLocationSnapshots -> {
                                                    if (!queryLocationSnapshots.isEmpty()){
                                                        UserLocation loc = queryLocationSnapshots.getDocuments().get(0).toObject(UserLocation.class);
                                                        // Check the filter if it exists under 5 KM radius of user's current location
                                                        if (radius5km(currentLocation, loc)) {
                                                            locationsToDisplay.add(loc);
                                                        }
                                                    }
                                                    completedQueries[0]++;
                                                    // Display the location marker if it passe all filters
                                                    if (completedQueries[0] == followings) {
                                                        displayLocationsOnMap(locationsToDisplay, filter5KmRadiusButton);
                                                    }

                                                })
                                                .addOnFailureListener(e -> {
                                                    // The mood doesn't have a location set with it
                                                    Log.e("MapsFragment", "Failure: "+ e.getMessage());
                                                    completedQueries[0]++;
                                                    if (completedQueries[0] == followings) {
                                                        displayLocationsOnMap(locationsToDisplay, filter5KmRadiusButton);
                                                    }
                                                });
                                    }
                                    else {
                                        // No mood events for this followee
                                        completedQueries[0]++;
                                        if (completedQueries[0] == followings) {
                                            displayLocationsOnMap(locationsToDisplay, filter5KmRadiusButton);
                                        }
                                    }
                                } )
                                .addOnFailureListener(e->{
                                    Log.e("MapsFragment", "Failure: "+ e.getMessage());
                                    completedQueries[0]++;
                                    if (completedQueries[0] == followings) {
                                        displayLocationsOnMap(locationsToDisplay, filter5KmRadiusButton);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load your following mood locations", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Boolean to filter if the location marker is within 5 kilometres of user's current location
     * @param currentLocation: Current location of the user
     * @param usrLoc: The location marker of followee user
     * @return True if distance is less or equal to 5KM
     */
    private boolean radius5km(Location currentLocation, UserLocation usrLoc) {
        Location moodLocation = new Location("");
        moodLocation.setLatitude(usrLoc.getLatitude());
        moodLocation.setLongitude(usrLoc.getLongitude());
        float distanceInMeters = currentLocation.distanceTo(moodLocation);
        return distanceInMeters <= 5000;
    }

    /**
     * This method apply filters on the map markers based on Mood History filters and the Following filters
     * and displays the filtered markers on the map.
     * @param selectedMoods:    The filter based on state of the mood
     * @param selectedTimeline: The filter based on timeline
     * @param keyword:          The filter based on a keyword in the mood event
     */
    // Taken from MoodHistoryFragment.java
    @Override
    public void onFiltersApplied(ArrayList<EmotionalStates> selectedMoods, String selectedTimeline, List<String> keyword) {
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

                        // Create a list to store all mood events retrieved from Firestore
                        ArrayList<Mood> moodList = new ArrayList<>();
                        // Iterate and convert each into mood abject and add to the list
                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                            moodList.add(createMoodFromSnapshot(snapshot, currentUserProfile));
                        }

                        // Apply the filters
                        applyFilters(moodList, selectedMoods, selectedTimeline, keyword);

                        // Retrieve the filtered moods
                        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();

                        // Extracting the moodIds from the filtered moods
                        List<String> moodIds = filteredMoods.stream()
                                .map(mood -> mood.getDocRef().getId())
                                .collect(Collectors.toList());

                        // Display the filtered mood markers
                        mapFilteredMoods(moodIds, filterMoodHistoryButton, userName,true);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to load moods", Toast.LENGTH_SHORT).show();
                    });
        }
        // Filter the Mood Following
        else {
            // Query the following and request database
            db.collection("followings_and_requests")
                    .whereEqualTo("follower", userName)
                    .whereEqualTo("status", "following")
                    .get()
                    .addOnSuccessListener(queryFollowingSnapshots -> {
                        // If no followings are found then clear the map
                        if (queryFollowingSnapshots.isEmpty()) {
                            mMap.clear();
                            Toast.makeText(requireContext(), "You are not following anyone", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // List to store all filtered mood IDs from followees
                        List<String> allFilteredMoodIds = new ArrayList<>();
                        // Iterate through each following document
                        for (QueryDocumentSnapshot document : queryFollowingSnapshots) {
                            FollowRequest follow = document.toObject(FollowRequest.class);
                            String followee = follow.getFollowee();

                            // Query the MoodDb collections for mood events of followee
                            db.collection("MoodDB")
                                    .whereEqualTo("owner.username", followee)
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {

                                        // A list to store mood events for this followee
                                        ArrayList<Mood> moodList = new ArrayList<>();
                                        // Iterate and convert each into mood abject and add to the list
                                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                                            UserProfile user = new UserProfile(followee);
                                            moodList.add(createMoodFromSnapshot(snapshot, user));
                                        }

                                        // Apply the filters
                                        applyFilters(moodList, selectedMoods, selectedTimeline, keyword);

                                        // Retrieve the filtered moods
                                        ArrayList<Mood> filteredMoods = MoodFiltering.getFilteredMoods();
                                        // Add the filtered mood IDs to the final list
                                        for (Mood mood : filteredMoods) {
                                            allFilteredMoodIds.add(mood.getDocRef().getId());
                                        }
                                        // Display the filtered mood markers
                                        mapFilteredMoods(allFilteredMoodIds, filterMoodFollowingButton, null, false );
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "Failed to load moods", Toast.LENGTH_SHORT).show();

                                    });

                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to load moods following", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * Applies the filters required on the mood display based on the History and Following fragments
     * @param moodList
     * @param selectedMoods
     * @param selectedTimeline
     * @param keyword
     */
    private void applyFilters(ArrayList<Mood> moodList, ArrayList<EmotionalStates> selectedMoods, String selectedTimeline, List<String> keyword) {
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

    /**
     * Maps a list of filtered mood IDs to their corresponding locations and displays them on the map.
     * @param moodIds: The list of ids markers to be displayed on map
     * @param filterButton: The filter being used
     * @param userId: The userId of the logged-in user for personal moods display
     * @param filterByUserId: Boolean if it's personal mood markers
     */
    // Since firebase has a limit of 30 for whereIn argument, we are gonna turn it into chunks.
    // https://stackoverflow.com/questions/61354866/is-there-a-workaround-for-the-firebase-query-in-limit-to-10
    private void mapFilteredMoods(List<String> moodIds, ImageButton filterButton, String userId, boolean filterByUserId){
        if (moodIds.isEmpty()) {
            mMap.clear();
            filterButton.setSelected(false);
            return;
        }
        // List to store all retrieved locations and initialize a counter
        List<UserLocation> locationCollection = new ArrayList<>();
        int[] completedQueries = {0};
        int totalQueries = (int) Math.ceil(moodIds.size() / 30.0);

        // Iterate through moodIds up to 30 items a time
        for (int i = 0; i < moodIds.size(); i += 30) {
            // Create a chunk sublist
            int j = Math.min(i + 30, moodIds.size());
            List<String> chunk = moodIds.subList(i,j);

            // Query the database to get the location
            Query query = db.collection("locations")
                    .whereIn("moodID", chunk);

            // Filter by userId for personal mood history
            if (filterByUserId) {
                query = query.whereEqualTo("userId", userId);
            }
            query.get()
                    .addOnSuccessListener(locationSnapshots -> {
                        // Iterate through each location document
                        for (QueryDocumentSnapshot document : locationSnapshots) {
                            UserLocation location = document.toObject(UserLocation.class);
                            locationCollection.add(location);
                        }
                        completedQueries[0]++;
                        // If all chunks have been processed then display the markers
                        if (completedQueries[0] == totalQueries) {
                            displayLocationsOnMap(locationCollection, filterButton);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to load filtered locations", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    /**
     * This method displays the mood markers on map after filtering
     * @param locationCollection: The collection of markers to be displayed
     * @param filterButton: The filter to be set as selected
     */
    private void displayLocationsOnMap(List<UserLocation> locationCollection, ImageButton filterButton){
        if (filterButton != filter5KmRadiusButton){
            mMap.clear();
        }
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
     * This method resets the filters, displays the map with all mood events and deselects the filter.
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
     * Creates a Mood object from a Firebase document snapshot
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
     * Generate a custom infoView for markers to display the emotional state with consistent colour
     * and also the username when required as snippet.
     * @param marker: The marker to display the infoView on
     * @return
     */
    @Override
    public View getInfoWindow(Marker marker) {
        if (!isAdded() || infoWindowView == null) return null;

        TextView titleTextView = infoWindowView.findViewById(R.id.info_window_title);
        TextView snippetTextView = infoWindowView.findViewById(R.id.info_window_snippet);

        // Set the title and customize its color
        String title = marker.getTitle();
        titleTextView.setText(title);

        // Get the color of the emotional state
        EmotionalStates state = EmotionalStates.fromStateName(title);
        if (state != null) {
            titleTextView.setTextColor(Color.parseColor(state.getColour()));
        } else {
            titleTextView.setTextColor(Color.BLACK);
        }

        // Set the snippet
        if (marker.getSnippet() != null) {
            snippetTextView.setText(marker.getSnippet());
            snippetTextView.setVisibility(View.VISIBLE);
        } else {
            snippetTextView.setVisibility(View.GONE);
        }
        return infoWindowView;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        return null;
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
    }
}