package com.example.gurpreetsingh.encircleme;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by GurpreetSingh on 4/18/17.
 */

public class EventInfoActivity extends AppCompatActivity implements OnMapReadyCallback{

    private String eventKey;
    private Event event;
    private GoogleMap googleMap;
    private TextView txtEventName, txtEventStartDate, txtEventTime, txtEventLocation, txtEventDescription, txtEventCreator;
    private ImageView creatorProfileImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("onCreate", "method started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.event_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        txtEventName = (TextView) findViewById(R.id.event_name);
        txtEventStartDate = (TextView) findViewById(R.id.start_date);
        txtEventTime = (TextView) findViewById(R.id.time);
        txtEventLocation = (TextView) findViewById(R.id.location);
        txtEventDescription = (TextView) findViewById(R.id.event_description);
        txtEventCreator = (TextView) findViewById(R.id.creator_name);
        creatorProfileImage = (ImageView) findViewById(R.id.creator_profile_image);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_snippet);
        // Configure MapFragment options and get a new instance
        GoogleMapOptions googleMapOptions =  new GoogleMapOptions().liteMode(true);
        mapFragment.newInstance(googleMapOptions);
        mapFragment.getMapAsync(this);

        eventKey = getIntent().getStringExtra("eventKey");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d("onNewIntent", "method started");
        super.onNewIntent(intent);
        Log.d("onNewIntent", "getting extra");
        eventKey = intent.getStringExtra("eventKey");
        loadEventInfoFromDB();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        // Load event info after map is ready.
        // If event loads before the map, the event marker would be added to a nonexisting map
        loadEventInfoFromDB();
    }


    private void loadEventInfoFromDB(){
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events/all_events/" + eventKey);
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                event = dataSnapshot.getValue(Event.class);
                Log.d("event", event.toString());
                displayEventInfo();
                showEventInMap();
                loadEventCreatorName();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("onCancelled", "Database Error: " + databaseError.getMessage());
                Toast.makeText(EventInfoActivity.this, "Could not load event", Toast.LENGTH_LONG).show();
            }
        });
    }


    // Initializes textviews inside layout to show the event info
    private void displayEventInfo(){
        String eventName = event.getName();
        String eventDescription = event.getAbout();
        String eventStartDate = event.getDate();
        String eventStartTime = event.getStartTime();
        String eventEndDate = event.getEndDate();
        String eventEndTime = event.getEndTime();
        String eventPlaceID = event.getPlaceID();
        String eventPlaceName = event.getPlaceName();
        double eventLatitude = event.getLatitude();
        double eventLongitude = event.getLongitude();

        Log.d("eventName", eventName);
        Log.d("eventDescription", eventDescription);
        Log.d("eventStartDate", eventStartDate);
        Log.d("eventStartTime", eventStartTime);
        if(eventEndDate!=null)
            Log.d("eventEndDate", eventEndDate);
        Log.d("eventEndTime", eventEndTime);
        if(eventPlaceID!=null)
            Log.d("eventPlaceID", eventPlaceID);
        Log.d("eventLatitude", Double.toString(eventLatitude));
        Log.d("eventLongitude", Double.toString(eventLongitude));

        // Set text in corresponding TextViews
        txtEventName.setText(event.getName());
        txtEventStartDate.setText(event.getDate());

        if(event.getEndDate()==null)
            // Display without an end date
            txtEventTime.setText("From " + event.getStartTime() + " to " + event.getEndTime());
        else
            // Display with end date
            txtEventTime.setText(event.getStartTime() + " to " + event.getEndDate() + " " + event.getEndTime());

        if(eventPlaceName!=null)
            txtEventLocation.setText(eventPlaceName);
        else
            txtEventLocation.setText("Lat: " + eventLatitude + "\nLng: " + eventLongitude);
        txtEventDescription.setText(event.getAbout());
    }


    // Displays a marker in the map fragment for locating the event
    private void showEventInMap(){
        LatLng eventLatLng = new LatLng(event.getLatitude(), event.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(eventLatLng)
                .title(event.getName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        googleMap.addMarker(markerOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(eventLatLng, 12));
    }


    // Loads the user's name who created the event and displays it
    private void loadEventCreatorName(){
        DatabaseReference eventCreatorsRef = FirebaseDatabase.getInstance().getReference("events/event_key_creators");
        // Get the user ID of the person that created this event
        eventCreatorsRef.child(eventKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final String eventCreatorID = dataSnapshot.getValue().toString();

                // Load the user's profile to get their name
                DatabaseReference userProfileRef = FirebaseDatabase.getInstance().getReference("users/" + eventCreatorID);
                userProfileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        String name = user.getName();
                        // Display the user's name
                        txtEventCreator.setText(name);
                        // Load the user's profile image
                        loadEventCreatorImage(eventCreatorID);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // Loads the event creator's profile image and displays it
    public void loadEventCreatorImage(String eventCreatorID){
        StorageReference creatorProfileImageRef = FirebaseStorage.getInstance().getReference("profile_images/" + eventCreatorID);
        creatorProfileImageRef.getBytes(1024*1024*5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                byte[] profileImageBytes;
                // user has profile pic --> display it
                Log.d("loadEventCreatorImage()", "getBytes successful");
                profileImageBytes = bytes;
                Log.d("loadEventCreatorImage()", "convert bytes to bitmap");
                Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.length);
                // Scale the image and display it
                creatorProfileImage.setImageBitmap(Bitmap.createScaledBitmap
                        (MapsActivity.getClip(profileImageBitmap), convertDPtoPX(40), convertDPtoPX(40), false));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // User has not set profile image or storage error
                Log.d("loadEventCreatorImage()", "Firebase storage exception " + e.getMessage());
                // Toast.makeText(EventInfoActivity.this, "No profile image", Toast.LENGTH_LONG).show();
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
}
