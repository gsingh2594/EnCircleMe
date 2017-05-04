package com.example.gurpreetsingh.encircleme;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class FriendsActivity extends AppCompatActivity {
    Button btnAlerts;
    Button btnMaps;
    Button btnProfile;
    Button friends;
    Button btnSetting;

    ImageView neutralFace;
    TextView noFriendsTextView;
    Button findFriendsButton;

    FirebaseAuth auth;
    String currentUserID;
    FirebaseDatabase database;
    DatabaseReference dbRef;

    long numOfFriends;
    final ArrayList<HashMap<String, String>> friendsList = new ArrayList<HashMap<String,String>>();
    final ArrayList<String> friendsIDList = new ArrayList<>();
    final HashMap<String, HashMap<String, String>> friendsMap = new HashMap<>();
    final HashMap<String, String> usernameToIDMap = new HashMap<>();
    final ArrayList<HashMap<String, String>> friendsNameUsernameList = new ArrayList<>();

    SimpleAdapter simpleAdapter;
    byte[] profileImageBytes;
    ArrayList<UserWithImage> userWithImageList = new ArrayList<>();
    Comparator userWithImageComparator;

    private BottomBar bottomBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.friends_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("My Friends");

/*
        Profile();
        Friends();
        Alerts();
        Maps();
        Settings();
*/

        neutralFace = (ImageView) findViewById(R.id.neutral_face_icon);
        noFriendsTextView = (TextView) findViewById(R.id.no_friends_textview);
        findFriendsButton = (Button) findViewById(R.id.find_friends_button);

        currentUserID = auth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_friends);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_profile) {
                    Intent profile = new Intent(getApplicationContext(), UserProfileActivity.class);
                    startActivity(profile);
                /*} else if (tabId == R.id.tab_friends) {
                    Intent friends = new Intent(getApplicationContext(), FriendsActivity.class);
                    startActivity(friends);*/
                } else if (tabId == R.id.tab_map) {
                    Intent map = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(map);
                } else if (tabId == R.id.tab_alerts) {
                    Intent events = new Intent(getApplicationContext(), EventsTabActivity.class);
                    startActivity(events);
                } else if (tabId == R.id.tab_chats) {
                    Intent events = new Intent(getApplicationContext(), ChatActivity.class);
                    startActivity(events);
                }

            }
        });
        /*BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_profile:
                        Intent profile = new Intent(getApplicationContext(), UserProfileActivity.class);
                        startActivity(profile);
                        break;
                    case R.id.action_friends:
                        Intent friends = new Intent(getApplicationContext(), FriendsActivity.class);
                        startActivity(friends);
                        break;
                    case R.id.action_map:
                        Intent map = new Intent(getApplicationContext(), MapsActivity.class);
                        startActivity(map);
                        break;
*//*                            case R.id.action_alerts:
                                Intent events = new Intent(getApplicationContext(), SearchActivity.class);
                                startActivity(events);
                                break;
                        *//*}
                return false;
            }
        });*/
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
        // Clear all elements in the lists so they may be reloaded
        friendsMap.clear();
        usernameToIDMap.clear();
        friendsIDList.clear();
        friendsList.clear();
        friendsNameUsernameList.clear();
        userWithImageList.clear();

        Log.d("loadFriendsList", "method started");
        DatabaseReference friendsRef = database.getReference("friends");
        friendsRef.child(currentUserID).orderByValue().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    numOfFriends = dataSnapshot.getChildrenCount();
                    Log.d("numOfFriends", Long.toString(numOfFriends));
                    // User has friends -> store the info for each friend
                    for(DataSnapshot friend : dataSnapshot.getChildren()){
                        if(!friendsMap.containsKey(friend.getKey())) {
                            // Friend has not been loaded yet --> store the info
                            HashMap<String, String> friendInfo = new HashMap<String, String>();
                            String userID = friend.getKey();
                            String username = friend.getValue().toString();
                            friendInfo.put("userID", userID);
                            friendInfo.put("username", username);
                            friendsMap.put(friend.getKey(), friendInfo);
                            friendsIDList.add(userID);
                            friendsList.add(friendInfo);
                            loadUserNameAndID(username);
                            //loadUserProfileImage(userID, username);
                        }
                    }
                    Log.d("loadFriendsList", "current user has friends!");
                    // Hide the "no friends" views
                    neutralFace.setVisibility(View.GONE);
                    noFriendsTextView.setVisibility(View.GONE);
                    findFriendsButton.setVisibility(View.GONE);
                    /*
                    // Find listview from layout and initialize with an adapter
                    final ListView listView = (ListView) findViewById(R.id.friends_listview);
                    simpleAdapter = new SimpleAdapter(FriendsActivity.this, friendsList,
                            R.layout.friend_requests_list_items, new String[]{"username"}, new int[]{R.id.friend_requests_text_view});
                    listView.setAdapter(simpleAdapter);
                    simpleAdapter.notifyDataSetChanged();

                    // Show friend's user profile when clicked
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String otherUserID = friendsList.get(position).get("userID");
                            Intent viewOtherUserProfile = new Intent(FriendsActivity.this, ViewOtherUserProfileActivity.class);
                            viewOtherUserProfile.putExtra("userID", otherUserID);
                            startActivity(viewOtherUserProfile);
                        }
                    }); */
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
                   /* simpleAdapter = new SimpleAdapter(FriendsActivity.this, friendsList,    // friendsList empty in this case
                            R.layout.friend_requests_list_items, new String[]{"username"}, new int[]{R.id.friend_requests_text_view} );
                    listView.setAdapter(simpleAdapter); */
                    listView.setAdapter(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("loadFriendsList", "error loading friends " + databaseError.getMessage());
            }
        });
    }


    // Loads a user's name and id in a NameAndID object for a given username
    private void loadUserNameAndID(final String username){
        Log.d("loadUserNameAndID", "username = " + username);
        DatabaseReference usernamesRef= database.getReference("usernames");
        usernamesRef.orderByKey().equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot result : dataSnapshot.getChildren()) {
                        NameAndID userNameAndID = result.getValue(NameAndID.class);
                        HashMap<String,String> hashMap = new HashMap<>();
                        hashMap.put("nameAndUsername", userNameAndID.getName() + " (" + username + ")");
                        hashMap.put("id", userNameAndID.getID());
                        hashMap.put("username", username);
                        friendsNameUsernameList.add(hashMap);
                        usernameToIDMap.put(username, userNameAndID.getID());
                        Log.d("loadUserNameAndID", "name and ID loaded: " + userNameAndID.toString());
                        if(friendsNameUsernameList.size() == numOfFriends)
                            displaySimpleResultsList(friendsNameUsernameList, friendsIDList);
                        loadUserProfileImage(userNameAndID, username);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // Loads a profile image for a specified userID
    private void loadUserProfileImage(NameAndID userNameAndID, final String username){
        final String userID = userNameAndID.getID();
        final String name = userNameAndID.getName();
        Log.d("loadUserProfileImage", "userID = " + userID + "\nname = " + name + "\nusername = " + username);
        Log.d("load profile pic", "about to load from storage");
        StorageReference profilePicStorageRef = FirebaseStorage.getInstance().getReference("profile_images/" + userID);
        profilePicStorageRef.getBytes(1024*1024*5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // user has profile pic --> display it
                Log.d("loadUserProfileImage()", "getBytes successful");
                profileImageBytes = bytes;
                Log.d("loadUserProfileImage()", "convert bytes to bitmap");
                Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.length);
                //profileImage.setImageBitmap(profileImageBitmap);
                Log.d("Image loaded", "UserWithID: \nuserID = " + userID + "\nname = " + name + "\nusername = " + username);
                UserWithImage uwi = new UserWithImage(userID, username, name, profileImageBitmap);
                userWithImageList.add(uwi);
                Log.d("userWithImageList", "size = " + userWithImageList.size());
                if(userWithImageList.size() == numOfFriends)
                    displayListOfFriends();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // User has not set profile image
                Log.d("loadUserProfileImage()", "Firebase storage exception " + exception.getMessage());
                Toast.makeText(FriendsActivity.this, "No profile image", Toast.LENGTH_LONG).show();
                UserWithImage uwi = new UserWithImage(userID, username, name, null);
                userWithImageList.add(uwi);
                Log.d("userWithImageList", "size = " + userWithImageList.size());
                if(userWithImageList.size() == numOfFriends)
                    displayListOfFriends();
            }
        });
    }


    // populates the listview with results that have a default icon. (No image displayed)
    private void displaySimpleResultsList(final ArrayList<HashMap<String,String>> nameAndUsernameList,
                                          final ArrayList<String> userIDList){
        Log.d("displaySimpleResults", "entering method");

        // Sort user's by name from ArrayList of HashMaps
        Comparator userHashMapComparator = new Comparator<HashMap<String,String>>() {
                @Override
                public int compare(HashMap<String,String> u1, HashMap<String,String> u2) {
                    int result = u1.get("nameAndUsername").compareTo(u2.get("nameAndUsername"));
                    if (result < 0) return -1;
                    else if (result > 0) return 1;
                    else return 0;
                }
            };

        Collections.sort(nameAndUsernameList, userHashMapComparator);


        final ListView listView = (ListView) findViewById(R.id.friends_listview);
        SimpleAdapter simpleAdapter = new SimpleAdapter(FriendsActivity.this, nameAndUsernameList,
                R.layout.friends_search_list_items, new String[]{"nameAndUsername"}, new int[]{R.id.friends_search_text_view} );
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("clicked on item", listView.getItemAtPosition(position).toString());
                HashMap userInfo = nameAndUsernameList.get(position);
                String nameAndUsername = userInfo.get("nameAndUsername").toString();
                String username = userInfo.get("username").toString();
                Log.d("clicked on item", "username: " + username);

                //Toast.makeText(AddFriendSearchActivity.this, "Clicked on " + Integer.toString(view.getId()), Toast.LENGTH_LONG).show();
                Toast.makeText(FriendsActivity.this, "User: " + username, Toast.LENGTH_LONG).show();

                String userID = usernameToIDMap.get(username);
                //String uID = resultsList.get(position).get("username");
                Log.d("userID: ", userID);

                if (userID.equals(currentUserID)) {
                    // user selects themself. Take them to their profile.
                    Intent userProfile = new Intent(FriendsActivity.this, UserProfileActivity.class);
                    startActivity(userProfile);
                } else {
                    // user can view other user profile
                    Intent viewOtherUserProfile = new Intent(FriendsActivity.this, ViewOtherUserProfileActivity.class);
                    viewOtherUserProfile.putExtra("userID", userID);
                    startActivity(viewOtherUserProfile);
                }
            }
        });
    }

    // Sorts the list of users by name with images and displays them in the ListView with an adapter
    private void displayListOfFriends(){
        Log.d("displayListOfFriends()", "entering method");
        // Sort the userWithImages list so that users appear in order of username
        if(userWithImageComparator == null) {
            userWithImageComparator = new Comparator<UserWithImage>() {
                @Override
                public int compare(UserWithImage u1, UserWithImage u2) {
                    int result = u1.getName().compareTo(u2.getName());
                    if (result < 0) return -1;
                    else if (result > 0) return 1;
                    else return 0;
                }
            };
        }
        Log.d("diplayListOfFriends", "sorting userWithImagesList");
        Collections.sort(userWithImageList, userWithImageComparator);

        // Initialize the adapter
        UserProfileListAdapter upla = new UserProfileListAdapter(FriendsActivity.this, userWithImageList);
        final ListView listView = (ListView) findViewById(R.id.friends_listview);
        listView.setAdapter(upla);
        // Add on click listener to view the user's profile
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String otherUserID = userWithImageList.get(position).getUserID();
                Log.d("onItemClick", "position = " + position + "\n name = " + userWithImageList.get(position).getName()
                        + "\nuser ID = " + userWithImageList.get(position).getUserID());
                Intent viewOtherUserProfile = new Intent(FriendsActivity.this, ViewOtherUserProfileActivity.class);
                viewOtherUserProfile.putExtra("userID", otherUserID);
                startActivity(viewOtherUserProfile);
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

    @Override
    protected void onResume() {
        super.onResume();
        bottomBar.setDefaultTab(R.id.tab_friends);
    }


/*    public void Profile() {
        btnProfile = (Button) findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent profile = new Intent(FriendsActivity.this, UserProfileActivity.class);
                profile.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(profile);
            }
        });
    }

    public void Alerts() {
        btnAlerts = (Button) findViewById(R.id.btnAlerts);
        btnAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alerts = new Intent(FriendsActivity.this, PlacePickerActivity.class);
                startActivity(alerts);
            }
        });
    }

    public void Maps() {
        btnMaps = (Button) findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alerts = new Intent(FriendsActivity.this, MapsActivity.class);
                startActivity(alerts);
            }
        });
    }

    public void Friends() {
        friends = (Button) findViewById(R.id.friends);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent offMaps = new Intent(FriendsActivity.this, FriendsActivity.class);
                startActivity(offMaps);
            }
        });
    }

    public void Settings() {
        btnSetting = (Button) findViewById(R.id.setting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent setting = new Intent(FriendsActivity.this, UserActivity.class);
                startActivity(setting);
            }
        });
    }*/
}