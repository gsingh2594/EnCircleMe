package com.example.gurpreetsingh.encircleme;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.icu.util.Calendar;     <-- not supported with our minSDK
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class EditActivity extends Activity implements View.OnClickListener{
    private TimePicker timePicker1;
    private TimePicker timePicker2;
    private TextView time;
    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int year, month, day;
    private int startHour, startMinute, endHour, endMinute;
    private String format = "";

    private Button btnDatePicker, btnTimePicker, btnEndTimePicker, btnSave;
    private LatLng latLng;
    private EditText eventName, about;
    private TextView txtDate, txtTime, txtEndTime;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private DatePickerDialog datePickerDialog;
    private TimePickerDialog startTimePickerDialog, endTimePickerDialog;

    private FirebaseAuth auth;
    private String userID;
    private String eventKey;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;

    private ArrayList<Event>usersCreatedEventsList;
    private ArrayList<String>usersCreatedEventsKeysList;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editactivity);

        btnDatePicker=(Button)findViewById(R.id.btn_date);
        btnTimePicker=(Button)findViewById(R.id.btn_time);
        btnEndTimePicker=(Button)findViewById(R.id.btn_endtime);
        btnSave = (Button) findViewById(R.id.save);
        txtDate=(TextView)findViewById(R.id.in_date);
        txtTime=(TextView)findViewById(R.id.in_time);
        txtEndTime=(TextView)findViewById(R.id.end_time);

        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);
        btnEndTimePicker.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        latLng = (LatLng) getIntent().getParcelableExtra("location");

        eventName = (EditText) findViewById(R.id.eventname);
        about = (EditText) findViewById(R.id.about);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
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

    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        if (v == btnDatePicker) {
            // Get Current Date
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {
                            txtDate.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
            datePickerDialog.show();
        }

        if (v == btnTimePicker) {
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            startTimePickerDialog = new TimePickerDialog(this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {
                            startHour = hourOfDay;
                            startMinute = minute;
                            int hour = hourOfDay % 12;
                            txtTime.setText(String.format("%02d:%02d %s", hour == 0 ? 12 : hour,
                                    minute, hourOfDay < 12 ? "am" : "pm"));
                        }
                    }, mHour, mMinute, false);
            startTimePickerDialog.show();
        }

        if (v == btnEndTimePicker) {
            // Get Current Time
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

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
                        }
                    }, mHour, mMinute, false);
            endTimePickerDialog.show();
        }

        if(v == btnSave){
            resetErrorMessages();
            if(inputsAreNotEmpty() && datesAndTimesAreValid()){
                // Save event in DB
                DatabaseReference eventsRef = database.getReference("events");
                eventKey = eventsRef.push().getKey();
                Event event = new Event(eventName.getText().toString(), about.getText().toString(),
                        txtDate.getText().toString(), txtTime.getText().toString(),
                        txtEndTime.getText().toString(), latLng);

                if(usersCreatedEventsList == null)
                    usersCreatedEventsList = new ArrayList<>();
                if(usersCreatedEventsKeysList==null)
                    usersCreatedEventsKeysList = new ArrayList<>();
                usersCreatedEventsList.add(event);
                usersCreatedEventsKeysList.add(eventKey);

                Map<String, Object>eventUpdates = new HashMap<>();
                eventUpdates.put("all_events/" + eventKey, event);
                eventUpdates.put("user_created_events/" + userID, usersCreatedEventsList);
                eventUpdates.put("user_created_events_keys/" + userID, usersCreatedEventsKeysList);

                // Update DB at multiple locations
                eventsRef.updateChildren(eventUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError != null){
                            // DB update failed
                            Log.d("btnSave", "Failed to save in database: \n" + databaseError.getMessage());
                            Toast.makeText(getBaseContext(), "Failed to create event", Toast.LENGTH_LONG).show();
                        }
                        else {
                            // DB update sucessful
                            Log.d("btnSave", "Database updated successfully");
                            Toast.makeText(getBaseContext(), "Event created!", Toast.LENGTH_LONG).show();

                            // Create marker for map
                            MarkerOptions marker = new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
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
                    }
                });
            }
        }
    }


    // removes any previously set errors on TextViews
    private void resetErrorMessages(){
        txtDate.setError(null);
        txtTime.setError(null);
        txtEndTime.setError(null);
    }

    // Returns true if all TextViews are not empty
    private boolean inputsAreNotEmpty(){
        if(txtDate.getText() == null){
            txtDate.setError("Please select a date!");
            return false;
        }
        else if(txtTime.getText() == null){
            txtTime.setError("Please select a start time!");
            return false;
        }
        else if(txtEndTime.getText() == null){
            txtEndTime.setError("Please select an end time!");
            return false;
        }else{
            return true;
        }
    }


    /* Returns true if selected date is not before the current date,
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
        else if(selectedCalendar.compareTo(currentCalendar) == 0) {
            // Selected date and current date are equal --> check if start time is before current time
            // Get start time from TextView
            String[] mdy = txtDate.getText().toString().split("/");
            int month = Integer.parseInt(mdy[0]);
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