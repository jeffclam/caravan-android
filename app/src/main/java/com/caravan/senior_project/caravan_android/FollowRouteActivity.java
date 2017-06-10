package com.caravan.senior_project.caravan_android;

import android.*;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.caravan.senior_project.users.User;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.MyLocationTracking;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;
import com.mapbox.services.android.location.LostLocationEngine;
import com.mapbox.services.android.navigation.v5.MapboxNavigation;
import com.mapbox.services.android.navigation.v5.NavigationConstants;
import com.mapbox.services.android.navigation.v5.RouteProgress;
import com.mapbox.services.android.navigation.v5.listeners.AlertLevelChangeListener;
import com.mapbox.services.android.navigation.v5.listeners.NavigationEventListener;
import com.mapbox.services.android.navigation.v5.listeners.OffRouteListener;
import com.mapbox.services.android.navigation.v5.listeners.ProgressChangeListener;
import com.mapbox.services.android.navigation.v5.models.RouteLegProgress;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.mapbox.services.api.ServicesException;
import com.mapbox.services.api.directions.v5.models.StepIntersection;
import com.mapbox.services.api.utils.turf.TurfConstants;
import com.mapbox.services.api.utils.turf.TurfMeasurement;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.api.directions.v5.DirectionsCriteria;
import com.mapbox.services.api.directions.v5.MapboxDirections;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;
import com.mapbox.services.api.directions.v5.models.LegStep;
import com.mapbox.services.api.directions.v5.models.RouteLeg;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static java.sql.Types.NULL;

public class FollowRouteActivity extends AppCompatActivity implements OnMapReadyCallback,
        ProgressChangeListener, NavigationEventListener, AlertLevelChangeListener, OffRouteListener {

    private static final String TAG = "FollowRouteActivity";

    // Map variables
    private MapView mapView;
    private MapboxMap map;
    private Polyline routeLine = null;
    private Marker destinationMarker;

    // Navigation related variables
    private DirectionsRoute currentRoute;
    private LocationEngine locationEngine;
    private MapboxNavigation navigation;
    private LocationEngineListener locationEngineListener;
    private Position destination;

    // Firebase related variables
    private FirebaseAuth mAuth;
    private DatabaseReference otherUserRef;
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth.AuthStateListener mAuthListener;
    private User user = new User("jeff@jeff.com");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_route);
        Mapbox.getInstance(this, getString(R.string.access_token));

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

        // Grab destination from previous activity
        Location finish = new Location("dummyProvider");
        Bundle locations = this.getIntent().getBundleExtra("locations");
        if (locations != null) {
            finish = locations.getParcelable("nextLoc");
        }
        destination = Position.fromCoordinates(finish.getLongitude(), finish.getLatitude());

        // Mapbox Setup
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Add a MapboxMap
        mapView.getMapAsync(this);

        navigation = new MapboxNavigation(this, Mapbox.getAccessToken());

        /* Updates map to where the user is once the location changes */
        /*locationEngineListener = new LocationEngineListener() {
            @Override
            public void onConnected() {
                // No action needed
            }

            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    // Move map to where the user location is.
                    updateMap(getLocation().getLatitude(), getLocation().getLongitude(), destination);
                    *//* Might or might not need this?
                    // Removes listener so it's not constantly updating
                    locationEngine.removeLocationEngineListener(this);
                    *//*
                }
            }
        };*/
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.map = mapboxMap;

        mapboxMap.moveCamera(CameraUpdateFactory.zoomBy(12));

        locationEngine = new MockLocationEngine();
        //map.setLocationSource(locationEngine);

        // Center to current location
        updateMap(getLocation().getLatitude(), getLocation().getLongitude(), destination);
        // Mark to final destination
        mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(destination.getLatitude(), destination.getLongitude())));

        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            mapboxMap.setMyLocationEnabled(true);
            mapboxMap.getTrackingSettings().setMyLocationTrackingMode(MyLocationTracking.TRACKING_FOLLOW);
            mapboxMap.getTrackingSettings().setDismissAllTrackingOnGesture(false);
        }

        Log.d("onMapReady: ", "Calculating route");
        calculateRoute();
        Log.d("onMapReady: ", "returned from calculate");

        if (currentRoute == null)
            Log.e("onMapReady: ", "Route is null");

        // Attach all of our navigation listeners.
        navigation.addNavigationEventListener(FollowRouteActivity.this);
        navigation.addProgressChangeListener(FollowRouteActivity.this);
        navigation.addAlertLevelChangeListener(FollowRouteActivity.this);

        // Adjust location engine to force a gps reading every second. This isn't required but gives an overall
        // better navigation experience for users. The updating only occurs if the user moves 3 meters or further
        // from the last update.
        locationEngine.setInterval(0);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.setFastestInterval(1000);
        locationEngine.activate();

        /*((MockLocationEngine) locationEngine).setRoute(currentRoute);
        navigation.setLocationEngine(locationEngine);
        navigation.startNavigation(currentRoute);*/
    }


    private void updateMap(double latitude, double longitude, Position destination) {
        // Animate camera to geocoder result location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(16)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);

        try {
            calculateRoute();
        } catch (ServicesException servicesException) {
            servicesException.printStackTrace();
        }
    }

    private Location getLocation() throws SecurityException{
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
                    locationEngine.removeLocationEngineListener(this);
                }
            }
        };

        map.setMyLocationEnabled(true);

        return lastLocation;
    }

    private void calculateRoute() {
        Location userLocation = map.getMyLocation();
        if (userLocation == null) {
            Timber.d("calculateRoute: User location is null, therefore, origin can't be set.");
            return;
        }

        Position origin = (Position.fromCoordinates(userLocation.getLongitude(), userLocation.getLatitude()));
        if (TurfMeasurement.distance(origin, destination, TurfConstants.UNIT_METERS) < 50) {
            map.removeMarker(destinationMarker);
            return;
        }

        navigation.getRoute(origin, destination, new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user");
                    return;
                } else if (response.body().getRoutes().size() < 1) {
                    Log.e(TAG, "No routes found");
                    return;
                }

                currentRoute = response.body().getRoutes().get(0);
                Log.d(TAG, "Distance: " + currentRoute.getDistance());
                Toast.makeText(
                        FollowRouteActivity.this,
                        "Route is " + currentRoute.getDistance() + " meters long.",
                        Toast.LENGTH_SHORT).show();
                ((MockLocationEngine) locationEngine).setRoute(currentRoute);
                navigation.setLocationEngine(locationEngine);
                navigation.startNavigation(currentRoute);
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Timber.e("onFailure: navigation.getRoute()", throwable);
            }
        });
    }

    /*private void getRoute(Position origin, Position destination) throws ServicesException {
        MapboxNavigation navigation = new MapboxNavigation(this, Mapbox.getAccessToken());

        LocationEngine locationEngine = LostLocationEngine.getLocationEngine(this);
        navigation.setLocationEngine(locationEngine);

        navigation.getRoute(origin, destination, new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user");
                    return;
                } else if (response.body().getRoutes().size() < 1) {
                    Log.e(TAG, "No routes found");
                    return;
                }

                currentRoute = response.body().getRoutes().get(0);
                Log.d(TAG, "Distance: " + currentRoute.getDistance());
                Toast.makeText(
                        FollowRouteActivity.this,
                        "Route is " + currentRoute.getDistance() + " meters long.",
                        Toast.LENGTH_SHORT).show();

                if (currentRoute != null) {
                    RouteLeg routeLeg = currentRoute.getLegs().get(0);
                    Log.d(TAG, "Next route distance: " + routeLeg.getDistance());
                    if (routeLeg.getSteps().size() > 0) {
                        LegStep nextStep;
                        if (routeLeg.getSteps().size() > 1)
                            nextStep = routeLeg.getSteps().get(1);
                        else
                            nextStep = routeLeg.getSteps().get(0);
                        Log.d(TAG, "Next route leg summary: " + nextStep.getDistance());
                        TextView nextDir = (TextView) findViewById(R.id.Nextdirection);
                        TextView timeRem = (TextView) findViewById(R.id.TimeRemaining);
                        TextView distRem = (TextView) findViewById(R.id.DistanceLeft);
                        ImageView arrow = (ImageView) findViewById(R.id.arrow);

                        int timeMin = (int) (currentRoute.getDuration() / 60);
                        double dist = currentRoute.getDistance() / 1609.34;
                        String diststr = String.format("%.1f miles", dist);

                        nextDir.setText(nextStep.getManeuver().getInstruction());
                        timeRem.setText(timeMin + " minutes");
                        distRem.setText(diststr);
                        //arrow.setImageResource("@drawable/");

                        FirebaseUser fb_user = mAuth.getCurrentUser();
                        if (fb_user != null) {
                            Log.v(TAG, "Uid: " + fb_user.getUid());
                            user.setRoute(currentRoute);
                            MyDirectionsRoute myroute = new MyDirectionsRoute(currentRoute);
                            dbRef.child("users").child(fb_user.getUid()).child("route").setValue(myroute);
                            Log.v(TAG, "user route set");
                        }

                        //database.child("users")
                    } else
                        Log.d(TAG, "Next route leg summary: No step found");
                }
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                Toast.makeText(FollowRouteActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }*/

    private void drawRoute(DirectionsRoute route) {
        LineString lineString  = LineString.fromPolyline(route.getGeometry(), Constants.PRECISION_6);
        List<Position> positions = lineString.getCoordinates();
        List<LatLng> latLngs = new ArrayList<>();

        for (Position position : positions) {
            latLngs.add(new LatLng(position.getLatitude(), position.getLongitude()));
        }

        if (routeLine != null) {
            map.removePolyline(routeLine);
        }

        /* Draw the lines */
        routeLine = map.addPolyline(new PolylineOptions()
                .addAll(latLngs)
                .color(Color.parseColor("#009688"))
                .width(5f));
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

     /*
   * Navigation listeners
   */

    @Override
    public void onRunning(boolean running) {
        if (running) {
            Timber.d("onRunning: Started");
        } else {
            Timber.d("onRunning: Stopped");
        }
    }

    @Override
    public void onProgressChange(Location location, RouteProgress routeProgress) {
        Timber.d("onProgressChange: fraction of route traveled: %f", routeProgress.getFractionTraveled());
        RouteLegProgress curLeg = routeProgress.getCurrentLegProgress();
        LegStep curStep = curLeg.getCurrentStep();

        Log.d(TAG, "Next route leg summary: " + curLeg.getCurrentStepProgress());
        TextView nextDir = (TextView) findViewById(R.id.Nextdirection);
        TextView timeRem = (TextView) findViewById(R.id.TimeRemaining);
        TextView distRem = (TextView) findViewById(R.id.DistanceLeft);
        ImageView arrow = (ImageView) findViewById(R.id.arrow);

        int timeMin = (int) (curLeg.getDurationRemaining() / 60);
        double dist = curLeg.getDistanceRemaining() / 1609.34;
        String diststr = String.format("%.1f miles", dist);

        nextDir.setText(curStep.getManeuver().getInstruction());
        timeRem.setText(timeMin + " minutes");
        distRem.setText(diststr);
    }

    @Override
    public void onAlertLevelChange(int alertLevel, RouteProgress routeProgress) {

        switch (alertLevel) {
            case NavigationConstants.HIGH_ALERT_LEVEL:
                Toast.makeText(FollowRouteActivity.this, "HIGH", Toast.LENGTH_LONG).show();
                break;
            case NavigationConstants.MEDIUM_ALERT_LEVEL:
                Toast.makeText(FollowRouteActivity.this, "MEDIUM", Toast.LENGTH_LONG).show();
                break;
            case NavigationConstants.LOW_ALERT_LEVEL:
                Toast.makeText(FollowRouteActivity.this, "LOW", Toast.LENGTH_LONG).show();
                break;
            case NavigationConstants.ARRIVE_ALERT_LEVEL:
                Toast.makeText(FollowRouteActivity.this, "ARRIVE", Toast.LENGTH_LONG).show();
                break;
            case NavigationConstants.DEPART_ALERT_LEVEL:
                Toast.makeText(FollowRouteActivity.this, "DEPART", Toast.LENGTH_LONG).show();
                break;
            default:
            case NavigationConstants.NONE_ALERT_LEVEL:
                Toast.makeText(FollowRouteActivity.this, "NONE", Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void userOffRoute(Location location) {
        Position newOrigin = Position.fromCoordinates(location.getLongitude(), location.getLatitude());
        navigation.getRoute(newOrigin, destination, new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                DirectionsRoute route = response.body().getRoutes().get(0);
                FollowRouteActivity.this.currentRoute = route;

                // Remove old route line from map and draw the new one.
                if (routeLine != null) {
                    map.removePolyline(routeLine);
                }
                drawRoute(route);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Timber.e("onFailure: navigation.getRoute()", throwable);
            }
        });
    }

    /*
   * Activity lifecycle methods
   */
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

        // Remove all navigation listeners
        navigation.removeAlertLevelChangeListener(this);
        navigation.removeNavigationEventListener(this);
        navigation.removeProgressChangeListener(this);
        navigation.removeOffRouteListener(this);

        // End the navigation session
        navigation.endNavigation();
    }

    public void logOut() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        this.finish();
    }
}
