package com.example.gurpreetsingh.encircleme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

/**
 * Created by GurpreetSingh on 4/18/17.
 */

public class EventInfoActivity extends AppCompatActivity implements OnMapReadyCallback{

    private static final int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<ChatMessageEvent> adapter;
    private String username;
    private String name;
    private boolean usernameIsLoaded = false;

    private String eventKey;
    private String userID;
    private Event event;

    private GoogleMap googleMap;
    private TextView txtEventName, txtEventStartDate, txtEventTime, txtEventLocation, txtEventDescription, txtEventCreator;
    private ImageView creatorProfileImage;
    private ScrollView childScroll;
    private ScrollView parentScroll;

    private Button btnEnCircleMe, btnEnCircleFriend, btnUnCircleMe, btnDeleteEvent;
    private Menu menu;
    private boolean userIsEventCreator = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("onCreate", "method started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.event_toolbar);
        setSupportActionBar(toolbar);
        setTitle("Event Info");
        ActionBar actionBar = getSupportActionBar();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.eventinfo_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.event_chat) {
            Intent modifyChat = new Intent(EventInfoActivity.this, EventInfoChatActivity.class);
            modifyChat.putExtra("eventKey", eventKey);
            startActivity(modifyChat);
        }
        if (id == R.id.delete_event) {
            //Intent modifyDelete=new Intent(EventInfoActivity.this,UserActivity.class);
            new AlertDialog.Builder(this)
                    .setTitle("Delete Event")
                    .setMessage("Would you like delete this event? This action cannot be undone.")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            startActivity(new Intent(EventInfoActivity.this, EventsTabActivity.class));
                            //finish();
                        }
                    }).create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        txtEventName = (TextView) findViewById(R.id.event_name);
        txtEventStartDate = (TextView)  findViewById(R.id.start_date);
        txtEventTime = (TextView)  findViewById(R.id.time);
        txtEventLocation = (TextView)  findViewById(R.id.location);
        txtEventDescription = (TextView)  findViewById(R.id.event_description);
        txtEventCreator = (TextView)  findViewById(R.id.creator_name);
        creatorProfileImage = (ImageView)  findViewById(R.id.creator_profile_image);

        btnEnCircleMe = (Button)  findViewById(R.id.encircle_event);
        btnEnCircleFriend = (Button)  findViewById(R.id.encircle_friends);
        btnUnCircleMe = (Button)  findViewById(R.id.uncircle_event);
        btnDeleteEvent = (Button)findViewById(R.id.delete_button);

        btnDeleteEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(EventInfoActivity.this)
                        .setTitle("Delete Event")
                        .setMessage("Would you like delete this event? This action cannot be undone.")
                        .setNegativeButton("No", null)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                startActivity(new Intent(EventInfoActivity.this, EventsTabActivity.class));
                                //finish();
                            }
                        }).create().show();
            }
        });


        btnEnCircleMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enCircleUserInDB();
            }
        });

        btnEnCircleFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent enCircleFriends = new Intent(EventInfoActivity.this, EnCircleFriendsActivity.class);
                enCircleFriends.putExtra("eventKey", eventKey);
                startActivity(enCircleFriends);
            }
        });

        btnUnCircleMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unCircleUserInDB();
            }
        });

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eventKey = getIntent().getStringExtra("eventKey");

        checkIfUserIsEventCreator();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_snippet);
        // Configure MapFragment options and get a new instance
        GoogleMapOptions googleMapOptions =  new GoogleMapOptions().liteMode(true);
        mapFragment.newInstance(googleMapOptions);
        mapFragment.getMapAsync(this);
    }

    // Checks in DB to see if the current user created the event being viewed
    private void checkIfUserIsEventCreator(){
        Log.d("checkIfUserIsEvent", "entering");
        DatabaseReference eventCreatorsRef = FirebaseDatabase.getInstance().getReference("events/event_key_creators");
        eventCreatorsRef.child(eventKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            Log.d("datasnapshot value", dataSnapshot.getValue().toString());
                            if (dataSnapshot.getValue().toString().equals(userID)) {
                                // User created the event --> show them as the event creator
                                Log.d("eventCreator", "user created this event");
                                showAsEventCreator();
                            } else {
                                // User did not make the event --> Check if they are encircled in the event
                                Log.d("eventCreator", "user did not create this event");
                                checkIfEnCircled();
                            }
                        }
                        else{
                            // User did not make the event --> Check if they are encircled in the event
                            Log.d("eventCreator", "user did not create this event");
                            checkIfEnCircled();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    // Checks to see whether the user has already EnCircled the event in the DB
    private void checkIfEnCircled(){
        Log.d("checking if encircled", "");
        // Check if user is already encircled for the event
        DatabaseReference userEncircledEventsRef = FirebaseDatabase.getInstance().getReference("events/user_encircled_events/" + userID);
        userEncircledEventsRef.orderByKey().equalTo(eventKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            // user is encircled to the event. Show un-encircleMe button
                            Log.d("userIsEncircled", "EnCircled already");
                            showAsUnCircleMe();
                        }
                        else {
                            // user is not encircled. Show en-circleMe button
                            Log.d("userIsEncircled", "EnCircled already");
                            showAsEnCircleMe();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("checkIfEncircled", "DB error: " + databaseError.getMessage());
                    }
                });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
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
                if(MapsActivity.eventHasEnded(event))
                    disableEnCircleFriends();
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
        String eventAddress = event.getAddress();
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
        if(eventAddress!=null)
            Log.d("eventAddress", eventAddress);
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


        // Display event place name and location
        if(eventPlaceName!=null) {
            // Event has a selected place
            if(eventAddress!=null)
                txtEventLocation.setText(eventPlaceName + "\n" + eventAddress);
            else
                txtEventLocation.setText(eventPlaceName);// This line shouldn't run because a place is stored with its address
        }
        else {
            // Event does not have a selected place
            if(eventAddress!=null)
                txtEventLocation.setText(eventAddress);
            else
                // No place name and no address --> resort to displaying lat & lng.
                // (Likely will never execute because Geocoder always finds an "address" to save)
                txtEventLocation.setText("Lat: " + eventLatitude + "\nLng: " + eventLongitude);
        }

        txtEventDescription.setText(event.getAbout());
    }


    // Displays a marker in the map fragment for locating the event
    private void showEventInMap(){
        LatLng eventLatLng = new LatLng(event.getLatitude(), event.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(eventLatLng)
                .title(event.getName())
                .snippet(event.getAddress());
                //.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker_encircleme)));

        boolean isCurrentEvent = !MapsActivity.eventHasNotStarted(event) && !MapsActivity.eventHasEnded(event);
        BitmapDrawable bitmapDraw;
        if(isCurrentEvent)
            // Is a future event --> use green marker
            bitmapDraw =(BitmapDrawable)getResources().getDrawable(R.drawable.ongoingmarker, null);
        else
            // Is a current event --> use red marker
            bitmapDraw =(BitmapDrawable)getResources().getDrawable(R.drawable.marker_encircleme, null);

        // Get the marker bitmap and scale it
        Bitmap markerBM=bitmapDraw.getBitmap();
        Bitmap largerMarkerBM = Bitmap.createScaledBitmap(markerBM, convertDPtoPX(25), convertDPtoPX(40), false);

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(largerMarkerBM));

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
                        (UserProfileListAdapter.getClip(profileImageBitmap), convertDPtoPX(40), convertDPtoPX(40), false));
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


    // Saves the user in user_encricled_events and event_attendees in DB
    private void enCircleUserInDB(){
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");
        HashMap<String, Object> eventUpdates = new HashMap<>();
        eventUpdates.put("user_encircled_events/" + userID + "/" + eventKey, true);
        eventUpdates.put("event_attendees/" + eventKey + "/" + userID, true);

        eventsRef.updateChildren(eventUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(EventInfoActivity.this, "You are EnCircled!", Toast.LENGTH_SHORT).show();
                showAsUnCircleMe();
            }
        });
    }

    // Saves the user in user_encricled_events and event_attendees in DB
    private void unCircleUserInDB(){
        DatabaseReference eventsRef = FirebaseDatabase.getInstance().getReference("events");
        HashMap<String, Object> eventUpdates = new HashMap<>();
        eventUpdates.put("user_encircled_events/" + userID + "/" + eventKey, null);
        eventUpdates.put("event_attendees/" + eventKey + "/" + userID, null);

        eventsRef.updateChildren(eventUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(EventInfoActivity.this, "You are UnCircled!", Toast.LENGTH_SHORT).show();
                showAsEnCircleMe();
            }
        });
    }


    private void showAsEnCircleMe(){
        Log.d("showAsEnCircleMe", "entering");
        btnEnCircleMe.setVisibility(View.VISIBLE);
        btnUnCircleMe.setVisibility(View.GONE);
    }

    private void showAsUnCircleMe(){
        Log.d("showAsUnCircleMe", "entering");
        btnEnCircleMe.setVisibility(View.GONE);
        btnUnCircleMe.setVisibility(View.VISIBLE);
    }

    private void showAsEventCreator(){
        Log.d("showAsEventCreator", "entering");
        btnEnCircleMe.setVisibility(View.GONE);
        btnUnCircleMe.setVisibility(View.GONE);
        userIsEventCreator = true;
        // invalidate menu so that onPrepareOptionsMenu gets called
        //invalidateOptionsMenu();
        // Show the delete button
       btnDeleteEvent.setVisibility(View.VISIBLE);
    }

    private void disableEnCircleFriends(){
        btnEnCircleFriend.setVisibility(View.GONE);
    }

    // For showing the delete icon in the menu if user is event creator
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        if(userIsEventCreator) {
            MenuItem deleteItem = menu.findItem(R.id.delete_event);
            deleteItem.setVisible(true);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

}
