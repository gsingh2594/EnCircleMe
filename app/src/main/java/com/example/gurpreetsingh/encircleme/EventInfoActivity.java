package com.example.gurpreetsingh.encircleme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class EventInfoActivity extends Fragment implements OnMapReadyCallback{

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

    private Button btnEnCircleMe, btnEnCircleFriend, btnUnCircleMe;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_event_info, container, false);
        //ListView listView = (ListView) view.findViewById(R.id.events_listview);
/*    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d("onCreate", "method started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.event_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();*/

/*        parentScroll = (ScrollView) findViewById(R.id.parent_scroll);
        childScroll=(ScrollView)findViewById(R.id.scroll_chat);*/

        //displayChatMessages();

        /*FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)view.findViewById(R.id.input);

                if(usernameIsLoaded) {
                    // Read the input field and push a new instance
                    // of ChatMessage to the Firebase database
                    if(input.getText().toString().equals("")){
                        // input.setError("Type a message");
                    }
                    else {
                        FirebaseDatabase.getInstance()
                                .getReference("event_chats/" + eventKey)
                                .push()
                                .setValue(new ChatMessageEvent(input.getText().toString(), username));

                        // Clear the input
                        input.setText("");
                    }
                }
                else{
                    ProgressDialog progressDialog = new ProgressDialog(EventInfoActivity.this.getActivity().getApplicationContext());
                    progressDialog.setMessage("One moment please");

                    while(!usernameIsLoaded){
                        Log.d("onClick", "Entering while loop because username is not loaded");
                        progressDialog.show();
                    }
                    progressDialog.hide();

                    if(input.getText().toString().equals("")){
                        //input.setError("Type a message");
                    }
                    else {
                        // Save message in DB
                        FirebaseDatabase.getInstance()
                                .getReference("event_chats/" + eventKey)
                                .push()
                                .setValue(new ChatMessageEvent(input.getText().toString(), username));

                        // Clear the input
                        input.setText("");
                    }
                }
            }
        });
        parentScroll.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                Log.v("PARENT", "PARENT TOUCH");
                findViewById(R.id.scroll_chat).getParent()
                        .requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });

        childScroll.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                Log.v("CHILD", "CHILD TOUCH");
                // Disallow the touch request for parent scroll on touch of
                // child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });*/
       return view;
    }


    @Override
    public void onStart() {
        super.onStart();

        txtEventName = (TextView) getView().findViewById(R.id.event_name);
        txtEventStartDate = (TextView) getView().findViewById(R.id.start_date);
        txtEventTime = (TextView) getView().findViewById(R.id.time);
        txtEventLocation = (TextView) getView().findViewById(R.id.location);
        txtEventDescription = (TextView) getView().findViewById(R.id.event_description);
        txtEventCreator = (TextView) getView().findViewById(R.id.creator_name);
        creatorProfileImage = (ImageView) getView().findViewById(R.id.creator_profile_image);

        btnEnCircleMe = (Button) getView().findViewById(R.id.encircle_event);
        btnEnCircleFriend = (Button) getView().findViewById(R.id.encircle_friends);
        btnUnCircleMe = (Button) getView().findViewById(R.id.uncircle_event);

        btnEnCircleMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enCircleUserInDB();
            }
        });

        btnEnCircleFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //enCircleFriendsInDB();
            }
        });

        btnUnCircleMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unCircleUserInDB();
            }
        });

        eventKey = getActivity().getIntent().getStringExtra("eventKey");
        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

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

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_snippet);
        // Configure MapFragment options and get a new instance
        GoogleMapOptions googleMapOptions =  new GoogleMapOptions().liteMode(true);
        mapFragment.newInstance(googleMapOptions);
        mapFragment.getMapAsync(this);

        loadUserName();
    }

    private void loadUserName(){
        DatabaseReference usernamesRef = FirebaseDatabase.getInstance().getReference("usernames");
        usernamesRef.orderByChild("id").equalTo(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 1) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        username = ds.getKey().toString();
                        usernameIsLoaded = true;
                        Toast.makeText(EventInfoActivity.this.getActivity().getApplicationContext(), "Welcome " + username, Toast.LENGTH_LONG).show();
                        //displayChatMessages();
                    }
                }
                else{
                    Log.d("Error", "user somehow has more than 1 username!");
                    throw new IllegalStateException("user has more than 1 username!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /*private void displayChatMessages() {
        ListView listOf_Messages = (ListView)getView().findViewById(R.id.list_of_eventmessages);

        adapter = new FirebaseListAdapter<ChatMessageEvent>(this, ChatMessageEvent.class,
                R.layout.message_event, FirebaseDatabase.getInstance().getReference("event_chats/" +eventKey)) {
            @Override
            protected void populateView(View v, ChatMessageEvent model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);
                Log.d("populate view", "getting msgs");

                // Set their text
                messageText.setText(model.getMessageText());
                messageText.setMovementMethod(LinkMovementMethod.getInstance());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("MM/dd/yy (hh:mma)",
                        model.getMessageTime()));
            }
        };
        listOf_Messages.setAdapter(adapter);
    }*/

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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("onCancelled", "Database Error: " + databaseError.getMessage());
                Toast.makeText(EventInfoActivity.this.getActivity().getApplicationContext(), "Could not load event", Toast.LENGTH_LONG).show();
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
                .snippet(event.getAddress())
                .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(R.drawable.marker_encircleme)));
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
        View customMarkerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.search_marker, null);
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
                Toast.makeText(getActivity(), "You are EnCircled!", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getActivity(), "You are UnCircled!", Toast.LENGTH_LONG).show();
                showAsEnCircleMe();
            }
        });
    }


    private void showAsEnCircleMe(){
        btnEnCircleMe.setVisibility(View.VISIBLE);
        btnUnCircleMe.setVisibility(View.GONE);
    }

    private void showAsUnCircleMe(){
        btnEnCircleMe.setVisibility(View.GONE);
        btnUnCircleMe.setVisibility(View.VISIBLE);
    }

}
