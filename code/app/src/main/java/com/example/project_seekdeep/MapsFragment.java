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
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.Manifest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
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

/**
 * MAP fragments uses google maps API to show the user's current location and mood based emoji markers.
 * It retrieves locations from the firebase database and displays them on the map with specified mood details.
 * Resources used:
 *  https://stackoverflow.com/questions/62111235/how-do-you-use-a-google-mapview-inside-of-a-fragment
 *  https://developer.android.com/training/data-storage/shared-preferences#java
 *  https://stackoverflow.com/questions/74367916/how-can-i-create-location-request-in-android-locationrequest-create-is-depr
 *  https://youtube.com/playlist?list=PLHQRWugvckFrWppucVnQ6XhiJyDbaCU79&si=LXVl0HjJwen_ij05
 *  https://developer.android.com/training/data-storage/shared-preferences#java
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;       // API to request location permission

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
        loadMoodLocations();            // Load the mood based markers

        // Retrieve location based setting from shared Preferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("LocationPref", Context.MODE_PRIVATE);
        boolean isLocationEnabled = sharedPreferences.getBoolean("location_enabled", false);

        // Attempt to enable location
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableLocationAndFetch();
        }
        else {
            // If permission isn’t granted, request it and update shared preference
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("location_enabled", true);
            editor.apply();
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
                        String markerTitle = emotionalState.toString();
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
        int drawableId; // Variable for the emoji gif
        if (emotionalState == EmotionalStates.SHAME) {drawableId = R.drawable.shame;}
        else if (emotionalState == EmotionalStates.SADNESS) {drawableId = R.drawable.sad;}
        else if (emotionalState == EmotionalStates.ANGER) {drawableId = R.drawable.angry;}
        else if (emotionalState == EmotionalStates.CONFUSION) {drawableId = R.drawable.confused;}
        else if (emotionalState == EmotionalStates.DISGUST) {drawableId = R.drawable.disgusted;}
        else if (emotionalState == EmotionalStates.HAPPINESS) {drawableId = R.drawable.happy;}
        else if (emotionalState == EmotionalStates.SURPRISE) {drawableId = R.drawable.surprised;}
        else if (emotionalState == EmotionalStates.FEAR) {drawableId = R.drawable.fear;}
        else {return BitmapDescriptorFactory.defaultMarker();}
        // Scale the drawable to bitmap
        return resizeMapIcons(drawableId, 70, 70);
    }


    /**
     * Scales the drawable resource to a bitmap descriptor of specified height and width
     * @param drawableId: The drawable object
     * @param width: The specified width
     * @param height: The specified height
     * @return
     */
    private BitmapDescriptor resizeMapIcons(int drawableId, int width, int height) {
        Drawable drawable = ContextCompat.getDrawable(requireContext(), drawableId);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);        // Create a bitmap with high quality ARGB
        Canvas canvas = new Canvas(bitmap);         // Generate a blank canvas
        drawable.setBounds(0, 0, width, height);        // Set the bound for canvas
        drawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);          // Convert bitmap to Bit Map Descriptor
    }
}