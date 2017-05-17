package com.caravan.senior_project.caravan_android;

import android.animation.TypeEvaluator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.ui.geocoder.GeocoderAutoCompleteView;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.api.geocoding.v5.models.CarmenFeature;

import java.util.List;


public class MainActivity extends AppCompatActivity implements PermissionsListener {

    public static String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbRef = database.getReference();
    DatabaseReference otherUserRef;
    DatabaseReference myUserRef;

    private MapView mapView;
    private MapboxMap map;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;
    private Location nextLoc = new Location("dummyProvider");
    private User user;
    private Coord friendCoord;
    private DirectionsRoute route;
    private PermissionsManager permissionsManager;

    private static final int PERMISSIONS_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);

        // Icon object
        IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        final Icon icon = iconFactory.fromResource(R.drawable.blue_marker);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //user is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in" + user.getUid());
                } else {
                    //user not signed in
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    logOut();
                }
            }
        };

        locationEngine = LocationSource.getLocationEngine(this);
        locationEngine.activate();

        Button current_location_button = (Button) findViewById(R.id.current_location_button);
        final Button get_directions = (Button) findViewById(R.id.get_directions_button);
        get_directions.setVisibility(View.INVISIBLE);

        // Create a mapView
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Add a MapboxMap
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
            }
        });

        /* Auto-completes the search bar for known locations */
        GeocoderAutoCompleteView autocomplete = (GeocoderAutoCompleteView) findViewById(R.id.query);
        autocomplete.setAccessToken(Mapbox.getAccessToken());
        autocomplete.setType(GeocodingCriteria.TYPE_POI);
        autocomplete.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
            @Override
            public void onFeatureClick(CarmenFeature feature) {
                Position position = feature.asPosition();
                updateMap(position.getLatitude(), position.getLongitude());
                nextLoc.setLatitude(position.getLatitude());
                nextLoc.setLongitude(position.getLongitude());
                get_directions.setVisibility(View.VISIBLE);
            }
        });

        /* Gets reference to other user */
        otherUserRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child("BKGE9xrtP5V6QwWYirRF3Rxpkdv2")
                .child("coord");
        Log.d(TAG, "DBRef Found: " + otherUserRef.toString());

        /* Listens to when the other location in the database changes */
        ValueEventListener otherLocation = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendCoord = dataSnapshot.getValue(Coord.class);
                if (dataSnapshot.exists()) {
                    Log.v(TAG, "Other Coords: " + friendCoord.getLatitude() + ", " + friendCoord.getLongitude());
                    
                    /* Adds marker to friend's location */
                    map.addMarker(new MarkerViewOptions()
                        .position(new LatLng(friendCoord.getLatitude(), friendCoord.getLongitude()))
                        .icon(icon));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Load failed: ", databaseError.toException());
            }
        };

        Log.v(TAG, "Value Event Listener: ");
        otherUserRef.addValueEventListener(otherLocation);
        Log.v(TAG, "Success");


        myUserRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child("oB0gb53K0rS7kW7vBbMAl7Co1h03")
                .child("route");
        /* Listens to route in the database changes */
        ValueEventListener the_route = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //route = dataSnapshot.getValue(DirectionsRoute.class);
                if (dataSnapshot.exists()) {
                    Log.v(TAG, "got a route");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Load failed: ", databaseError.toException());
            }
        };
        myUserRef.addValueEventListener(the_route);
    }

    private void updateMap(double latitude, double longitude) {
        // Build marker
        map.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title("Geocoder result"));

        // Animate camera to geocoder result location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(15)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);
    }

    private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
        // Method is used to interpolate the marker animation.

        private LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    }

    public void currentLocation(View view) {
        Log.d(TAG, "CurrentLocationButton");
        permissionsManager = new PermissionsManager(this);
        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            permissionsManager.requestLocationPermissions(this);
        } else {
            Log.d(TAG, "CurrentLocationButton: getLocation()");
            getLocation();
        }
    }

    private Location getLocation() {
        // Tries to get the last location of the user. If permissions not granted, fails.
        try {
            Location lastLocation = locationEngine.getLastLocation();
            if (lastLocation != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
            }

            locationEngineListener = new LocationEngineListener() {
                @Override
                public void onConnected() {
                    // No action needed
                }

                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Move map to where the user location is.
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
                        // Removes listener so it's not constantly updating
                        locationEngine.removeLocationEngineListener(this);
                    }
                }
            };

            map.setMyLocationEnabled(true);

            if (lastLocation != null) {
                FirebaseUser fb_user = mAuth.getCurrentUser();
                if (fb_user != null) {
                    user = new User(fb_user.getEmail());
                    Log.v(TAG, "Uid: " + fb_user.getUid());
                    user.setCoords(lastLocation.getLatitude(), lastLocation.getLongitude());
                    dbRef.child("users").child(fb_user.getUid()).child("user").setValue(user);
                    Log.v(TAG, "User set and sent to DB");
                }
            }
            return lastLocation;
        } catch (SecurityException security) {
            Log.e(TAG, "Permission not granted");
        }
        return null;
    }

     public void openDirections(View view) {
        Intent intent = new Intent(this, MapRouteActivity.class);
        Bundle locations = new Bundle();
        locations.putParcelable("currentLoc", getLocation());
        locations.putParcelable("nextLoc", nextLoc);
        intent.putExtra("locations", locations);
        startActivity(intent);
    }

    public void logOut() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        this.finish();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "This app needs location permissions in order to show its functionality.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            getLocation();
        } else {
            Toast.makeText(this, "You didn't grant location permissions.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
