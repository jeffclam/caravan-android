package com.caravan.senior_project.caravan_android;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;
import com.mapbox.services.android.location.LostLocationEngine;
import com.mapbox.services.android.navigation.v5.MapboxNavigation;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.api.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.api.directions.v5.DirectionsCriteria;
import com.mapbox.services.api.directions.v5.MapboxDirections;
import com.mapbox.services.api.directions.v5.models.DirectionsResponse;
import com.mapbox.services.api.directions.v5.models.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.caravan.senior_project.users.*;

public class MapRouteActivity extends AppCompatActivity {

    private static final String TAG = "MapRouteActivity";

    private MapView mapView;
    private MapboxMap map;
    private DirectionsRoute currentRoute;
    private Location nextLoc;
    private Polyline routeLine;
    private int roomCode;
    private RoomManager rm;

    // Firebase related variables
    private FirebaseAuth mAuth;
    private DatabaseReference otherUserRef;
    private DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String uid;
    private String roomKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.access_token));

        setContentView(R.layout.activity_directions);

        mAuth = FirebaseAuth.getInstance();

        // Icon object for custom marker image
        IconFactory iconFactory = IconFactory.getInstance(MapRouteActivity.this);
        final Icon icon = iconFactory.fromResource(R.drawable.blue_marker);

        // Initialize a start and end from points sent from MainActivity
        Location start = new Location("dummyProvider");
        Location finish = new Location("dummyProvider");
        Bundle bundle = this.getIntent().getBundleExtra("bundle");
        if (bundle != null) {
            start = bundle.getParcelable("currentLoc");
            finish = bundle.getParcelable("nextLoc");
            roomKey = bundle.getString("roomKey");
            uid = bundle.getString("uid");
            nextLoc = finish;

            final Position origin =
                    Position.fromCoordinates(start.getLongitude(), start.getLatitude());
            final Position destination =
                    Position.fromCoordinates(finish.getLongitude(), finish.getLatitude());
            /* Midpoint between origin and destination */
            final LatLng centroid = new LatLng(
                    (origin.getLatitude() + destination.getLatitude()) / 2,
                    (origin.getLongitude() + destination.getLongitude()) / 2
            );
            if (roomKey.equals("")) {
                roomCode = generateRoomCode();
                dbRef.child("rooms").child(Integer.toString(roomCode)).child("start").child("1").setValue(start.getLongitude());
                dbRef.child("rooms").child(Integer.toString(roomCode)).child("start").child("0").setValue(start.getLatitude());
                dbRef.child("rooms").child(Integer.toString(roomCode)).child("finish").child("1").setValue(finish.getLongitude());
                dbRef.child("rooms").child(Integer.toString(roomCode)).child("finish").child("0").setValue(finish.getLatitude());
                FirebaseUser fb_user = mAuth.getCurrentUser();
                if (fb_user != null) {
                    dbRef.child("rooms").child(Integer.toString(roomCode)).child("users").child(fb_user.getUid()).child("1").
                            setValue(start.getLongitude());
                    dbRef.child("rooms").child(Integer.toString(roomCode)).child("users").child(fb_user.getUid()).child("0").
                            setValue(start.getLatitude());
                }
            } else {
                roomCode = Integer.parseInt(roomKey);
                rm = new RoomManager();
            }

            TextView codeView = (TextView) findViewById(R.id.Roomcode);
            codeView.setText("Room Code: " + roomCode);

            // Create a mapView
            mapView = (MapView) findViewById(R.id.mapview);
            mapView.onCreate(savedInstanceState);

            // Add a MapboxMap
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    map = mapboxMap;

                /* Set the camera's position to middle of origin and destination */
                    CameraPosition position = new CameraPosition.Builder()
                            .target(new LatLng(centroid.getLatitude(), centroid.getLongitude()))
                            .build();
                    map.moveCamera(CameraUpdateFactory
                            .newCameraPosition(position));

                /* Set a marker at the starting location */
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(origin.getLatitude(), origin.getLongitude()))
                            .icon(icon));

                /* Set a marker at the destination location */
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(destination.getLatitude(), destination.getLongitude())));

                    try {
                        getRoute(origin, destination);
                    } catch (ServicesException servicesException) {
                        servicesException.printStackTrace();
                    }

                    if (rm != null) {
                        rm.readRoom(roomKey, uid, new RoomManager.waitTilReady() {
                            @Override
                            public void roomExists(boolean exists) {
                                if (exists) {
                                    rm.showRoommates(map, MapRouteActivity.this);
                                } else {
                                    if (rm.getRoom() == null) {
                                        Toast.makeText(MapRouteActivity.this, "Room does not exist",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    } else {
                        Log.e(TAG, "rm.room is empty rn");
                    }

                }

            });
        }
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
                        MapRouteActivity.this,
                        "Route is " + currentRoute.getDistance() + " meters long.",
                        Toast.LENGTH_SHORT).show();
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
                Toast.makeText(MapRouteActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* Call MapBox commands to draw a route onto the map */
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
    
     /* Start route, send location information to the next activity */
     public void startRoute(View view) {
         Intent intent = new Intent(this, FollowRouteActivity.class);
         Bundle locations = new Bundle();
         locations.putParcelable("nextLoc", nextLoc);
         intent.putExtra("locations", locations);
         intent.putExtra("ROOM_CODE", roomCode);
         intent.putExtra("UID", uid);
         startActivity(intent);
    }

    /* Generate ramdom room code */
    public int generateRoomCode() {
        return 1111;
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

    public void logOut() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        this.finish();
    }
}
