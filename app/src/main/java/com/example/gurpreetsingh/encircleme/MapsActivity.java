package com.example.gurpreetsingh.encircleme;

/*This product includes software developed by
        Iiro Krankka (https://github.com/roughike)*/

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.Calendar;
import java.util.HashMap;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnPoiClickListener, GoogleMap.InfoWindowAdapter,
        GoogleMap.OnInfoWindowClickListener {

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
    private LatLng userLocation;
    private boolean locationInitialized;

    private HashMap<String, Event> eventsInfoMap;
    private HashMap<String, Bitmap> creatorProfileImagesMap;
    private HashMap<String, Place> placeInfoBottomSheet;

    private static final long ONE_MEGABYTE = 1024*1024;

    private BottomSheetBehavior bottomSheetBehavior;
    private View bottomSheet;
    private TextView textFavorites;
    private TextView textSchedules;
    private TextView textMusic;
    private BottomBar bottomBar;

    public static Intent notificationService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = (Toolbar) findViewById(R.id.map_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        setTitle("Nearby Events");

        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setPeekHeight(250);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);


        notificationService = new Intent(getApplicationContext(), FirebaseNotificationService.class);
        startService(notificationService);

        locationInitialized = false;
        buildGoogleApiClient();

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_map);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_profile) {
                    Intent profile = new Intent(getApplicationContext(), UserProfileActivity.class);
                    startActivity(profile);
                } else if (tabId == R.id.tab_friends) {
                    Intent friends = new Intent(getApplicationContext(), FriendsActivity.class);
                    startActivity(friends);
                /*} else if (tabId == R.id.tab_map) {
                    Intent map = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(map);*/
                } else if (tabId == R.id.tab_alerts) {
                    Intent events = new Intent(getApplicationContext(), EventsTabActivity.class);
                    startActivity(events);
                } else if (tabId == R.id.tab_chats) {
                    Intent events = new Intent(getApplicationContext(), ChatActivity.class);
                    startActivity(events);
                }
            }
        });
        //HashMaps for storing info used in marker info window
        eventsInfoMap = new HashMap<String, Event>();
        creatorProfileImagesMap = new HashMap<String, Bitmap>();
        placeInfoBottomSheet = new HashMap<>();
    }

    private void loadEventsFromDB(){
        DatabaseReference eventLocationsRef = FirebaseDatabase.getInstance().getReference("events/geofire_locations");
        GeoFire eventsGeoFireRef = new GeoFire(eventLocationsRef);

        // GeoQuery to retrieve events within a specified distance of the user's current location
        GeoQuery eventsGeoQuery = eventsGeoFireRef.queryAtLocation(new GeoLocation(userLocation.latitude, userLocation.longitude), 200); // 200 km == 124 miles
        Log.d("loadEventsFromDB()", "starting GeoQuery");
        eventsGeoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                final String eventKey = key;
                final GeoLocation eventLocation = location;
                Log.d("onKeyEntered", "event found @" + eventLocation.toString() + "\nEvent Key: " + eventKey);

                // Check if event info has already been loaded
                Log.d("onKeyEntered", "eventsInfoMap.containsKey() = " + eventsInfoMap.containsKey(eventKey));
                if (!eventsInfoMap.containsKey(eventKey)) {
                    // Not loaded yet --> load event info from DB
                    Log.d("event not loaded", "loading event info");
                    DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events/all_events/" + eventKey);
                    eventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get Event object for event info
                            Event event = dataSnapshot.getValue(Event.class);

                            // Check if the event has already happened
                            Log.d("event already happened?", "checking");
                            boolean eventEnded = eventHasEnded(event);
                            boolean isFutureEvent;
                            if(!eventEnded){
                                // Event has not ended. Check if it has started
                                if(eventHasNotStarted(event)) {
                                    // Event is in the future
                                    isFutureEvent = true;
                                }else{
                                    // Event has started --> currently happening
                                    isFutureEvent = false;
                                }
                                // Store event in HashMap
                                eventsInfoMap.put(eventKey, event);
                                loadCreatorProfileImage(eventKey, eventLocation, isFutureEvent);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(MapsActivity.this, "Error loading event info", Toast.LENGTH_LONG).show();
                            Log.d("loadEventsFromDB", "Database error" + databaseError.getMessage());
                        }
                    });
                }
                else{
                    Log.d("onKeyEntered", "Event info has already been loaded, or the event already happened for eventKey = " + eventKey);
                }
            }

            @Override
            public void onKeyExited(String key) {
                Log.d("onKeyExited", "Event disappearing!");
                // Remove event from HashMap
                eventsInfoMap.remove(key);
                // Remove creator's profile image from HashMap
                creatorProfileImagesMap.remove(key);
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("onKeyMoved", "Somehow an event location has changed");
            }

            @Override
            public void onGeoQueryReady() {
                Log.d("onGeoQueryReady", "All event info has loaded. However, event creator profile pictures might not be loaded!");
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d("onGeoQueryError", error.getMessage() + error.getDetails());
                Toast.makeText(MapsActivity.this, "GeoQuery Error", Toast.LENGTH_LONG).show();
            }
        });
    }

    // returns true if event **end date** and time is before the current time, or false otherwise
    static boolean eventHasEnded(Event event){
        int endMonth, endDay, endYear;
        if(event.getEndDate() != null) {
            // End date exists --> use it
            String[] mdy = event.getEndDate().split("/");
            endMonth = Integer.parseInt(mdy[0]) -1; // decrement month by 1 because range of months is 0-11
            endDay = Integer.parseInt(mdy[1]);
            endYear = Integer.parseInt(mdy[2]);
        }
        else{
            // No end date given --> use start date
            String[] mdy = event.getDate().split("/");
            endMonth = Integer.parseInt(mdy[0]) -1; // decrement month by 1 because range of months is 0-11
            endDay = Integer.parseInt(mdy[1]);
            endYear = Integer.parseInt(mdy[2]);
        }

        String hourMin[] = event.getEndTime().split("[: ]");
        int endHour;
        if(hourMin[2].equals("pm"))
            if(Integer.parseInt(hourMin[0]) != 12)
                endHour = Integer.parseInt(hourMin[0])+ 12;
                // If pm and hour!=12, add 12 to the hour to convert to 24 hour format
            else
                endHour = Integer.parseInt(hourMin[0]);
        else
            endHour = Integer.parseInt(hourMin[0]); // Hour is in am

        int endMin = Integer.parseInt(hourMin[1]);

        /*for(String elem : hourMin)
            Log.d("hourMin", elem);*/
        Log.d("endYear", Integer.toString(endYear));
        Log.d("endMonth", Integer.toString(endMonth));
        Log.d("endDay", Integer.toString(endDay));
        Log.d("endHour", Integer.toString(endHour));
        Log.d("endMin", Integer.toString(endMin));

        Calendar endCalendar = Calendar.getInstance();
        Log.d("endCalendar", Long.toString(endCalendar.getTimeInMillis()));
        endCalendar.clear(); // Clear the fields in the calendar so the compareTo method works properly
        endCalendar.set(endYear, endMonth, endDay, endHour, endMin, 0);
        endCalendar.getTimeInMillis(); // get to recalculate time in millis
        Log.d("endCalendar", "after setting calendar: " + Long.toString(endCalendar.getTimeInMillis()));

        // Get current time
        Calendar currentCalendar = Calendar.getInstance();
        Log.d("currentCalendar", Long.toString(currentCalendar.getTimeInMillis()));
        // Compare current time to event end time
        int result = endCalendar.compareTo(currentCalendar);
        Log.d("eventHasEnded", "result= " + Integer.toString(result));
        if(result == -1) // End time is before current time
            return true; // --> Event has ended
        if(result == 1)  // End time is after end time
            return false;// --> Event has not ended
        else             // End time and current time are equal
            return true; // --> event has ended
    }


    // Returns true if event **start date** and time is before current date and time
    static boolean eventHasNotStarted(Event event){
        Calendar calendar = Calendar.getInstance();
        String[] mdy = event.getDate().split("/");
        int month = Integer.parseInt(mdy[0]) -1;
        int day = Integer.parseInt(mdy[1]);
        int year = Integer.parseInt(mdy[2]);
        Log.d("month", Integer.toString(month));
        Log.d("day", Integer.toString(day));
        Log.d("year", Integer.toString(year));

        String[] hourMin = event.getEndTime().split(":");
        int endHour = Integer.parseInt(hourMin[0]);
        Log.d("hourMin[0]", hourMin[0]);
        Log.d("hourMin[1]", hourMin[1]);
        String[] minuteAMPM = hourMin[1].split(" ");
        int endMinute = Integer.parseInt(minuteAMPM[0]);
        Log.d("minuteAMPM[0]", minuteAMPM[0]);
        Log.d("minuteAMPM[1]", minuteAMPM[1]);
        Log.d("endHour", Integer.toString(endHour));
        Log.d("endMinute", Integer.toString(endMinute));

        Calendar eventEndDate = Calendar.getInstance(); // calendar object for event end date & time
        eventEndDate.clear();      // Remove previous values so the set() method works properly
        eventEndDate.set(year, month, day, endHour, endMinute);

        // Compare current calendar to event end calendar
        Log.d("calendar.compareTo", Integer.toString(calendar.compareTo(eventEndDate)));
        if(calendar.compareTo(eventEndDate) < 0){
            // current date & time is before event end date & time
            return true;
        }
        else { // Event has already ended
            //Log.d("eventHasNotHappened", "eventKey " + eventKey + "has already happened ");
            return false;
        }
    }


    private void addLocationMarker(String key, GeoLocation location, boolean isFutureEvent) {
        // Create location marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(location.latitude, location.longitude));
        markerOptions.title(key);   // For retrieving associated event info later

        BitmapDrawable bitmapDraw;
        if(isFutureEvent)
            // Is a future event --> use green marker
            bitmapDraw =(BitmapDrawable)getResources().getDrawable(R.drawable.marker_encircleme, null);
        else
            // Is a current event --> use red marker
            bitmapDraw =(BitmapDrawable)getResources().getDrawable(R.drawable.ongoingmarker, null);

        // Get the marker bitmap and scale it
        Bitmap markerBM=bitmapDraw.getBitmap();
        Bitmap largerMarkerBM = Bitmap.createScaledBitmap(markerBM, convertDPtoPX(25), convertDPtoPX(40), false);

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(largerMarkerBM));
        //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.ongoingmarker)));

        mGoogleMap.addMarker(markerOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that the result was from the autocomplete widget.
        if (requestCode == REQUEST_SELECT_PLACE) {
            if (resultCode == RESULT_OK) {
                // Get the user's selected place from the Intent.
                Place place = PlaceAutocomplete.getPlace(this, data);
                placeInfoBottomSheet.put(place.getAddress().toString(), place);

                String placeDetailsStr = place.getName() + "\n"
                        + place.getLocale() + "\n"
                        + place.getId() + "\n"
                        + place.getLatLng().toString() + "\n"
                        + place.getAddress() + "\n"
                        + place.getAttributions() + "\n"
                        + place.getPhoneNumber() + "\n"
                        + place.getWebsiteUri() + "\n"
                        + place.getRating();

                //mAutoCompleteFragment.setText(placeDetailsStr);*//*
                String placeName = (String) place.getName();
                //String locale = (Locale) place.getLocale();
                String placeAddress = (String) place.getAddress();
                LatLng latLng = place.getLatLng();
                String placeNumber;
                if(place.getPhoneNumber() !=null)
                    placeNumber = (String) place.getPhoneNumber();
                String placeUri;
                if(place.getWebsiteUri() != null) // Prevent crash if no website URI exists
                    placeUri = place.getWebsiteUri().toString();
                String placeRating = Float.toString(place.getRating());
                mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(placeName).snippet(placeAddress)
                        .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.pin))));
                // .icon(BitmapDescriptorFactory.fromResource((person_pin))));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            }
        } else if (resultCode == Activity.RESULT_OK) {
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
        if (id == R.id.action_tab) {
            Intent modifyTab = new Intent(MapsActivity.this, EditActivity.class);
            //modifyTab.putExtra("location", userLocation);
            startActivity(modifyTab);
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
                            FirebaseAuth.getInstance().signOut();
                            stopService(notificationService);
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
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        //bottomBar.setDefaultTab(R.id.tab_map);
        // Resume location updates when user returns to the MapsActivity
        bottomBar.setDefaultTab(R.id.tab_map);
        if(mGoogleApiClient.isConnected()){
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
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
        mGoogleMap.setInfoWindowAdapter(this);
        mGoogleMap.setOnInfoWindowClickListener(this);
        MapsInitializer.initialize(this);
        addCustomMarker();

        Log.d("onMapReady()","checking location permissions");
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                Log.d("Location permissions", "already granted");
                //buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            //buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                Intent edit = new Intent(MapsActivity.this, EditActivity.class);
                edit.putExtra("location", latLng);
                MapsActivity.this.startActivityForResult(edit, EDIT_REQUEST);
            }
        });

        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (eventsInfoMap.get(marker.getTitle()) == null){
                    if(placeInfoBottomSheet.containsKey(marker.getSnippet()));
                    updateBottomSheetContent(placeInfoBottomSheet.get(marker.getSnippet().toString()));
                }
                return false;
            }
        });
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
    }

    private void updateBottomSheetContent(Place place) {
        TextView name = (TextView) bottomSheet.findViewById(R.id.detail_name);
        TextView address = (TextView) bottomSheet.findViewById(R.id.detail_address);
        TextView number = (TextView) bottomSheet.findViewById(R.id.detail_phone);
        TextView website = (TextView) bottomSheet.findViewById(R.id.detail_website);
        TextView rating = (TextView) bottomSheet.findViewById(R.id.detail_rating);
        //TextView price = (TextView) bottomSheet.findViewById(R.id.detail_price);
        //TextView type = (TextView) bottomSheet.findViewById(R.id.detail_placetype);

        name.setText(place.getName());
        address.setText(place.getAddress());
        if(place.getPhoneNumber() !=null)
            number.setText(place.getPhoneNumber());
        if (place.getWebsiteUri() != null)
            website.setText(place.getWebsiteUri().toString());
        if (place.getRating() != 0)
            rating.setText(String.valueOf(place.getRating()));
/*        if (place.getPriceLevel() != 0)
            price.setText(Integer.toString(place.getPriceLevel()));
/*
        if (place.getPlaceTypes() != null)
            type.setText(place.getPlaceTypes().toString());
*/
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void addCustomMarker() {
        Log.d(TAG, "addCustomMarker()");
        if (mGoogleMap == null) {
        }

        /*// adding a marker on map with image from  dradrawable
        mGoogleMap.addMarker(new MarkerOptions()
                .position(new LatLng(40.758879, -73.985110))
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.neutral_face_icon))));*/
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        Log.d("buildGoogleApiClient()", "connecting to google services");
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("onConnected()", "Connected to Google services");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setSmallestDisplacement(10); // no location updates unless user has moved 5 meters. Default is 0 meters
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
        Log.d("onLocationChanged()", "new location");
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        userLocation = latLng;
        //move map camera only the first time location is received
        if(!locationInitialized) {
            CameraUpdate centerAndZoom = CameraUpdateFactory.newLatLngZoom(userLocation, 13);
            //CameraUpdate center=CameraUpdateFactory.newLatLng(latLng);
            //CameraUpdate zoom=CameraUpdateFactory.zoomTo(13);
            //mGoogleMap.moveCamera(center);
            //mGoogleMap.animateCamera(zoom);
            mGoogleMap.animateCamera(centerAndZoom);
            locationInitialized = true;
        }
        // Reload events from DB based on new location
        loadEventsFromDB();
        /*
        //optionally, stop location updates if only current location is needed
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
        */
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
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        //return null;
        // Check if marker title is a key to an event
        if(eventsInfoMap.get(marker.getTitle()) != null)
            // Marker represents an event
            return prepareEventInfoView(marker);
        else
            // Marker represents a place
            return null;
        //preparePlaceInfoView(marker);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String eventKey = marker.getTitle();
        // Check if the marker represents a place or an event
        if(eventsInfoMap.containsKey(eventKey)) {
            // Marker is an event --> show full event info
            Intent showFullEventInfo = new Intent(MapsActivity.this, EventInfoActivity.class);
            showFullEventInfo.putExtra("eventKey", eventKey);
            startActivity(showFullEventInfo);
        }
    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {
        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.search_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.pin_image);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    private View prepareEventInfoView(Marker marker){
        // Get event info from HashMap
        String eventKey = marker.getTitle();
        Event event = eventsInfoMap.get(eventKey);

        //prepare InfoView programmatically
        LinearLayout infoView = new LinearLayout(MapsActivity.this);

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        imageParams.gravity=Gravity.CENTER_VERTICAL;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = convertDPtoPX(5);  // 5dp
        params.setMargins(margin, margin, margin, margin);

        infoView.setOrientation(LinearLayout.HORIZONTAL);
        infoView.setLayoutParams(params);

        // profile image view
        ImageView infoImageView = new ImageView(MapsActivity.this);
        Bitmap profileImage = creatorProfileImagesMap.get(eventKey);
        if(profileImage != null){
            // Event creator has profile image
            infoImageView.setImageBitmap(getClip(creatorProfileImagesMap.get(eventKey)));
        }
        else {
            // Event creator does not have profile image
            //Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);
            Drawable drawable = getResources().getDrawable(android.R.drawable.ic_dialog_map);
            infoImageView.setImageDrawable(drawable);
        }
        infoImageView.setLayoutParams(imageParams);
        infoView.addView(infoImageView);

        LinearLayout subInfoView = new LinearLayout(MapsActivity.this);
        subInfoView.setOrientation(LinearLayout.VERTICAL);
        params.gravity=Gravity.CENTER_VERTICAL;
        subInfoView.setLayoutParams(params);

        /*
        TextView subInfoLat = new TextView(MapsActivity.this);
        subInfoLat.setText("Lat: " + marker.getPosition().latitude);
        TextView subInfoLnt = new TextView(MapsActivity.this);
        subInfoLnt.setText("Lnt: " + marker.getPosition().longitude);
        subInfoView.addView(subInfoLat);
        subInfoView.addView(subInfoLnt);
        */
        TextView eventNameText = new TextView(MapsActivity.this);
        eventNameText.setText(event.getName());
        eventNameText.setPadding(convertDPtoPX(5), 0, 0, 0);
        eventNameText.setMaxWidth(convertDPtoPX(200));
        eventNameText.setTypeface(null, Typeface.BOLD);

        TextView eventDescriptionText = new TextView(MapsActivity.this);
        eventDescriptionText.setText(event.getAbout());
        eventDescriptionText.setMaxLines(2);
        eventDescriptionText.setEllipsize(TextUtils.TruncateAt.END);
        eventDescriptionText.setPadding(convertDPtoPX(5), 0, 0, 0);
        eventDescriptionText.setMaxWidth(convertDPtoPX(200));

        subInfoView.addView(eventNameText);
        subInfoView.addView(eventDescriptionText);
        infoView.addView(subInfoView);
        return infoView;
    }


    private View preparePlaceInfoView(Marker marker){
        LinearLayout infoView = new LinearLayout(MapsActivity.this);
        LinearLayout.LayoutParams infoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoView.setOrientation(LinearLayout.VERTICAL);
        infoView.setLayoutParams(infoViewParams);

        TextView placeTitleText = new TextView(MapsActivity.this);
        placeTitleText.setText(marker.getTitle());
        placeTitleText.setTypeface(null, Typeface.BOLD);

        TextView placeAddressText = new TextView(MapsActivity.this);
        placeAddressText.setText(marker.getSnippet());

        infoView.addView(placeTitleText);
        infoView.addView(placeAddressText);
        return infoView;
    }

    // Returns a bitmap as a cropped circle
    public static Bitmap getClip(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    /*@Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
    */

    private void loadCreatorProfileImage(String key, GeoLocation location, final boolean isFutureEvent){
        final String eventKey = key;
        final GeoLocation eventLocation = location;
        // Load event creator's userID from DB
        DatabaseReference eventKeyCreatorsRef = FirebaseDatabase.getInstance()
                .getReference("events/event_key_creators/" + eventKey);
        eventKeyCreatorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String creatorID =dataSnapshot.getValue().toString();
                    Log.d("creatorID", creatorID);

                    // Load creator's profile image
                    final StorageReference creatorProfileImageRef = FirebaseStorage.getInstance()
                            .getReference("profile_images/" + creatorID);
                    creatorProfileImageRef.getBytes(ONE_MEGABYTE * 5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            // Profile image returned
                            Log.d("Creator's profile image", "image returned from storage");
                            Bitmap profileImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            // Scale profile image
                            Bitmap scaledProfileImage = profileImage.createScaledBitmap(profileImage, convertDPtoPX(60), convertDPtoPX(60), false);
                            creatorProfileImagesMap.put(eventKey, scaledProfileImage);
                            // Create location marker
                            addLocationMarker(eventKey, eventLocation, isFutureEvent);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Error or no profile image
                            Log.d("Creator's profile image", "no image found");
                            creatorProfileImagesMap.put(eventKey, null);    // null to indicate no profile image
                            // Create location marker
                            addLocationMarker(eventKey, eventLocation, isFutureEvent);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // used to set sizes in dp units programmatically. (Some views set sizes programmtically in px, not dp)
    // We should use this method to make certain views display consistently on different screen densities
    private int convertDPtoPX(int sizeInDP){
        float scale = getResources().getDisplayMetrics().density;       // note that 1dp = 1px on a 160dpi screen
        int dpAsPixels = (int) (sizeInDP * scale + 0.5f);
        return dpAsPixels;  // return the size in pixels
    }

    private int convertDPtoPX(double sizeInDP){
        float scale = getResources().getDisplayMetrics().density;       // note that 1dp = 1px on a 160dpi screen
        double dpAsPixels = Math.floor(sizeInDP * scale + 0.5f);
        return (int)dpAsPixels;  // return the size in pixels
    }

}


