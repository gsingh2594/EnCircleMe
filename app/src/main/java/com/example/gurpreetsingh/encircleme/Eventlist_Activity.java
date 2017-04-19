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
import java.util.HashMap;

public class Eventlist_Activity extends AppCompatActivity {


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
        setContentView(R.layout.event_list);

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
                    Intent events = new Intent(getApplicationContext(), Eventlist_Activity.class);
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
        final ArrayList<HashMap<String, String>> EventsList = new ArrayList<HashMap<String,String>>();
        DatabaseReference eventsRef = database.getReference("events/all_events");
        eventsRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    // User has friends -> store the info for each friend
                    for(DataSnapshot event : dataSnapshot.getChildren()){
                        HashMap<String, String> eventInfo = new HashMap<String, String>();
                        eventInfo.put("userID", event.getKey());
                        eventInfo.put("name", event.getValue().toString());
                        EventsList.add(eventInfo);
                    }

                    // Find listview from layout and initialize with an adapter
                    // Find listview from layout and initialize with an adapter
                    final ListView listView = (ListView) findViewById(R.id.friends_listview);
                    simpleAdapter = new SimpleAdapter(Eventlist_Activity.this, EventsList,
                            R.layout.friend_requests_list_items, new String[]{"name"}, new int[]{R.id.friend_requests_text_view});
                    listView.setAdapter(simpleAdapter);

                    // Show friend's user profile when clicked
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String otherUserID = EventsList.get(position).get("userID");
                            Intent fullEventInfo = new Intent(Eventlist_Activity.this, FullEventInfo.class);
                            fullEventInfo.putExtra("userID", otherUserID);
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

    @Override
    protected void onResume() {
        super.onResume();
        bottomBar.setDefaultTab(R.id.tab_alerts);
    }


}