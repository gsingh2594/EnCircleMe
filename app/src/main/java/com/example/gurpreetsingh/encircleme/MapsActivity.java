package com.example.gurpreetsingh.encircleme;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnPoiClickListener {

    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = null;
    private static final int EDIT_REQUEST = 1;
    private static final int REQUEST_SELECT_PLACE = 1000;
    private PlaceAutocompleteFragment mAutoCompleteFragment;
    public static final String TAG = "SampleActivityBase";
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private static final int MY_PERMISSION_FINE_LOCATION = 101;
    private TextView mPlaceDetailsText;
    private TextView mPlaceAttribution;
    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    Button btnAlerts;
    Button btnMaps;
    Button btnProfile;
    Button friends;
    Button btnSetting;
    ImageButton btnSearch;

    private TextView textFavorites;
    private TextView textSchedules;
    private TextView textMusic;

    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("path/to/geofire");
    GeoFire geoFire = new GeoFire(ref);


    //Button
    public void Profile() {
        btnProfile = (Button) findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent time = new Intent(MapsActivity.this, UserProfileActivity.class);
                startActivity(time);
            }
        });
    }

    public void Alerts() {
        btnAlerts = (Button) findViewById(R.id.btnAlerts);
        btnAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alerts = new Intent(MapsActivity.this, PlacePickerActivity.class);
                startActivity(alerts);
            }
        });
    }

    public void Maps() {
        btnMaps = (Button) findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alerts = new Intent(MapsActivity.this, MapsActivity.class);
                startActivity(alerts);
            }
        });
    }

    public void Friends() {
        friends = (Button) findViewById(R.id.friends);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent offMaps = new Intent(MapsActivity.this, FriendsActivity.class);
                startActivity(offMaps);
            }
        });
    }

    public void Settings() {
        btnSetting = (Button) findViewById(R.id.setting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent setting = new Intent(MapsActivity.this, UserActivity.class);
                startActivity(setting);
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);
        mPlaceDetailsText = (TextView) findViewById(R.id.place_details);
        mPlaceAttribution = (TextView) findViewById(R.id.place_attribution);

        Alerts();
        Maps();
        Friends();
        Profile();
        Settings();
        //Search();

    }
        /*mAutoCompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        mAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName());

                String placeDetailsStr = place.getName() + "\n"
                        + place.getId() + "\n"
                        + place.getLatLng().toString() + "\n"
                        + place.getAddress() + "\n"
                        + place.getAttributions() + "\n"
                        + place.getPhoneNumber() + "\n"
                        + place.getWebsiteUri() + "\n"
                        + place.getRating();
                mAutoCompleteFragment.setText(placeDetailsStr);

                String placeName = (String) place.getName();
                String placeAddress = (String) place.getAddress();
                String placeNumber = (String) place.getPhoneNumber();
                String placeUri = place.getWebsiteUri().toString();
                String placeRating = Float.toString(place.getRating());
                LatLng latLng = place.getLatLng();
                mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(placeName).snippet(placeRating + " " + placeUri*//*placeAddress + " Phone:" + placeNumber*//*));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });*/




    /*private void openAutocompleteActivity() {
        try {
            // The autocomplete activity requires Google Play Services to be available. The intent
            // builder checks this and throws an exception if it is not the case.
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(this);
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
        } catch (GooglePlayServicesRepairableException e) {
            // Indicates that Google Play Services is either not installed or not up to date. Prompt
            // the user to correct the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(this, e.getConnectionStatusCode(),
                    0 *//* requestCode *//*).show();
        } catch (GooglePlayServicesNotAvailableException e) {
            // Indicates that Google Play Services is not available and the problem is not easily
            // resolvable.
            String message = "Google Play Services is not available: " +
                    GoogleApiAvailability.getInstance().getErrorString(e.errorCode);

            Log.e(TAG, message);
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_SELECT_PLACE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);

                String placeDetailsStr = place.getName() + "\n"
                        + place.getId() + "\n"
                        + place.getLatLng().toString() + "\n"
                        + place.getAddress() + "\n"
                        + place.getAttributions();
                //mAutoCompleteFragment.setText(placeDetailsStr);*//*
                String placeName = (String) place.getName();
                String placeAddress = (String) place.getAddress();
                LatLng latLng = place.getLatLng();
                mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(placeName).snippet(placeAddress));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(17));
            }
        }
        else if (resultCode == Activity.RESULT_OK) {
            MarkerOptions markerOptions = data.getParcelableExtra("marker");
            mGoogleMap.addMarker((markerOptions));
        }
        /*else switch(requestCode) {
                case (EDIT_REQUEST) :*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_searchoverlay, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            // Method #3
            try {
                Intent intent = new PlaceAutocomplete.IntentBuilder
                        (PlaceAutocomplete.MODE_OVERLAY)
                        .setBoundsBias(BOUNDS_MOUNTAIN_VIEW)
                        .build(MapsActivity.this);
                startActivityForResult(intent, REQUEST_SELECT_PLACE);
            } catch (GooglePlayServicesRepairableException |
                    GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
            return true;
        }
        if (id == R.id.settings){
            Intent modifySettings=new Intent(MapsActivity.this,UserActivity.class);
            startActivity(modifySettings);
        }
        if (id == R.id.logout) {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Would you like to logout?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            startActivity(new Intent(MapsActivity.this, MainActivity.class));
                            //finish();
                        }
                    }).create().show();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.setOnPoiClickListener(this);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(true);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                Intent edit = new Intent(MapsActivity.this, EditActivity.class);
                edit.putExtra("location", latLng);
                MapsActivity.this.startActivityForResult(edit, EDIT_REQUEST);

/*                Circle circle = mGoogleMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(300)
                        .strokeColor(Color.BLUE)
                        .fillColor(Color.TRANSPARENT));*/

            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
/**
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);
*/
        //move map camera
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //optionally, stop location updates if only current location is needed
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onPoiClick(PointOfInterest poi) {
        Toast.makeText(getApplicationContext(), poi.name + //"\nPlace ID:" + poi.placeId +
                        "\nLatitude:" + poi.latLng.latitude +
                        " Longitude:" + poi.latLng.longitude,
                Toast.LENGTH_LONG).show();
    }

    public void onBackPressed() {
        moveTaskToBack(true);

    /*//put the AlertDialog code here
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Would you like to logout?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        startActivity(new Intent(MapsActivity.this, MainActivity.class));
                       //finish();
                    }
                }).create().show();*/
    }

   /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (EDIT_REQUEST) : {
                if (resultCode == Activity.RESULT_OK) {
                    MarkerOptions markerOptions = data.getParcelableExtra("marker");
                    mGoogleMap.addMarker(markerOptions);
                }
                break;
            }
        }
    }
*/
}


