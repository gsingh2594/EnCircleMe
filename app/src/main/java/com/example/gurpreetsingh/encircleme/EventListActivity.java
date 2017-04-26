package com.example.gurpreetsingh.encircleme;

/**
 * Created by GurpreetSingh on 4/18/17.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class EventListActivity extends AppCompatActivity {


    FirebaseAuth auth;
    String currentUserID;
    FirebaseDatabase database;
    DatabaseReference dbRef;
    SimpleAdapter simpleAdapter;
    private HashMap<String, Event> eventsInfo;

    private BottomBar bottomBar;

    //ArrayList<String> list=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.eventlist_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("EnCircleMe Events");

        currentUserID = auth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        eventsInfo = new HashMap<String, Event>();
/*        listview=(ListView)findViewById(R.id.listview);
        final ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,list);
        listview.setAdapter(adapter);
        dref=FirebaseDatabase.getInstance().getReference("events/all_events");
        dref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                list.add(dataSnapshot.getValue(String.class));
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                list.remove(dataSnapshot.getValue(String.class));
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_alerts);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_profile) {
                    Intent profile = new Intent(getApplicationContext(), UserProfileActivity.class);
                    startActivity(profile);
                } else if (tabId == R.id.tab_friends) {
                    Intent friends = new Intent(getApplicationContext(), FriendsActivity.class);
                    startActivity(friends);
                } else if (tabId == R.id.tab_map) {
                    Intent map = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(map);
/*                } else if (tabId == R.id.tab_alerts) {
                    Intent events = new Intent(getApplicationContext(), EventListActivity.class);
                    startActivity(events);*/
                } else if (tabId == R.id.tab_chats) {
                    Intent events = new Intent(getApplicationContext(), ChatActivity.class);
                    startActivity(events);
                }
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d("onStart", "starting the activity");
        loadEventsList();
        if(simpleAdapter != null)
            simpleAdapter.notifyDataSetChanged();
    }


    // load and display list of all the events (past and future)
    private void loadEventsList(){
        Log.d("loadEventsList", "method started");
        final ArrayList<HashMap<String, String>> eventsList = new ArrayList<HashMap<String,String>>();
        DatabaseReference eventsRef = database.getReference("events/all_events");
        eventsRef.orderByChild("date").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    for(DataSnapshot child : dataSnapshot.getChildren()){
                        // Retrieve data as the Event object
                        Event event = child.getValue(Event.class);
                        Log.d("event info", event.toString());
                        // Check if event has ended
                        if(!eventHasEnded(event)) {
                            // Store event info in a hashmap
                            HashMap<String, String> eventInfo = new HashMap<String, String>();
                            eventInfo.put("eventKey", child.getKey());
                            eventInfo.put("name", event.getName());
                            eventInfo.put("description", event.getAbout());
                            eventInfo.put("startDate", event.getDate());
                            eventInfo.put("startTime", event.getStartTime());
                            // Add event info hashmap to the events arraylist
                            eventsList.add(eventInfo);
                        }
                    }

                    // Comparator for comparing and sorting dates for the events
                    Comparator eventDatesAndTimesComparator = new Comparator<HashMap<String, String>>() {
                        @Override
                        public int compare(HashMap<String, String> event1, HashMap<String, String> event2) {
                            String[] startMDY1 = event1.get("startDate").split("/");
                            String[] startMDY2 = event2.get("startDate").split("/");
                            int year1 = Integer.parseInt(startMDY1[2]);
                            int year2 = Integer.parseInt(startMDY2[2]);
                            if(year1 < year2)
                                return -1; // first event date is before the second event
                            else if(year1 > year2)
                                return 1; // second event date is before the first event
                            else { // years are equal --> check months
                                int month1 = Integer.parseInt(startMDY1[0]);
                                int month2 = Integer.parseInt(startMDY2[0]);
                                if (month1 < month2)
                                    return -1;
                                else if(month1 > month2)
                                    return 1;
                                else{ // months are equal --> check days
                                    int day1 = Integer.parseInt(startMDY1[1]);
                                    int day2 = Integer.parseInt(startMDY2[1]);
                                    if(day1 < day2)
                                        return -1;
                                    else if(day1 > day2)
                                        return 1;
                                    else{ // days are equal --> check times
                                        return 0;

                                    }
                                }
                            }

                        }
                    };

                    /* Sort the events in the events list by date using the comparator
                     https://docs.oracle.com/javase/7/docs/api/java/util/Collections.html#sort(java.util.List)
                     The documentation says it uses a mergesort implementation ^
                     --> Runtime of O(n * log n) worst case, or almost O(n) when mostly sorted */
                    Collections.sort(eventsList, eventDatesAndTimesComparator);

                    // Find listview from layout and initialize with an adapter
                    final ListView listView = (ListView) findViewById(R.id.events_listview);
                    simpleAdapter = new SimpleAdapter(EventListActivity.this, eventsList, R.layout.events_list_items,
                            new String[]{"startDate", "startTime", "name", "description"},
                            new int[]{R.id.start_date, R.id.start_time, R.id.event_name, R.id.event_about});
                    listView.setAdapter(simpleAdapter);

                    // Show EventViewActivity when clicked
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String eventKey = eventsList.get(position).get("eventKey");
                            Intent fullEventInfo = new Intent(EventListActivity.this, EventInfoActivity.class);
                            fullEventInfo.putExtra("eventKey", eventKey);
                            startActivity(fullEventInfo);
                        }
                    });
                }
            }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
    }

    // returns true if event end date is before the current time, or false otherwise
    private boolean eventHasEnded(Event event){
        int endMonth, endDay, endYear;
        if(event.getEndDate() != null) {
            String[] mdy = event.getEndDate().split("/");
            endMonth = Integer.parseInt(mdy[0]);
            endDay = Integer.parseInt(mdy[1]);
            endYear = Integer.parseInt(mdy[2]);
        }
        else{
            String[] mdy = event.getDate().split("/");
            endMonth = Integer.parseInt(mdy[0]);
            endDay = Integer.parseInt(mdy[1]);
            endYear = Integer.parseInt(mdy[2]);
        }

        String hourMin[] = event.getEndTime().split("[: ]");
        int hour = hourMin[2].equals("pm")?
                // If pm, add 12 to the hour to convert to 24 hour format
                Integer.parseInt(hourMin[0])+ 12 : Integer.parseInt(hourMin[0]);
        for(String elem : hourMin)
            Log.d("hourMin", elem);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(endYear, endMonth, endDay);
        return false;
    }


    @Override
    protected void onResume() {
        super.onResume();
        bottomBar.setDefaultTab(R.id.tab_alerts);
    }


}