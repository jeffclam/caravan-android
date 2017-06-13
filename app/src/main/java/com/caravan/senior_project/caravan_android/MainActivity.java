package com.caravan.senior_project.caravan_android;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.caravan.senior_project.users.RoomManager;
import com.caravan.senior_project.users.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
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
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.api.geocoding.v5.models.CarmenFeature;
import java.util.List;


public class MainActivity extends AppCompatActivity implements PermissionsListener {

    public static String TAG = "MainActivity";

    private String[] myDrawerButtons;
    private ListView myDrawerList;

    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbRef = database.getReference();
    private User user;
    private RoomManager rm;
    private String roomKey;

    private MapView mapView;
    private MapboxMap map;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;
    private Location nextLoc = new Location("dummyProvider");
    private PermissionsManager permissionsManager;

    private Button get_directions;

    GeocoderAutoCompleteView autocomplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));

        // Layout Configurations
        setContentView(R.layout.activity_main);

        // Set get buttons invisible
        get_directions = (Button) findViewById(R.id.get_directions_button);
        get_directions.setVisibility(View.INVISIBLE);

        // Initialize Drawer
        myDrawerButtons = getResources().getStringArray(R.array.drawer_array);
        myDrawerList = (ListView) findViewById(R.id.left_drawer);
        myDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, myDrawerButtons));
        myDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // Get the LocationEngine to track current location
        locationEngine = LocationSource.getLocationEngine(this);
        locationEngine.activate();

        // Initialize mAuth
        mAuth = FirebaseAuth.getInstance();

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
        autocomplete = (GeocoderAutoCompleteView) findViewById(R.id.query);
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

        if (user == null) {
            FirebaseUser fb_user = mAuth.getCurrentUser();
            if (fb_user != null) {
                user = new User();
                user.setUID(fb_user.getUid());
                try {
                    user.setLocation(getLocation());
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
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
                    } else {
                        Log.e(TAG, "No location???");
                    }
                }
            };
            locationEngine.addLocationEngineListener(locationEngineListener);
            map.setMyLocationEnabled(true);

            if (lastLocation != null) {
                    user.setLocation(lastLocation);
                    dbRef.child("users").child(user.getUID()).child("location").
                            setValue(user.getLocation());
                    Log.v(TAG, "User set and sent to DB");
            }
            return lastLocation;
        } catch (SecurityException security) {
            Log.e(TAG, "Permission not granted");
        }
        return null;
    }

    public void getRoom(View view) {
        Log.d(TAG, "get_room_button pressed");
        roomKey = autocomplete.getText().toString();
        if (roomKey.length() != 4) {
            Toast.makeText(MainActivity.this, "Room keys are 4 characters long",
                    Toast.LENGTH_SHORT).show();
        } else {
            if (rm == null) {
                rm = new RoomManager();
            }

            if (user.getLocation() == null && user.getUID() == null) {
                user.setLocation(getLocation());
            }

            rm.readRoom(roomKey, user.getUID(), new RoomManager.waitTilReady() {
                @Override
                public void roomExists(boolean exists) {
                    if (exists) {
                        rm.showRoommates(map, MainActivity.this);
                    }
                }
            });

            if (rm.getRoom() == null) {
                Toast.makeText(MainActivity.this, "Room does not exist",
                        Toast.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "readRoom coords: " + rm.getLatitude() + ", " + rm.getLongitude());
                updateMap(rm.getLatitude(), rm.getLongitude());
                nextLoc.setLatitude(rm.getLatitude());
                nextLoc.setLongitude(rm.getLongitude());
                get_directions.setVisibility(View.VISIBLE);
            }
        }
    }

     public void openDirections(View view) {
         Intent intent = new Intent(this, MapRouteActivity.class);
         Bundle bundle = new Bundle();
         bundle.putParcelable("currentLoc", getLocation());
         if (roomKey == null) {
             roomKey = "";
         }
         bundle.putParcelable("nextLoc", nextLoc);
         bundle.putString("roomKey", roomKey);
         bundle.putString("uid", user.getUID());
         intent.putExtra("bundle", bundle);
         startActivity(intent);
    }

    public void logOut() {
        Log.d(TAG, "logout button pressed");
        mAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        this.finish();
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        Log.d(TAG, "drawer list position " + position + " clicked.");
        switch (position) {
            case 0:
                logOut();
                break;
            default:
                break;
        }
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
    public void onStop() {
        super.onStop();
        mapView.onStop();
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
