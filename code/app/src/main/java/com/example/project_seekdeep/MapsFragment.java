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
import android.widget.Toast;
import android.Manifest;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MAP fragments uses google maps API to show the user's current location and mood based emoji markers.
 * It retrieves locations from the firebase database and displays them on the map with specified mood details.
 * Resources used:
 *  https://stackoverflow.com/questions/62111235/how-do-you-use-a-google-mapview-inside-of-a-fragment
 *  https://developer.android.com/training/data-storage/shared-preferences#java
 *  https://stackoverflow.com/questions/74367916/how-can-i-create-location-request-in-android-locationrequest-create-is-depr
 *  https://youtube.com/playlist?list=PLHQRWugvckFrWppucVnQ6XhiJyDbaCU79&si=LXVl0HjJwen_ij05
 *  https://developers.google.com/maps/documentation/android-sdk/reference/com/google/android/libraries/maps/model/BitmapDescriptorFactory#HUE_YELLOW
 *  https://stackoverflow.com/questions/17839388/creating-a-scaled-bitmap-with-createscaledbitmap-in-android
 *  https://stackoverflow.com/questions/47807621/draw-emoji-on-bitmap-with-drawtextonpath?utm_source=chatgpt.com
 * @author Saurabh
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;       // API to request location permission
    private String currentMapView;

    // Image buttons that are selectable
    private ImageButton filterMoodHistoryButton;
    private ImageButton filterMoodFollowingButton;
    private ImageButton filter5KmRadiusButton;

    /**
     * Empty constructor required by database
     */
    public MapsFragment() {
    }

    /**
     * Inflates the layout and initializes map and location
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

        // Initialize the map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);

        // Prepare the map
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }
        // Used fused location client to get location from google services
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        // Location request for high accuracy every 10 secs
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
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

        // Initialize the filtered mood history and other selectable buttons
        filterMoodHistoryButton.setOnClickListener(v -> {
            if (currentMapView == "Mood History") {
                currentMapView = "";
                filterMoodHistoryButton.setSelected(false);
            } else {
                currentMapView = "Mood History";
                filterMoodHistoryButton.setSelected(true);
                filterMoodFollowingButton.setSelected(false);
                filter5KmRadiusButton.setSelected(false);
            }
            loadMap();
        });

        // Initialize the filtered mood following and other selectable buttons
        filterMoodFollowingButton.setOnClickListener(v -> {
            if (currentMapView == "Mood Following") {
                currentMapView = "";
                filterMoodFollowingButton.setSelected(false);
            } else {
                currentMapView = "Mood Following";
                filterMoodHistoryButton.setSelected(false);
                filterMoodFollowingButton.setSelected(true);
                filter5KmRadiusButton.setSelected(false);
            }
            loadMap();
        });

        // Initialize the filtered 5 KM radius and other selectable buttons
        filter5KmRadiusButton.setOnClickListener(v -> {
            if (currentMapView == "5 KM radius") {
                currentMapView = "";
                filter5KmRadiusButton.setSelected(false);
            } else {
                currentMapView = "5 KM radius";
                filterMoodHistoryButton.setSelected(false);
                filterMoodFollowingButton.setSelected(false);
                filter5KmRadiusButton.setSelected(true);
            }
            loadMap();
        });

        return view;
    }

    /**
     * Call when map is ready to be used and creates and instance of google map to be used.
     * Sets up the map with mood markers and the user's location.
     * @param googleMap
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Attempt to enable location
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableLocationAndFetch();
        }
        else {
            // If permission isn’t granted, request it
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        loadMap();
    }

    /**
     * This method checks which display is selected and loads that display
     */
    private void loadMap() {
        mMap.clear(); // Clear existing markers
        if ("Mood History".equals(currentMapView)) {
            loadMoodHistoryLocations();
        }
        else if ("Mood Following".equals(currentMapView)) {
            loadMoodFollowingLocations();
        }
        else if ("5 KM radius".equals(currentMapView)) {
            loadFilter5kmRadiusLocations();
        }
        else{
            loadMoodLocations();
        }
    }

    /**
     * This method loads the the emoticon markers on map that have been filtered from the mood history tab.
     * When they are reset on the history tab all the filters are off and map just displays every Mood event of the user.
     */
    private void loadMoodHistoryLocations() {
        // Get the current user to match with database
        UserProfile currentUserProfile = ((MainActivity) requireActivity()).getCurrentUsername();
        String userName = currentUserProfile.getUsername();

        // Access the shared preference that stores the filter mood data
        SharedPreferences sharedPref = requireContext().getSharedPreferences("MoodFilter", Context.MODE_PRIVATE);
        String moodIdsString = sharedPref.getString("filtered_mood_ids", "");

        // If the data is empty then load normal view
        if (moodIdsString.isEmpty()){
            Toast.makeText(requireContext(), "No Filter applied or such event doesn't exist", Toast.LENGTH_SHORT).show();
            loadMoodLocations();
            return;
        }

        // Convert the comma separated ID string into a list
        List<String> mooodIds = Arrays.asList(moodIdsString.split(","));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Match the database locations with current user
        db.collection("locations")
                .whereEqualTo("userId", userName)
                .whereIn("moodID", mooodIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Iterate throught each item in the database collection 'locations'
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserLocation location = document.toObject(UserLocation.class);
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        EmotionalStates emotionalState = location.getEmotionalState();;
                        String markerTitle = emotionalState.getStateName();
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(markerTitle)
                                .icon(getEmotionalLocation(emotionalState)));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load your mood locations", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * This method loads the the emoticon markers on map that have been filtered from the mood following tab.
     */
    private void loadMoodFollowingLocations(){
        Toast.makeText(requireContext(), "View is in development", Toast.LENGTH_SHORT).show();
    }

    /**
     * This method filter the map by radius and only displays markers in 5 KM vicinity
     */
    private void loadFilter5kmRadiusLocations(){
        Toast.makeText(requireContext(), "View is in development", Toast.LENGTH_SHORT).show();
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
     * Loads the mood based location markers that are emojis of the specified mood.
     */
    private void loadMoodLocations() {
        // Current user's profile
        UserProfile currentUserProfile = ((MainActivity) requireActivity()).getCurrentUsername();
        String userName = currentUserProfile.getUsername();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Match the database locations with current user
        db.collection("locations")
                .whereEqualTo("userId", userName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Iterate throught each item in the database collection 'locations'
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        UserLocation location = document.toObject(UserLocation.class);
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        EmotionalStates emotionalState = location.getEmotionalState();;
                        String markerTitle = emotionalState.getStateName();
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(markerTitle)
                                .icon(getEmotionalLocation(emotionalState)));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load your mood locations", Toast.LENGTH_SHORT).show();
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
}