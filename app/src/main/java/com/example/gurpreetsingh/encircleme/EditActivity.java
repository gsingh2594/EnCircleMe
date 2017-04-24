package com.example.gurpreetsingh.encircleme;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

//import android.icu.util.Calendar;     <-- not supported with our minSDK


public class EditActivity extends Activity implements View.OnClickListener{
    private TimePicker timePicker1;
    private TimePicker timePicker2;
    private TextView time;
    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int startYear, startMonth, startDay;
    private int startHour, startMinute, endHour, endMinute;
    private boolean startDateSelected, startTimeSelected, endDateSelected, endTimeSelected;
    private String format = "";

    private TextView mPlaceAttribution;
    private Button btnDatePicker, btnTimePicker, btnEndDatePicker, btnEndTimePicker, btnSave, btnPlacePicker, btnPlacemap;
    private EditText eventName, about;
    private TextView txtDate, txtTime, txtEndDate, txtEndTime;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private DatePickerDialog datePickerDialog, endDatePickerDialog;
    private TimePickerDialog startTimePickerDialog, endTimePickerDialog;
    private LatLng latLng;
    private String placeID;

    private static final int PLACE_PICKER_REQUEST = 1000;
    private GoogleApiClient mClient;
    private TextView mName;
    private TextView mplace;
    private TextView mAddress;
    private TextView mLatLng;

    private FirebaseAuth auth;
    private String userID;
    private String eventKey;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private DatabaseReference eventsRef;

    private ArrayList<Event>usersCreatedEventsList;
    private ArrayList<String>usersCreatedEventsKeysList;
    private int nextUserCreatedEventIndex = -1; // -1 indicates error until user's events load from DB

    private StorageReference profileImageStorageRef;
    private byte[] profileImageBytes;
    private static final long ONE_MEGABYTE = 1024 * 1024;

    private String placeName;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        btnPlacemap = (Button) findViewById(R.id.placemap);
        mPlaceAttribution = (TextView) findViewById(R.id.place_attribution);
        btnDatePicker=(Button)findViewById(R.id.btn_date);
        btnTimePicker=(Button)findViewById(R.id.btn_time);
        btnEndDatePicker = (Button)findViewById(R.id.btn_end_date);
        btnEndTimePicker=(Button)findViewById(R.id.btn_endtime);
        btnSave = (Button) findViewById(R.id.save);
        txtDate=(TextView)findViewById(R.id.in_date);
        txtTime=(TextView)findViewById(R.id.in_time);
        txtEndDate = (TextView)findViewById(R.id.end_date);
        txtEndTime=(TextView)findViewById(R.id.end_time);

        mPlaceAttribution = (TextView) findViewById(R.id.place_attribution);
        //btnPlacePicker=(Button)findViewById(R.id.pickerButton);
        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);
        btnEndDatePicker.setOnClickListener(this);
        btnEndTimePicker.setOnClickListener(this);
        //btnPlacePicker.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnPlacemap.setOnClickListener(this);


        //Place Picker result textview
        // mName = (TextView) findViewById(R.id.textView1);
        mplace = (TextView) findViewById(R.id.textView1);
        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();

        //latLng = (LatLng) getIntent().getParcelableExtra("location");

        eventName = (EditText) findViewById(R.id.eventname);
        about = (EditText) findViewById(R.id.about);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        loadNextUserCreatedEventIndex();

        //Button button1 = (Button) findViewById(R.id.pickerButton);

        /*
        final TextView Date = (TextView) findViewById(R.id.textView3);
        final TextView Time = (TextView) findViewById(R.id.textView4);*/

        /*timePicker1 = (TimePicker) findViewById(R.id.timePicker1);
        timePicker2 = (TimePicker) findViewById(R.id.timePicker2);
        time = (TextView) findViewById(R.id.textView4);
        dateView = (TextView) findViewById(R.id.textView3);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month, day);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        showStartTime(hour, min);
        showEndTime(hour, min);*/
    }

    private void loadNextUserCreatedEventIndex(){
        // Load user created events from DB to determine next index for a new event
        DatabaseReference userCreatedEventsRef = dbRef.child("events/user_created_events/" + userID);
        userCreatedEventsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    ArrayList<Event> userCreatedEventsList = dataSnapshot.getValue(new GenericTypeIndicator<ArrayList<Event>>(){});
                    nextUserCreatedEventIndex = userCreatedEventsList.size();
                    Log.d("dataSnapshot exists", userCreatedEventsList.toString());
                }
                else{
                    nextUserCreatedEventIndex = 0;
                    Log.d("dataSnapshot exists", "Does not exist");
                }
                Log.d("nextUserEventIndex", Integer.toString(nextUserCreatedEventIndex));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("onCancelled()", "Error loading from DB " + databaseError.getMessage());
                Toast.makeText(EditActivity.this, "Error loading events list from DB", Toast.LENGTH_LONG).show();
            }
        });
    }

    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        if (v == btnDatePicker) {
            final Calendar c = Calendar.getInstance();
            if (startYear == 0 && startMonth == 0 && startDay == 0) {
                // User has not picked a start date yet. Set start date to the current date
                startYear = c.get(Calendar.YEAR);
                startMonth = c.get(Calendar.MONTH);
                startDay = c.get(Calendar.DAY_OF_MONTH);
            }

            datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            txtDate.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
                            startYear = year;
                            startMonth = monthOfYear;
                            startDay = dayOfMonth;

                            startDateSelected = true;
                            resetDateAndTimeErrors();
                            // Check if selected date is equal to current date
                            if (startYear == c.get(Calendar.YEAR) && startMonth == c.get(Calendar.MONTH)
                                    && startDay == c.get(Calendar.DAY_OF_MONTH)) {
                                if (startTimeSelected && !endDateSelected && endTimeSelected)
                                    // start and end times are selected, but end date is not selected
                                    // Check if start and end times are valid for the single (start) date
                                    datesAndTimesAreValid();
                                else if (startTimeSelected) {
                                    // Check if start time is later than current time
                                    checkStartTimeValidity();
                                }
                            }
                        }
                    }, startYear, startMonth, startDay);
            datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
            datePickerDialog.show();
        }

        if (v == btnTimePicker) {
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // If start time is already set, display it as the default time in the picker
            if (startTimeSelected) {
                mHour = startHour;
                mMinute = startMinute;
            }else{
                mMinute+= 5;  // add 5 minutes to default start time (the current time)

                // Check if 5 minute increase should change the hour.
                // Note: Does not take into account if the date should be incremented --> End date required
                if (mMinute >= 60) {
                    mMinute -= 60;
                    mHour += 1;
                    if (mHour > 24)
                        Toast.makeText(EditActivity.this, "You should add an end date!", Toast.LENGTH_LONG).show();
                }
            }

                // Launch Time Picker Dialog
                startTimePickerDialog = new TimePickerDialog(this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                startHour = hourOfDay;
                                startMinute = minute;

                                int hour = hourOfDay % 12;
                                txtTime.setText(String.format("%02d:%02d %s", hour == 0 ? 12 : hour,
                                        minute, hourOfDay < 12 ? "am" : "pm"));

                                startTimeSelected = true;
                                resetDateAndTimeErrors();
                                if (startDateSelected) {
                                    // Check if selected start date is equal to current date
                                    if (startYear == c.get(Calendar.YEAR) && startMonth == c.get(Calendar.MONTH)
                                            && startDay == c.get(Calendar.DAY_OF_MONTH)) {
                                        checkStartTimeValidity();
                                        if (!endDateSelected && endTimeSelected) {
                                            datesAndTimesAreValid();
                                        }
                                    }
                                }
                            }
                        }, mHour, mMinute, false);
                startTimePickerDialog.show();
            }

            if (v == btnEndDatePicker) {
                if (!startDateSelected) {
                    // User must select a start date before the end date
                    txtDate.setError("Please select a start date first!");
                } else {
                    endDatePickerDialog = new DatePickerDialog(EditActivity.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            txtEndDate.setText((month + 1) + "/" + dayOfMonth + "/" + year);

                            endDateSelected = true;
                            if(endTimeSelected){
                                resetDateAndTimeErrors();
                            }
                        }
                    }, startYear, startMonth, startDay);    // Set datePicker to have same date as selected start date

                    // Add one day to the selected start date and set it as the minimum end date:
                    Calendar defaultEndDate = Calendar.getInstance();   // gets current date
                    defaultEndDate.clear(); // clear the current date
                    defaultEndDate.set(startYear, startMonth, startDay);  // set calendar to selected start date
                    // add one day (in milliseconds) to selected start date (in milliseconds)
                    long defaultEndDateInMillis = defaultEndDate.getTimeInMillis() + (24 * 60 * 60 * 1000);
                    // set the minimum date to the one day in advance
                    endDatePickerDialog.getDatePicker().setMinDate(defaultEndDateInMillis);
                    endDatePickerDialog.show();
                }
            }

            if (v == btnEndTimePicker) {
                // Get Current Time
                final Calendar cal = Calendar.getInstance();
                mHour = cal.get(Calendar.HOUR_OF_DAY);
                mMinute = cal.get(Calendar.MINUTE);

                // Set end time equal to start time if it has been set
                if (startTimeSelected) {
                    mHour = startHour;
                    mMinute = startMinute + 5;  // Default end time increased by 5 minutes

                    // Check if minute increase should change the hour.
                    // Note: Does not take into account if the date should be incremented --> End date required
                    if (mMinute >= 60) {
                        mMinute -= 60;
                        mHour += 1;
                        if (mHour > 24)
                            Toast.makeText(EditActivity.this, "You should add an end date!", Toast.LENGTH_LONG).show();
                    }
                }

                // Launch Time Picker Dialog
                endTimePickerDialog = new TimePickerDialog(this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                endHour = hourOfDay;
                                endMinute = minute;
                                int hour = hourOfDay % 12;
                                txtEndTime.setText(String.format("%02d:%02d %s", hour == 0 ? 12 : hour,
                                        minute, hourOfDay < 12 ? "am" : "pm"));

                                endTimeSelected = true;
                                resetDateAndTimeErrors();
                                if (startTimeSelected && startDateSelected)
                                    datesAndTimesAreValid();
                            }
                        }, mHour, mMinute, false);
                endTimePickerDialog.show();
            }

        /*if (v == btnPlacePicker) {
            mAddress = (TextView) findViewById(R.id.textView2);
            btnPlacePicker=(Button)findViewById(R.id.pickerButton1);

            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(EditActivity.this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }*/

        if (v == btnPlacemap) {
            Intent i = new Intent(this, PlaceActivity.class);
            startActivityForResult(i, 1);
        }

        if (v == btnSave) {
            saveEventInDB();
        }
    }



    // Checks for valid inputs (showing error messages if not valid) and saves event in DB if inputs are valid
    private void saveEventInDB() {
        resetAllErrors();
        // Check to make sure inputs are valid
        if (inputsAreNotEmpty() && datesAndTimesAreValid()) {
            // Inputs are valid --> proceed to save in DB
            Log.d("inputsAreNotEmpty = ", Boolean.toString(inputsAreNotEmpty()));
            Log.d("datesAndTimes= ", Boolean.toString(datesAndTimesAreValid()));

            // Check if user's pasts events ArrayList has already loaded
            if (nextUserCreatedEventIndex != -1) {
                // Previous events ArrayList already loaded
                // Save event in DB at nextUserCreatedEventIndex
                eventsRef = database.getReference("events");
                // Get a unique key for the event from firebase push() method
                eventKey = eventsRef.push().getKey();

                // Get all user input for the event
                String evName = eventName.getText().toString();
                String evAbout = about.getText().toString();
                String evStartDate = txtDate.getText().toString();
                String evStartTime = txtTime.getText().toString();
                String evEndDate;
                if(endDateSelected)
                    evEndDate = txtEndDate.getText().toString();
                else
                    // Set end date equal to start date
                    evEndDate = txtDate.getText().toString();
                String evEndTime = txtEndTime.getText().toString();
                String evPlaceID = null;    // placeID saved as null if no place selected
                if(placeID != null)
                    evPlaceID = placeID;
                double evLat = latLng.latitude;
                double evLng = latLng.longitude;

                // Create event object to save
                Event event = new Event(evName, evAbout, evStartDate, evStartTime, evEndDate, evEndTime, placeName, evPlaceID, evLat, evLng);

                // HashMap for multipath updates
                Map<String, Object> eventUpdates = new HashMap<>();
                eventUpdates.put("all_events/" + eventKey, event);
                eventUpdates.put("user_created_events/" + userID + "/" + nextUserCreatedEventIndex, event);
                eventUpdates.put("event_key_creators/" + eventKey, userID);
                //eventUpdates.put("geofire_locations/" + eventKey, new GeoLocation(latLng.latitude, latLng.longitude));

                // Update DB at multiple locations
                eventsRef.updateChildren(eventUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            // DB update failed
                            Log.d("btnSave", "Failed to save in database: \n" + databaseError.getMessage());
                            Toast.makeText(getBaseContext(), "Failed to create event", Toast.LENGTH_LONG).show();
                        } else {
                            // DB update successful
                            Log.d("btnSave", "Database updated successfully");
                            Toast.makeText(getBaseContext(), "Event created!", Toast.LENGTH_LONG).show();

                            // Save location with GeoFire
                            DatabaseReference geoFireLocationsRef = eventsRef.child("geofire_locations");
                            GeoFire geoFireEvents = new GeoFire(geoFireLocationsRef);
                            geoFireEvents.setLocation(eventKey, new GeoLocation(latLng.latitude, latLng.longitude),
                                    new GeoFire.CompletionListener() {
                                        @Override
                                        public void onComplete(String key, DatabaseError error) {
                                            Log.d("GeoFire.setLocation()", "GeoLocation saved");
                                            Toast.makeText(EditActivity.this, "Location saved", Toast.LENGTH_LONG).show();

                                            nextUserCreatedEventIndex++;
                                            // Load profile image from storage
                                            profileImageStorageRef = FirebaseStorage.getInstance().getReference("profile_images/" + userID);
                                            profileImageStorageRef.getBytes(ONE_MEGABYTE * 5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                                @Override
                                                public void onSuccess(byte[] bytes) {
                                                    // user has profile pic --> display it
                                                    Log.d("loadUserProfileImage()", "getBytes successful");
                                                    profileImageBytes = bytes;
                                                    Log.d("loadUserProfileImage()", "convert bytes to bitmap");
                                                    Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.length);

                                                    // Create marker with user profile image for map
                                                    createMarkerForMap(profileImageBitmap);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    // Handle any errors
                                                    Log.d("loadUserProfileImage()", "Firebase storage exception " + exception.getMessage());
                                                    Toast.makeText(EditActivity.this, "Failed to load profile image", Toast.LENGTH_LONG).show();
                                                    // Create marker without a profile image for map
                                                    createMarkerForMap(null);
                                                }
                                            });
                                        }
                                    });
                        }
                    }


                    private void createMarkerForMap(Bitmap profileImage) {
                        // Marker for map
                        MarkerOptions marker;
                        // Check if a Bitmap was passed as an argument
                        if (profileImage != null) {
                            // User has profile image --> display it in the marker
                            Bitmap scaledProfileImage = Bitmap.createScaledBitmap(profileImage, 200, 200, false);
                            marker = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory
                                    .fromBitmap(getMarkerBitmapFromView(scaledProfileImage)));
                        } else {
                            // User doesn't have profile image --> show default icon
                            marker = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory
                                    .fromBitmap(getMarkerBitmapFromView(R.drawable.neutral_face_icon)));
                        }
                        if (eventName.getText() != null) {
                            marker.title("Event: " + eventName.getText().toString() + ", " +
                                    (about.getText().toString()));
                            marker.snippet((txtDate.getText().toString() + " " + (txtTime.getText().toString() +
                                    " to " + (txtEndTime.getText().toString()))));
                            marker.draggable(true);
                        }

                        // Return marker as activity result
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("marker", marker);
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                });
            } else {
                AlertDialog.Builder databaseErrorAlert = new AlertDialog.Builder(EditActivity.this);
                databaseErrorAlert.setMessage("Database error loading previous events");
            }
        }
    }


    // Create marker bitmap with a drawable icon
    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {
        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
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

    // Create marker bitmap with profile image
    private Bitmap getMarkerBitmapFromView(Bitmap profileImageBitmap) {
        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.profile_image);
        markerImageView.setImageBitmap(profileImageBitmap);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("onActivityResult", "entering");
        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(this, data);

            /*final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            final String latlng = place.getLatLng().toString();
*/
            mName.setText(formatPlaceDetails(getResources(), place.getName(), place.getRating(),
                    place.getId(), place.getAddress(), place.getPhoneNumber(),
                    place.getWebsiteUri(), place.getLatLng()));
            //mAddress.setText(address);
            //mLatLng.setText(latlng);
            //mAttributions.setText(Html.fromHtml(attributions));

// Display attributions if required.
           /* CharSequence attributions = place.getAttributions();
            if (!TextUtils.isEmpty(attributions)) {
                mPlaceAttribution.setText(Html.fromHtml(attributions.toString()));
            } else {
                mPlaceAttribution.setText("");
            }*/
        }
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                if(data.getStringExtra("user_picked_location") != null){
                    // User selected a location by dropping a marker on the map
                    // Retrieve the latitude and longitude of dropped marker from PlaceActivity
                    double lat = data.getDoubleExtra("lat", -1);
                    double lng = data.getDoubleExtra("lng", -1);
                    if (lat != -1 && lng != -1) {
                        LatLng eventLocation = new LatLng(lat, lng);
                        // set latLng for saving in DB
                        latLng = eventLocation;
                        // display lat and lng of user's selected place
                        mplace.setText("Lat:\n" + String.valueOf(latLng.latitude)
                                + "\nLng:\n" + String.valueOf(latLng.longitude));
                    }
                }
                else {
                    // User selected a place on the map
                    String result = data.getStringExtra("result");
                    placeID = data.getStringExtra("place_id");
                    placeName = data.getStringExtra("place_name");
                    String address = data.getStringExtra("address");
                    String vicinity = data.getStringExtra("vicinity");
                    Log.d("placeName", placeName);
                    mplace.setText(placeName);  // display the name of the picked place
                    double lat = Double.parseDouble(data.getStringExtra("lat"));
                    double lng = Double.parseDouble(data.getStringExtra("lng"));
                    latLng = new LatLng(lat, lng);
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
            // remove any error message since place is now selected
            mplace.setError(null);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private static Spanned formatPlaceDetails(Resources res, CharSequence name, float rating, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri, LatLng latLng) {
        Log.e(TAG, res.getString(R.string.place_details, name, rating, id, address, phoneNumber,
                websiteUri, latLng));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber, rating,
                websiteUri, latLng));
    }


    // removes any previously set errors on date and time TextViews
    private void resetDateAndTimeErrors(){
        txtDate.setError(null);
        txtTime.setError(null);
        txtEndDate.setError(null);
        txtEndTime.setError(null);
    }

    private void resetAllErrors(){
        eventName.setError(null);
        about.setError(null);
        txtDate.setError(null);
        txtTime.setError(null);
        txtEndDate.setError(null);
        txtEndTime.setError(null);
        mplace.setError(null);
    }

    // Returns true if all TextViews are not empty
    private boolean inputsAreNotEmpty(){
        boolean inputsNotEmpty = true;
        Log.d("inputsAreNotEmpty", "starting method");
        Log.d("inputsAreNotEmpty", "checking event name");
        if(eventName.getText().toString().equals("")){
            eventName.setError("Please enter a name for the event");
            inputsNotEmpty = false;
        }
        Log.d("inputsAreNotEmpty", "checking event description");
        if(about.getText().toString().equals("")){
            about.setError("Please enter an event description");
            inputsNotEmpty = false;
        }
        Log.d("inputsAreNotEmpty", "checking date");
        if(!startDateSelected){
            txtDate.setError("Please select a date!");
            inputsNotEmpty = false;
        }
        Log.d("inputsAreNotEmpty", "checking start time");
        if(!startTimeSelected){
            txtTime.setError("Please select a start time!");
            inputsNotEmpty = false;
        }
        Log.d("inputsAreNotEmpty", "checking end time");
        if(!endTimeSelected){
            txtEndTime.setError("Please select an end time!");
            inputsNotEmpty = false;
        }
        Log.d("inputsAreNotEmpty", "checking location");
        if(mplace.getText().equals("Place")){
            mplace.setError("Please select a place!");
            inputsNotEmpty = false;
        }
        Log.d("event name", eventName.getText().toString());
        Log.d("event description", about.getText().toString());
        return inputsNotEmpty;
    }


    /* Returns true if selected date and start time is not before the current date/time,
    and end time is not before/equal to start time */
    private boolean datesAndTimesAreValid(){
        // Get current date and time via Calendar
        Calendar currentCalendar = Calendar.getInstance();
        DatePicker datePicker = datePickerDialog.getDatePicker();
        Calendar selectedCalendar = Calendar.getInstance();
        // Set selectedCalendar equal to date chosen from DatePickerDialog
        selectedCalendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());

        // Check if selected date is before current date
        if(selectedCalendar.compareTo(currentCalendar) < 0) {
            // selected date is before current date --> not valid
            txtDate.setError("Date cannot be in the past!");
            txtDate.requestFocus();
            return false;
        }
        // Check if selected date is equal to current date
        else if(selectedCalendar.compareTo(currentCalendar) == 0) {
            // Selected date and current date are equal --> check if start time is before current time
            // Get start time from TextView
            String[] mdy = txtDate.getText().toString().split("/");
            int month = Integer.parseInt(mdy[0])-1;
            int day = Integer.parseInt(mdy[1]);
            int year = Integer.parseInt(mdy[2]);
            Calendar startTime = Calendar.getInstance(); // calendar object for start time
            startTime.clear();      // Remove previous values so the set() method works properly
            startTime.set(year, month, day, startHour, startMinute); // set calendar so it is easy to get time in millis

            // Check if start time is before current time
            Log.d("startTime", Long.toString(startTime.getTimeInMillis()));
            Log.d("currentTime", Long.toString(Calendar.getInstance().getTimeInMillis()));
            if(startTime.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
                // start time is before current time --> invalid
                txtTime.setError("Start time must be later than the current time!");
                return false;
            }
            else {
                // Selected date is after current date --> Check if start time is before end time
                //if (txtTime.getText().toString().compareTo(txtEndTime.getText().toString()) < 0)
                Log.d("startHour", Integer.toString(startHour));
                Log.d("startMinute", Integer.toString(startMinute));
                Log.d("endHour", Integer.toString(endHour));
                Log.d("endMinute", Integer.toString(endMinute));
                if(endHour > startHour)
                    return true;
                else if(endHour==startHour){
                    if(endMinute > startMinute)
                        return true;
                    else{
                        txtEndTime.setError("End time must be after the start time!");
                        return false;
                    }
                }
                else { // endHour is before startHour
                    txtEndTime.setError("End time must be after the start time!");
                    return false;
                }
            }
        }
        else{
            // selected date is after current date --> check if times are valid
            Log.d("startHour", Integer.toString(startHour));
            Log.d("startMinute", Integer.toString(startMinute));
            Log.d("endHour", Integer.toString(endHour));
            Log.d("endMinute", Integer.toString(endMinute));
            if(endHour > startHour)
                return true;
            else if(endHour==startHour){
                if(endMinute > startMinute)
                    return true;
                else{
                    txtEndTime.setError("End time must be after the start time!");
                    return false;
                }
            }
            else { // endHour is before startHour
                txtEndTime.setError("End time must be after the start time!");
                return false;
            }
        }
    }

    /* Compares selected start time to the current time
       Displays error message if current time is after selected start time */
    private void checkStartTimeValidity(){
        // Get start time from TextView
        String[] mdy = txtDate.getText().toString().split("/");
        int month = Integer.parseInt(mdy[0]) -1;
        int day = Integer.parseInt(mdy[1]);
        int year = Integer.parseInt(mdy[2]);
        Log.d("month", Integer.toString(month));
        Log.d("day", Integer.toString(day));
        Log.d("year", Integer.toString(year));
        Log.d("startHour", Integer.toString(startHour));
        Log.d("startMinute", Integer.toString(startMinute));
        Calendar startTime = Calendar.getInstance(); // calendar object for start time
        startTime.clear();      // Remove previous values so the set() method works properly
        startTime.set(year, month, day, startHour, startMinute); // set calendar so it is easy to get time in millis

        // Check if start time is before current time
        Log.d("startTime", Long.toString(startTime.getTimeInMillis()));
        Log.d("currentTime", Long.toString(Calendar.getInstance().getTimeInMillis()));
        if(startTime.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
            // start time is before current time --> invalid
            txtTime.setError("Start time must be later than the current time!");
        }
    }


   /* @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);
        Toast.makeText(getApplicationContext(), "Choose a date",
                Toast.LENGTH_LONG)
                .show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this,
                    myDateListener, year, month, day);
        }
        return null;
    }
*/

    private DatePickerDialog.OnDateSetListener myDateListener = new
            DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker arg0,
                                      int arg1, int arg2, int arg3) {
                    // TODO Auto-generated method stub
                    // arg1 = year
                    // arg2 = month
                    // arg3 = day
                    showDate(arg1, arg2+1, arg3);
                }
            };

    private void showDate(int year, int month, int day) {
        dateView.setText(new StringBuilder().append(month).append("/")
                .append(day).append("/").append(year));
    }

/*

    @TargetApi(Build.VERSION_CODES.M)
    public void setTime(View view) {
        showDialog(888);
        Toast.makeText(getApplicationContext(), "Choose a date",
                Toast.LENGTH_LONG)
                .show();
    }

    public void showStartTime(int hour, int min) {
        if (hour == 0) {
            hour += 12;
            format = "AM";
        } else if (hour == 12) {
            format = "PM";
        } else if (hour > 12) {
            hour -= 12;
            format = "PM";
        } else {
            format = "AM";
        }

        time.setText(new StringBuilder().append(hour).append(" : ").append(min)
                .append(" ").append(format));
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void setTimePicker2(View view) {
        int hour = timePicker2.getHour();
        int min = timePicker2.getMinute();
        showEndTime(hour, min);
    }

    public void showEndTime(int hour, int min) {
        if (hour == 0) {
            hour += 12;
            format = "AM";
        } else if (hour == 12) {
            format = "PM";
        } else if (hour > 12) {
            hour -= 12;
            format = "PM";
        } else {
            format = "AM";
        }

        time.setText(new StringBuilder().append(hour).append(" : ").append(min)
                .append(" ").append(format));
    }*/

}