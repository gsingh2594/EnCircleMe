package com.example.gurpreetsingh.encircleme;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roughike.bottombar.BottomBar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by Brayden on 5/7/2017.
 */

public class EventInvitesFragment extends Fragment {
    FirebaseAuth auth;
    String currentUserID;
    FirebaseDatabase database;
    DatabaseReference dbRef;
    long numOfInvites;
    ArrayList<String>eventKeysList = new ArrayList<>();
    ArrayList<String>eventInvitersList = new ArrayList<>();
    final ArrayList<HashMap<String, String>> eventsList = new ArrayList<>();
    SimpleAdapter simpleAdapter;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_event_list, container, false);
        currentUserID = auth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadEventInvites();
        /*if(simpleAdapter != null)
            simpleAdapter.notifyDataSetChanged();*/
    }


    // Load all event invites for the current user
    private void loadEventInvites() {
        Log.d("loadEventInvites", "starting method");
        DatabaseReference eventInvitesRef = database.getReference("event_invites/" + currentUserID);
        eventInvitesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    numOfInvites = dataSnapshot.getChildrenCount();
                    Log.d("numOfInvies", "numOfInvites = " + numOfInvites);
                    for(DataSnapshot invite : dataSnapshot.getChildren()){
                        String eventKey = invite.getKey();
                        String inviterID = invite.getValue().toString();
                        Log.d("invite loaded", "eventKey = " + eventKey + "\ninviterID = " + inviterID);
                        eventKeysList.add(eventKey);
                        eventInvitersList.add(inviterID);
                        if(eventKeysList.size() == numOfInvites)
                            loadEventsAndInviters();
                    }
                }
                else{
                    // No event invites
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("loadEventInvites", "DB error: " + databaseError.getMessage());
            }
        });
    }


    /* Load the corresponding event info and the name and username of the event inviter
       for all loaded event invites */
    private void loadEventsAndInviters() {
        Log.d("loadEvents", "method started");
        for (int i = 0; i < eventKeysList.size(); i++) {
            final String eventKey = eventKeysList.get(i);
            final String eventInviterID = eventInvitersList.get(i);

            // Load the event info from the DB
            DatabaseReference eventsRef = database.getReference("events/all_events/" + eventKey);
            eventsRef.orderByChild("date").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Retrieve data as the Event object
                        Event event = dataSnapshot.getValue(Event.class);
                        Log.d("event info", event.toString());
                        // Check if event has ended
                        if (!eventHasEnded(event)) {
                            // Event has not ended --> store event info in a hashmap
                            final HashMap<String, String> eventInfo = new HashMap<String, String>();
                            eventInfo.put("eventKey", dataSnapshot.getKey());
                            eventInfo.put("name", event.getName());
                            eventInfo.put("description", event.getAbout());
                            eventInfo.put("startDate", event.getDate());
                            eventInfo.put("startTime", event.getStartTime());
                            eventInfo.put("endDate", event.getEndDate());
                            eventInfo.put("endTime", event.getEndTime());


                            // Load the event inviter's name and username from DB
                            DatabaseReference usernamesRef = database.getReference("usernames");
                            usernamesRef.orderByChild("id").equalTo(eventInviterID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for(DataSnapshot user: dataSnapshot.getChildren()) {
                                                String username = user.getKey();
                                                NameAndID nameAndID = user.getValue(NameAndID.class);
                                                eventInfo.put("invitedBy", nameAndID.getName() + " (" + username + ")");
                                                eventsList.add(eventInfo);
                                                if (eventsList.size() == numOfInvites)
                                                    displayInvitesList();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Log.d("loadInviter", "DB error: " + databaseError.getMessage());
                                        }
                                    });
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("loadEvents", "DB error: " + databaseError.getMessage());
                }
            });
        }
    }


    // returns true if event end date is before the current time, or false otherwise
    private boolean eventHasEnded(Event event){
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
                endHour = Integer.parseInt(hourMin[0])+ 12; // If pm and hour!=12, add 12 to the hour to convert to 24 hour format
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
        endCalendar.getTimeInMillis(); // get to recalcculate time in millis
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


    // Sorts the loaded events list by start date and time, then populates the list view
    private void displayInvitesList(){
        // Comparator for comparing and sorting dates for the events
        Comparator eventDatesAndTimesComparator = new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> event1, HashMap<String, String> event2) {

                int event1Month, event1Day, event1Year, event1Hour, event1Min;
                int event2Month, event2Day, event2Year, event2Hour, event2Min;

                // Get event1 start month, day, year
                String[] mdy1 = event1.get("startDate").split("/");
                event1Month = Integer.parseInt(mdy1[0]);
                event1Month-=1; // decrement month by 1 because range of months is 0-11
                event1Day = Integer.parseInt(mdy1[1]);
                event1Year = Integer.parseInt(mdy1[2]);

                // Get event2 start month, day, year
                String[] mdy2 = event2.get("startDate").split("/");
                event2Month = Integer.parseInt(mdy2[0]);
                event2Month-=1; // decrement month by 1 because range of months is 0-11
                event2Day = Integer.parseInt(mdy2[1]);
                event2Year = Integer.parseInt(mdy2[2]);

                // Get event1 start hour and min
                String hourMin1[] = event1.get("startTime").split("[: ]");
                if(hourMin1[2].equals("pm"))
                    if(Integer.parseInt(hourMin1[0]) != 12)
                        event1Hour = Integer.parseInt(hourMin1[0])+ 12; // If pm and hour!=12, add 12 to the hour to convert to 24 hour format
                    else
                        event1Hour = Integer.parseInt(hourMin1[0]);
                else
                    event1Hour = Integer.parseInt(hourMin1[0]); // Hour is in am
                event1Min = Integer.parseInt(hourMin1[1]);

                // Get event2 start hour and min
                String[] hourMin2 = event2.get("startTime").split("[: ]");
                if(hourMin2[2].equals("pm"))
                    if(Integer.parseInt(hourMin2[0]) != 12)
                        event2Hour = Integer.parseInt(hourMin2[0])+ 12; // If pm and hour!=12, add 12 to the hour to convert to 24 hour format
                    else
                        event2Hour = Integer.parseInt(hourMin2[0]);
                else
                    event2Hour = Integer.parseInt(hourMin2[0]); // Hour is in am
                event2Min = Integer.parseInt(hourMin2[1]);


                // Calendar for event1
                Calendar event1Calendar = Calendar.getInstance();
                event1Calendar.clear(); // Clear the fields in the calendar so the compareTo method works properly
                event1Calendar.set(event1Year, event1Month, event1Day, event1Hour, event1Min, 0);
                //event1Calendar.getTimeInMillis(); // get to recalcculate time in millis
                Log.d("event1Calendar", "after setting calendar: " + Long.toString(event1Calendar.getTimeInMillis()));

                // Calendar for event1
                Calendar event2Calendar = Calendar.getInstance();
                event2Calendar.clear(); // Clear the fields in the calendar so the compareTo method works properly
                event2Calendar.set(event2Year, event2Month, event2Day, event2Hour, event2Min, 0);
                //event1Calendar.getTimeInMillis(); // get to recalcculate time in millis
                Log.d("event2Calendar", "after setting calendar: " + Long.toString(event2Calendar.getTimeInMillis()));

                // Compare the event1 and event2 calendars and and return the result
                int result = event1Calendar.compareTo(event2Calendar);
                Log.d("comparator result", Integer.toString(result));
                return result;
            }
        };

        /* Sort the events in the events list by date using the comparator
                     https://docs.oracle.com/javase/7/docs/api/java/util/Collections.html#sort(java.util.List)
                     The documentation says it uses a mergesort implementation ^
                     --> Runtime of O(n * log n) worst case, or almost O(n) when mostly sorted */
        Collections.sort(eventsList, eventDatesAndTimesComparator);

        // Find listview from layout and initialize with an adapter
        final ListView listView = (ListView) getView().findViewById(R.id.events_listview);
        simpleAdapter = new SimpleAdapter(EventInvitesFragment.this.getActivity().getApplicationContext(), eventsList, R.layout.event_invites_list_items,
                new String[]{"startDate", "startTime", "name", "description", "invitedBy"},
                new int[]{R.id.start_date, R.id.start_time, R.id.event_name, R.id.event_about, R.id.inviter});
        listView.setAdapter(simpleAdapter);

        // Show event info when clicking on item in list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String eventKey = eventsList.get(position).get("eventKey");
                Intent fullEventInfo = new Intent(EventInvitesFragment.this.getActivity().getApplicationContext(), EventInfoActivity.class);
                fullEventInfo.putExtra("eventKey", eventKey);
                startActivity(fullEventInfo);
            }
        });
    }


}
