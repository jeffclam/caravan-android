package com.caravan.senior_project.caravan_android;

import android.*;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;
import com.mapbox.services.android.location.LostLocationEngine;
import com.mapbox.services.android.navigation.v5.MapboxNavigation;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.api.ServicesException;
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
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.sql.Types.NULL;

public class FollowRouteActivity extends AppCompatActivity {

    private static final String TAG = "FollowRouteActivity";

    private MapView mapView;
    private MapboxMap map;
    private DirectionsRoute currentRoute;
    private Polyline routeLine = null;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;
    private DatabaseReference otherUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_route);
        Mapbox.getInstance(this, getString(R.string.access_token));

        locationEngine = LocationSource.getLocationEngine(this);
        locationEngine.activate();

        // Grab destination from previous activity
        Location finish = new Location("dummyProvider");
        Bundle locations = this.getIntent().getBundleExtra("locations");
        if (locations != null) {
            finish = locations.getParcelable("nextLoc");
        }
        final Position destination =
                Position.fromCoordinates(finish.getLongitude(), finish.getLatitude());

        // Mapbox Setup
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Add a MapboxMap
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                // Center to current location
                updateMap(getLocation().getLatitude(), getLocation().getLongitude(), destination);
                // Mark to final destination
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(destination.getLatitude(), destination.getLongitude())));
            }
        });

        /* Updates map to where the user is once the location changes */
        locationEngineListener = new LocationEngineListener() {
            @Override
            public void onConnected() {
                // No action needed
            }

            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    // Move map to where the user location is.
                    updateMap(getLocation().getLatitude(), getLocation().getLongitude(), destination);
                    /* Might or might not need this?
                    // Removes listener so it's not constantly updating
                    locationEngine.removeLocationEngineListener(this);
                    */
                }
            }
        };
    }

    private void updateMap(double latitude, double longitude, Position destination) {
        // Animate camera to geocoder result location
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(16)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 5000, null);

        try {
            getRoute(
                    Position.fromCoordinates(getLocation().getLongitude(),
                            getLocation().getLatitude()),
                    destination);
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
            } else
                Log.d(TAG, "Next route leg summary: No step found");
        }

        return lastLocation;
    }

    private void getRoute(Position origin, Position destination) throws ServicesException {
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
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                Toast.makeText(FollowRouteActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

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
}
