package com.caravan.senior_project.caravan_android;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationListener;
import com.mapbox.mapboxsdk.location.LocationServices;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.Constants;
import com.mapbox.services.android.geocoder.ui.GeocoderAutoCompleteView;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.models.CarmenFeature;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Directions extends AppCompatActivity {

    private static final String TAG = "DirectionsActivity";

    private MapView mapView;
    private MapboxMap map;
    private LocationServices locationServices;
    private DirectionsRoute currentRoute;

    private static final int PERMISSIONS_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_directions);

        Location currentLoc = new Location("dummyProvider");
        Location destLoc = new Location("dummyProvider");
        Bundle locations = this.getIntent().getBundleExtra("locations");
        if (locations != null) {
            currentLoc = locations.getParcelable("currentLoc");
            destLoc = locations.getParcelable("nextLoc");
        }

        final Position origin = Position.fromCoordinates(currentLoc.getLongitude(),
                                                                    currentLoc.getLatitude());
        final Position destination = Position.fromCoordinates(destLoc.getLongitude(),
                                                                    destLoc.getLatitude());
        locationServices = LocationServices.getLocationServices(Directions.this);

        final LatLng centroid = new LatLng(
            (origin.getLatitude() + destination.getLatitude()) / 2,
                (origin.getLongitude() + destination.getLongitude()) / 2
        );

        Button current_location_button = (Button) findViewById(R.id.current_location_button);

        // Create a mapView
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        // Add a MapboxMap
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                CameraPosition position = new CameraPosition.Builder()
                        .target(new LatLng(centroid.getLatitude(), centroid.getLongitude()))
                        .build();
                Log.d("**Directions camera**", "The camera's position is " + centroid.getLatitude() + ","
                        + centroid.getLongitude());
                map.moveCamera(CameraUpdateFactory
                        .newCameraPosition(position));

                mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(origin.getLatitude(), origin.getLongitude()))
                .title("Origin")
                .snippet("Cal Poly"));
                mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(destination.getLatitude(), destination.getLongitude()))
                .title("Destination")
                .snippet("Boisen"));

                try {
                    getRoute(origin, destination);
                } catch (ServicesException servicesException) {
                    servicesException.printStackTrace();
                }

            }

        });

        GeocoderAutoCompleteView autocomplete = (GeocoderAutoCompleteView) findViewById(R.id.query);
        autocomplete.setAccessToken(MapboxAccountManager.getInstance().getAccessToken());
        autocomplete.setType(GeocodingCriteria.TYPE_POI);
        autocomplete.setOnFeatureListener(new GeocoderAutoCompleteView.OnFeatureListener() {
            @Override
            public void OnFeatureClick(CarmenFeature feature) {
                Position position = feature.asPosition();
                updateMap(position.getLatitude(), position.getLongitude());
            }
        });

    }

    private void getRoute(Position origin, Position destination) throws ServicesException {

        MapboxDirections client = new MapboxDirections.Builder()
                .setOrigin(origin)
                .setDestination(destination)
                .setProfile(DirectionsCriteria.PROFILE_CYCLING)
                .setAccessToken(MapboxAccountManager.getInstance().getAccessToken())
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                Log.d(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user");
                    return;
                } else if (response.body().getRoutes().size() < 1) {
                    Log.e(TAG, "No roughts found");
                    return;
                }

                currentRoute = response.body().getRoutes().get(0);
                Log.d(TAG, "Distance: " + currentRoute.getDistance());
                Toast.makeText(
                        Directions.this,
                        "Route is " + currentRoute.getDistance() + " meters long.",
                        Toast.LENGTH_SHORT).show();

                drawRoute(currentRoute);
            }

            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Log.e(TAG, "Error: " + throwable.getMessage());
                Toast.makeText(Directions.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoute(DirectionsRoute route) {
        LineString lineString  = LineString.fromPolyline(route.getGeometry(), Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        map.addPolyline(new PolylineOptions()
        .add(points)
        .color(Color.parseColor("#009688"))
        .width(5));
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
        Log.d("CurrentLocationButton", "You pressed the button and it's supposed to do something!");
        getLocation();
    }

    private void getLocation() {
        if (!locationServices.areLocationPermissionsGranted()) {
            // If permissions are not granted yet, request them.
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_LOCATION);
            Log.d("LocationNotGranted", "Permission has just been granted");
        } else {
            // Else, get the last known location.
            Location lastLocation = locationServices.getLastLocation();
            Log.d("PermissionGrantedAlrdy", "Permission has been granted already.");
            if (lastLocation != null) {
                Log.d("LastLocation", "Last Location is not null.");
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
                locationServices.addLocationListener(new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            Log.d("Location", "There's a new location.");
                            // Move map to where the user location is.
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
                            locationServices.removeLocationListener(this);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }
}
