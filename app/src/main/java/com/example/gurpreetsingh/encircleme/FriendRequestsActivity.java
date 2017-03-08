package com.example.gurpreetsingh.encircleme;

import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FriendRequestsActivity extends AppCompatActivity {
    FirebaseDatabase firebaseDatabase;
    DatabaseReference dbRef;
    DatabaseReference dbUserFriendRequests;
    FirebaseAuth auth;

    private String currentUserID;
    private String currentUserUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_requests);

        Toolbar toolbar = (Toolbar) findViewById(R.id.friend_requests_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Friend Requests");

        auth =FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRef = firebaseDatabase.getReference();    // root of DB
        dbUserFriendRequests = firebaseDatabase.getReference("friend_requests/" + currentUserID);

        // Load all friend requests from DB
        dbUserFriendRequests.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ArrayList<HashMap<String, String>> requestsList = new ArrayList<HashMap<String,String>>();
                final ArrayList<String> userIDList = new ArrayList<String>();
                final ArrayList<String> usernamesList = new ArrayList<String>();

                ImageView neutralFace = (ImageView) findViewById(R.id.neutral_face_icon);
                TextView noRequestsTextView = (TextView) findViewById(R.id.no_users_found_textview);

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
                    neutralFace.setVisibility(View.GONE);
                    noRequestsTextView.setVisibility(View.GONE);

                    final ListView listView = (ListView) findViewById(R.id.friend_requests_listview);
                    SimpleAdapter simpleAdapter = new SimpleAdapter(FriendRequestsActivity.this, requestsList,
                            R.layout.friend_requests_list_items, new String[]{"username"}, new int[]{R.id.friend_requests_text_view} );
                    listView.setAdapter(simpleAdapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            final int itemPosition = position;
                            final String otherUserID = requestsList.get(position).get("userID");
                            final String otherUserName = requestsList.get(position).get("username");

                            AlertDialog.Builder acceptDeclineDialog = new AlertDialog.Builder(FriendRequestsActivity.this);

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

                                                for(DataSnapshot user: dataSnapshot.getChildren())
                                                    // retrieve username
                                                    currentUserUsername = user.getKey();
                                                Log.d("onDataChange", "stored username to variable");

                                                // Update DB in both users' friends location
                                                DatabaseReference friendsRef = firebaseDatabase.getReference("friends");
                                                HashMap<String, Object>friendUpdates = new HashMap<String, Object>();
                                                friendUpdates.put(currentUserID + "/" + otherUserID, otherUserName);
                                                friendUpdates.put(otherUserID + "/" + currentUserID, currentUserUsername);
                                                friendsRef.updateChildren(friendUpdates, new DatabaseReference.CompletionListener() {
                                                    @Override
                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                        Toast.makeText(FriendRequestsActivity.this, "You are now friends!", Toast.LENGTH_LONG).show();
                                                        requestsList.remove(itemPosition);
                                                        SimpleAdapter simpleAdapter = new SimpleAdapter(FriendRequestsActivity.this, requestsList,
                                                                R.layout.friend_requests_list_items, new String[]{"username"}, new int[]{R.id.friend_requests_text_view} );
                                                        listView.setAdapter(simpleAdapter);

                                                        //TODO: remove friend request from database after accept
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
                                public void onClick(DialogInterface dialog, int which){
                                    //TODO: handle the declined friend request
                                }
                            });

                            acceptDeclineDialog.create();
                            acceptDeclineDialog.show();
                        }
                    });
                }
                else{
                    Toast.makeText(FriendRequestsActivity.this, "No friend requests right now", Toast.LENGTH_LONG).show();
                    neutralFace.setVisibility(View.VISIBLE);
                    noRequestsTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(FriendRequestsActivity.this, "Database Error", Toast.LENGTH_LONG).show();
            }
        });

    }
}
