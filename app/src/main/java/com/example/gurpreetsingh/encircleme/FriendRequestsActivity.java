package com.example.gurpreetsingh.encircleme;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class FriendRequestsActivity extends AppCompatActivity {
    FirebaseDatabase firebaseDatabase;
    DatabaseReference dbRef;
    DatabaseReference dbUserFriendRequests;
    FirebaseAuth auth;

    private String currentUserID;
    private String currentUserUsername;

    final ArrayList<HashMap<String, String>> requestsList = new ArrayList<HashMap<String, String>>();
    final ArrayList<String> userIDList = new ArrayList<String>();
    final ArrayList<String> usernamesList = new ArrayList<String>();

    ImageView neutralFace;

    TextView noRequestsTextView, findFriendsTextView;
    Button findFriendsButton;

    private long numOfFriendRequests;
    private boolean friendRequestsLoaded = false;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        Toolbar toolbar = (Toolbar) findViewById(R.id.friend_requests_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Friend Requests");

        neutralFace = (ImageView) findViewById(R.id.neutral_face_icon);
        noRequestsTextView = (TextView) findViewById(R.id.no_users_found_textview);
        findFriendsTextView = (TextView) findViewById(R.id.find_friends_textview);
        findFriendsButton = (Button) findViewById(R.id.find_friends_button);

        listView = (ListView) findViewById(R.id.friend_requests_listview);

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRef = firebaseDatabase.getReference();    // root of DB

        dbUserFriendRequests = firebaseDatabase.getReference("friend_requests/" + currentUserID);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Remove any previously stored friend requests from ArrayList
        requestsList.clear();
        // Reload friend requests
        checkForFriendRequests(); // called in onResume() to prevent calling twice if it were placed in onCreate()
    }


    // Loads the number of friend requests already in the DB and displays/hides the "find friends" views
    private void checkForFriendRequests(){
        dbUserFriendRequests.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                numOfFriendRequests = dataSnapshot.getChildrenCount();
                if(numOfFriendRequests > 0) {
                    // Friend requests exist --> hide "find friends" views and load all requests
                    findFriendsButton.setVisibility(View.GONE);
                    findFriendsTextView.setVisibility(View.GONE);
                    noRequestsTextView.setVisibility(View.GONE);
                    neutralFace.setVisibility(View.GONE);
                    // Load info for each request
                    loadAllFriendRequests();
                }
                else{
                    // No friend requests --> show "find friends" view
                    findFriendsButton.setVisibility(View.VISIBLE);
                    findFriendsTextView.setVisibility(View.VISIBLE);
                    noRequestsTextView.setVisibility(View.VISIBLE);
                    neutralFace.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("onCancelled", "database error" + databaseError.getMessage());
            }
        });
    }

    // Load all friend requests from DB and display in list view
    private void loadAllFriendRequests() {
        /*
        dbUserFriendRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { */
        dbUserFriendRequests.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                HashMap<String, String> requestInfo = new HashMap<String, String>();
                String userID = dataSnapshot.getKey();
                String username = dataSnapshot.getValue().toString();
                requestInfo.put("userID", userID);
                requestInfo.put("username", username);
                requestsList.add(requestInfo);
                userIDList.add(userID);
                usernamesList.add(username);
                if(!friendRequestsLoaded) {
                    // Don't show listview until all requests loaded
                    if (requestsList.size() == numOfFriendRequests) {
                        friendRequestsLoaded = true;
                        displayRequestsInListView();
                    }
                }else{
                    // All requests already loaded --> update list view
                    displayRequestsInListView();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // Remove request from list
                HashMap<String, String> requestInfo = new HashMap<String, String>();
                String userID = dataSnapshot.getKey();
                String username = dataSnapshot.getValue().toString();
                requestInfo.put("userID", userID);
                requestInfo.put("username", username);
                requestsList.remove(requestInfo);
                // Update list view
                displayRequestsInListView();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


               /* final ArrayList<HashMap<String, String>> requestsList = new ArrayList<HashMap<String,String>>();
                final ArrayList<String> userIDList = new ArrayList<String>();
                final ArrayList<String> usernamesList = new ArrayList<String>();

                ImageView neutralFace = (ImageView) findViewById(R.id.neutral_face_icon);
                TextView noRequestsTextView = (TextView) findViewById(R.id.no_users_found_textview);
                TextView findFriendsTextView = (TextView) findViewById(R.id.find_friends_textview);
                Button findFriendsButton = (Button) findViewById(R.id.find_friends_button);

                // Check if user has any friends
                if(dataSnapshot.hasChildren()) {
                    for (DataSnapshot request : dataSnapshot.getChildren()) {
                        HashMap<String, String> requestInfo = new HashMap<String, String>();
                        String userID = request.getKey();
                        String username = request.getValue().toString();
                        requestInfo.put("userID", userID);
                        requestInfo.put("username", username);
                        requestsList.add(requestInfo);
                        userIDList.add(userID);
                        usernamesList.add(username);
                    }
                    // Hide the "no requests" views
                    neutralFace.setVisibility(View.GONE);
                    noRequestsTextView.setVisibility(View.GONE);
                    findFriendsTextView.setVisibility(View.GONE);
*/

   private void displayRequestsInListView() {
       // Find listview from layout and initialize with an adapter
       SimpleAdapter simpleAdapter = new SimpleAdapter(FriendRequestsActivity.this, requestsList,
               R.layout.friend_requests_list_items, new String[]{"username"}, new int[]{R.id.friend_requests_text_view});
       listView.setAdapter(simpleAdapter);

       // Handle click events on each list item
       listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               final int itemPosition = position;
               final String otherUserID = requestsList.get(position).get("userID");
               final String otherUserName = requestsList.get(position).get("username");

               // Redirect to ViewOtherUserProfileActivity where they can accept the friend request
               Intent showProfile = new Intent(FriendRequestsActivity.this, ViewOtherUserProfileActivity.class);
               showProfile.putExtra("userID", otherUserID);
               startActivity(showProfile);

               /* AlertDialog.Builder acceptDeclineDialog = new AlertDialog.Builder(FriendRequestsActivity.this);

               // User accepts friend request
               acceptDeclineDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       // Retrieve current user's username
                       DatabaseReference usernamesRef = firebaseDatabase.getReference("usernames");
                       Log.d("usernamesRef.toString()", usernamesRef.toString());
                       usernamesRef.orderByValue().equalTo(currentUserID)
                               .addListenerForSingleValueEvent(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                       Log.d("onDataChange", "about to check for dataSnapshot.exists()");
                                       if (dataSnapshot.hasChildren()) {
                                           Log.d("onDataChange", "snapshot exists!");

                                           for (DataSnapshot user : dataSnapshot.getChildren())
                                               // retrieve username
                                               currentUserUsername = user.getKey();
                                           Log.d("onDataChange", "stored username to variable");

                                           // Update DB in both users' "friends" location and delete the request
                                           DatabaseReference updatesRef = firebaseDatabase.getReference();
                                           HashMap<String, Object> friendsUpdates = new HashMap<String, Object>();
                                           friendsUpdates.put("friends/" + currentUserID + "/" + otherUserID, otherUserName);
                                           friendsUpdates.put("friends/" + otherUserID + "/" + currentUserID, currentUserUsername);
                                           friendsUpdates.put("friend_requests/" + currentUserID + "/" + otherUserID, null);    // null value deletes the request
                                           updatesRef.updateChildren(friendsUpdates, new DatabaseReference.CompletionListener() {
                                               @Override
                                               public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                   Toast.makeText(FriendRequestsActivity.this, "You are now friends!", Toast.LENGTH_LONG).show();

                                                   // Remove request from list and refresh the listview
                                                   requestsList.remove(itemPosition);
                                                   SimpleAdapter simpleAdapter = new SimpleAdapter(FriendRequestsActivity.this, requestsList,
                                                           R.layout.friend_requests_list_items, new String[]{"username"}, new int[]{R.id.friend_requests_text_view});
                                                   listView.setAdapter(simpleAdapter);
                                               }
                                           });
                                       }
                                   }

                                   @Override
                                   public void onCancelled(DatabaseError databaseError) {
                                       Toast.makeText(FriendRequestsActivity.this, "Database Error", Toast.LENGTH_LONG).show();
                                   }
                               });
                   }
               });

               // User declines friend request
               acceptDeclineDialog.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       // Delete friend request from DB
                       DatabaseReference friendRequestsRef = firebaseDatabase.getReference("friend_requests");
                       friendRequestsRef.child(currentUserID).child(otherUserID).removeValue(new DatabaseReference.CompletionListener() {
                           @Override
                           public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                               Toast.makeText(FriendRequestsActivity.this, "Friend request removed.", Toast.LENGTH_LONG).show();

                               // Remove request from list and refresh the listview
                               requestsList.remove(itemPosition);
                               SimpleAdapter simpleAdapter = new SimpleAdapter(FriendRequestsActivity.this, requestsList,
                                       R.layout.friend_requests_list_items, new String[]{"username"}, new int[]{R.id.friend_requests_text_view});
                               listView.setAdapter(simpleAdapter);
                           }
                       });
                   }
               });
               acceptDeclineDialog.setMessage("Accept friend request from " + usernamesList.get(position) + "?");
               acceptDeclineDialog.create();
               acceptDeclineDialog.show(); */
           }
       });
   }
                   /*
                }
                else{
                    // No friend requests in DB
                    // Toast.makeText(FriendRequestsActivity.this, "No friend requests right now", Toast.LENGTH_LONG).show();
                    // Show "no requests" views
                    neutralFace.setVisibility(View.VISIBLE);
                    noRequestsTextView.setVisibility(View.VISIBLE);
                    findFriendsTextView.setVisibility(View.VISIBLE);
                    findFriendsButton.setVisibility(View.VISIBLE);

                    findFriendsButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(FriendRequestsActivity.this, AddFriendSearchActivity.class));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(FriendRequestsActivity.this, "Database Error", Toast.LENGTH_LONG).show();
            }
        });

    } */


    private void removeRequestFromListView(){

    }
}
