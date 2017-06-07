package com.example.zhanga7141.mytestapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.LocaleDisplayNames;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private boolean isGpsEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BETWEEN_UPDATES = 1000 * 15;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 5.0f;
    private boolean isTracked = true;
    private Location myLocation;
    private static final float MY_LOC_ZOOM_FACTOR = 20.0f;
    private boolean dotColor = false;
    private int isTracking = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        //initial marker
        LatLng champaign = new LatLng(40.116421, -88.243385);
        mMap.addMarker(new MarkerOptions().position(champaign).title("Born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(champaign));
        Log.d("My Map", "home location works");

    }

    //changes the map type from "normal" to "satellite" view
    public void changeMapType(View v) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE) {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public void getLocation() {

        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //gps status
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGpsEnabled == true) {
                Log.d("MyMaps", "getLocation: GPS is enabled");
            }

            //network status
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled == true) {
                Log.d("MyMaps", "getLocation: Network is enabled");
            }

            if (!isGpsEnabled && !isNetworkEnabled) {
                Log.d("MyMaps", "getLocation: No Provider is enabled");

            } else {
                canGetLocation = true;
                if (isGpsEnabled == true) {
                    Log.d("MyMaps", "getLocation: GPS enabled & requesting location updates");
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    Log.d("MyMaps", "Permissions granted");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                    Log.d("MyMaps", "getLocation: GPS update request is happening");
                    Toast.makeText(this, "Currently Using GPS", Toast.LENGTH_SHORT).show();
                }
                if (isNetworkEnabled == true) {
                    Log.d("MyMaps", "getLocation: Network enabled & requesting location updates");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    Log.d("MyMaps", "getLocation: Network update request is happening");
                    Toast.makeText(this, "Currently Using Network", Toast.LENGTH_SHORT).show();

                }

            }
        } catch (Exception e) {
            Log.d("MyMaps", "Caught an exception in getLocation");
            e.printStackTrace();
        }

    }

    public void track(View v) {
        isTracking++;


        if (isTracking % 2 == 1) {
            Log.d("MyMaps", "Tracking on");
            Toast.makeText(MapsActivity.this, "Tracking on", Toast.LENGTH_SHORT).show();
            getLocation();
        }

        else {
            Log.d("MyMaps", "Tracking off");
            Toast.makeText(MapsActivity.this, "Tracking off", Toast.LENGTH_SHORT).show();

            if (ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d("MyMaps", "Permission check failed");
                Toast.makeText(MapsActivity.this, "Permission check failed", Toast.LENGTH_SHORT);
                return;
            }
            locationManager.removeUpdates(locationListenerNetwork);
            locationManager.removeUpdates(locationListenerGPS);
            Log.d("MyMaps", "track: remove updates");
            Toast.makeText(MapsActivity.this, "track: remove updates", Toast.LENGTH_SHORT);

        }

    }

    public void searchPlaces(View view) {
        EditText locationSearch = (EditText) findViewById(R.id.searchField);
        String location = locationSearch.getText().toString();
        List<Address> addressList = new ArrayList<>();
        List<Address> distanceList = new ArrayList<>();

        //check for empty search
        if (location.equals("")) {
            Toast.makeText(MapsActivity.this, "Empty Search", Toast.LENGTH_SHORT).show();
            return;
        } else if (location != null || !location.equals("")) {
            Log.d("MyMaps", "Starting Search");
            Geocoder geocoder = new Geocoder(this);
            try {
                //sets a 10 list search result
                addressList = geocoder.getFromLocationName(location, 10);
                Log.d("MyMaps", "10 max search result");
            } catch (IOException e) {
                e.printStackTrace();
            }

            //calculates radius for every location add adds the ones that are 5 or under
            for (int i = 0; i < addressList.size(); i++) {
                Log.d("mymaps", "currently calculating distances");
                Address currentAddress = addressList.get(i);

                double earthRadius = 3958.75; // miles (or 6371.0 kilometers)
                double dLat = Math.toRadians(currentAddress.getLatitude()-myLocation.getLatitude());
                double dLng = Math.toRadians(currentAddress.getLongitude()-myLocation.getLongitude());
                double sindLat = Math.sin(dLat / 2);
                double sindLng = Math.sin(dLng / 2);
                double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                        * Math.cos(Math.toRadians(myLocation.getLatitude())) * Math.cos(Math.toRadians(currentAddress.getLatitude()));
                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
                double dist = earthRadius * c;

                //adds 5 mile radius
                Log.d("mymaps","checking to see if radius is less than 5");
                if (dist <= 5) {
                    distanceList.add(addressList.get(i));
                    Log.d("MyMaps", "radius is less than 5 and added it to distanceList");
                } else {
                    Log.d("mymaps","distance is not less than 5");
                }
            }

            if (distanceList.size() == 0) {
                Log.d("MyMaps", "no search results found");
                Toast.makeText(MapsActivity.this, "No search results within 5 miles", Toast.LENGTH_SHORT).show();
            }

            //adds marker to every location 5 miles or less away
            for (int i = 0; i < distanceList.size(); i++) {

                Address address = distanceList.get(i);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                Log.d("Mymaps", "currently adding markers");
                mMap.addMarker(new MarkerOptions().position(latLng).title("Search Results"));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }

        }
    }

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "GPS Location has changed");
            Toast.makeText(MapsActivity.this, "GPS Location has changed", Toast.LENGTH_SHORT).show();

            //drops a marker on the map
            dropMarker(LocationManager.GPS_PROVIDER);
            Log.d("MyMaps", "called dropmarker() method from GPS");

            // disable network updates
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }
            locationManager.removeUpdates(locationListenerNetwork);
            dotColor = true;

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

            switch (status) {
                case LocationProvider.AVAILABLE:

                    Log.d("MyMaps", "LocationProvider is available");
                    break;
                case LocationProvider.OUT_OF_SERVICE:

                    if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);

                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;
                default:
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BETWEEN_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    break;

            }

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("MyMaps", "Network Location has changed");
            Toast.makeText(MapsActivity.this, "Network Location has changed", Toast.LENGTH_SHORT).show();

            //drops a marker on map
            dropMarker(LocationManager.NETWORK_PROVIDER);
            Log.d("MyMaps", "called dropmarker() method from network");

            dotColor = false;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMaps", "Network onStatusChanged called");
            Toast.makeText(MapsActivity.this, "Network onStatusChanged called", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public void dropMarker(String provider) {

        LatLng userLocation = null;

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);
        }

        if (myLocation == null) {

            Log.d("MyMaps", "dropMarker: myLocation is null");

        } else {
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            //toast of coordinates
            Toast.makeText(MapsActivity.this, "" + myLocation.getLatitude() + ", " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();

            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);

            Circle circle;

            if (dotColor == true) {
                circle = mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.BLUE));
                Log.d("MyMaps", "GPS-BLUE");
            } else if (dotColor == false) {
                circle = mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.RED));
                Log.d("MyMaps", "NETWORK-RED");
            }

            mMap.animateCamera(update);
        }

    }

    public void clearAll(View v)
    {
        mMap.clear();
    }




}
