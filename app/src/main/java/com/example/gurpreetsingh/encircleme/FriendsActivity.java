package com.example.gurpreetsingh.encircleme;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FriendsActivity extends AppCompatActivity {
    ImageButton btnAlerts;
    ImageButton btnMaps;
    ImageButton btnProfile;
    ImageButton friends;
    ImageButton btnSetting;

    ImageView neutralFace;
    TextView noFriendsTextView;
    Button findFriendsButton;

    FirebaseAuth auth;
    String currentUserID;
    FirebaseDatabase database;
    DatabaseReference dbRef;

    SimpleAdapter simpleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.friends_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("My Friends");

        Profile();
        Friends();
        Alerts();
        Maps();
        Settings();

        neutralFace = (ImageView) findViewById(R.id.neutral_face_icon);
        noFriendsTextView = (TextView) findViewById(R.id.no_friends_textview);
        findFriendsButton = (Button) findViewById(R.id.find_friends_button);

        currentUserID = auth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
    }


    @Override
    protected void onStart(){
        super.onStart();
        Log.d("onStart", "starting the activity");
        loadFriendsList();
        if(simpleAdapter != null)
            simpleAdapter.notifyDataSetChanged();
    }


    // load and display list of the current user's friends
    private void loadFriendsList(){
        Log.d("loadFriensList", "method started");
        final ArrayList<HashMap<String, String>> friendsList = new ArrayList<HashMap<String,String>>();
        DatabaseReference friendsRef = database.getReference("friends");
        friendsRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    // User has friends -> store the info for each friend
                    for(DataSnapshot friend : dataSnapshot.getChildren()){
                        HashMap<String, String> friendInfo = new HashMap<String, String>();
                        friendInfo.put("userID", friend.getKey());
                        friendInfo.put("username", friend.getValue().toString());
                        friendsList.add(friendInfo);
                    }
                    // Hide the "no friends" views
                    neutralFace.setVisibility(View.GONE);
                    noFriendsTextView.setVisibility(View.GONE);
                    findFriendsButton.setVisibility(View.GONE);

                    // Find listview from layout and initialize with an adapter
                    final ListView listView = (ListView) findViewById(R.id.friends_listview);
                    simpleAdapter = new SimpleAdapter(FriendsActivity.this, friendsList,
                            R.layout.friend_requests_list_items, new String[]{"username"}, new int[]{R.id.friend_requests_text_view} );
                    listView.setAdapter(simpleAdapter);

                    // Show friend's user profile when clicked
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String otherUserID = friendsList.get(position).get("userID");
                            Intent viewOtherUserProfile = new Intent(FriendsActivity.this, ViewOtherUserProfileActivity.class);
                            viewOtherUserProfile.putExtra("userID", otherUserID);
                            startActivity(viewOtherUserProfile);
                        }
                    });
                }
                else{
                    // User does not have friends
                    neutralFace.setVisibility(View.VISIBLE);
                    noFriendsTextView.setVisibility(View.VISIBLE);
                    findFriendsButton.setVisibility(View.VISIBLE);
                    findFriendsButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent addFriendSearch = new Intent(FriendsActivity.this, AddFriendSearchActivity.class);
                            startActivity(addFriendSearch);
                        }
                    });
                    // Set listview to be empty to prevent showing friends who have been deleted
                    final ListView listView = (ListView) findViewById(R.id.friends_listview);
                    simpleAdapter = new SimpleAdapter(FriendsActivity.this, friendsList,    // friendsList empty in this case
                            R.layout.friend_requests_list_items, new String[]{"username"}, new int[]{R.id.friend_requests_text_view} );
                    listView.setAdapter(simpleAdapter);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.friends_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_for_friends:
                Intent searchForFriends = new Intent(FriendsActivity.this, AddFriendSearchActivity.class);
                startActivity(searchForFriends);
                return true;
            case R.id.friend_requests_icon:
                Intent viewFriendRequests = new Intent(FriendsActivity.this, FriendRequestsActivity.class);
                startActivity(viewFriendRequests);
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    public void Profile() {
        btnProfile = (ImageButton) findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent time = new Intent(FriendsActivity.this, UserProfileActivity.class);
                startActivity(time);
            }
        });
    }

    public void Alerts() {
        btnAlerts = (ImageButton) findViewById(R.id.btnAlerts);
        btnAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alerts = new Intent(FriendsActivity.this, PlacePickerActivity.class);
                startActivity(alerts);
            }
        });
    }

    public void Maps() {
        btnMaps = (ImageButton) findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alerts = new Intent(FriendsActivity.this, MapsActivity.class);
                startActivity(alerts);
            }
        });
    }

    public void Friends() {
        friends = (ImageButton) findViewById(R.id.friends);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent offMaps = new Intent(FriendsActivity.this, FriendsActivity.class);
                startActivity(offMaps);
            }
        });
    }

    public void Settings() {
        btnSetting = (ImageButton) findViewById(R.id.setting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent setting = new Intent(FriendsActivity.this, UserActivity.class);
                startActivity(setting);
            }
        });
    }
}