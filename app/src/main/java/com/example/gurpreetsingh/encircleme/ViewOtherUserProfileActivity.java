package com.example.gurpreetsingh.encircleme;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewOtherUserProfileActivity extends AppCompatActivity {
    FirebaseDatabase database;
    FirebaseAuth auth;
    DatabaseReference dbRef;
    DatabaseReference dbUserRef;

    private TextView profileName;
    private TextView profileBio;
    private ImageView addFriendIcon;

    String userID;
    String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_other_user_profile);
        Intent receivedIntent = getIntent();
        userID = receivedIntent.getStringExtra("userID");     // userID of other user

        currentUserID = auth.getInstance().getCurrentUser().getUid();// userID of logged in user
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();                             // root directory of DB
        dbUserRef = database.getReference("users/" + userID);

        profileName = (TextView) findViewById(R.id.user_profile_name);
        profileBio = (TextView) findViewById(R.id.user_profile_bio);
        addFriendIcon = (ImageView) findViewById(R.id.add_friend_icon);

        addFriendIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView sendRequestText = new TextView(ViewOtherUserProfileActivity.this);
                sendRequestText.setText("Do you want to send a friend request?");
                sendRequestText.setPadding(65, 0, 0, 0);
                // build the dialog
                AlertDialog.Builder sendRequestDialogBuilder = new AlertDialog.Builder(ViewOtherUserProfileActivity.this);
                sendRequestDialogBuilder.setTitle("Send Friend Request");
                sendRequestDialogBuilder.setView(sendRequestText);
                sendRequestDialogBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.d("Send Request Dialog", "Send button clicked");
                        sendFriendRequest();
                    }
                });
                sendRequestDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                sendRequestDialogBuilder.show();
            }
        });

        dbUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                profileName.setText(user.getName());
                if(user.getBio()!=null)
                    profileBio.setText(user.getBio());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ViewOtherUserProfileActivity.this, "Data could not be retrieved", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void sendFriendRequest(){
        final DatabaseReference friendRequestsRef = database.getReference("friend_requests");
        Log.d("onDataChange", "about to check if friend request exists");

        // First, check if there is already a pending friend request in DB
        friendRequestsRef.child(userID).child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // Request already exists -> Inform user with an alert dialog
                    TextView textView = new TextView(ViewOtherUserProfileActivity.this);
                    textView.setText("You have already sent this user a friend request!");
                    textView.setTextSize(R.dimen.text_large);
                    //textView.setPadding(65,10,0,0);

                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ViewOtherUserProfileActivity.this);
                    alertBuilder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertBuilder.setView(textView);
                    alertBuilder.show();
                }
                else{ // No request exists -> Save request in DB

                    // Retrieve current user's username
                    DatabaseReference usernamesRef = database.getReference("usernames");
                    Log.d("usernamesRef.toString()", usernamesRef.toString());
                    usernamesRef.orderByValue().equalTo(currentUserID)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d("onDataChange", "about to check for dataSnapshot.exists()");
                            if (dataSnapshot.hasChildren()) {
                                Log.d("onDataChange", "snapshot exists!");

                                // variable to store current user's username
                                String currentUserUsername = null;
                                for(DataSnapshot user: dataSnapshot.getChildren())
                                    // retrieve username
                                    currentUserUsername = user.getKey();
                                Log.d("onDataChange", "stored username to variable");

                                if(currentUserUsername!=null) {
                                    /* Save at friend_requests/otherUserID/currentUserID
                                    with the username of the user that sent the request (the current user) */
                                    friendRequestsRef.child(userID).child(currentUserID).setValue(currentUserUsername);
                                    Toast.makeText(ViewOtherUserProfileActivity.this, "Friend request sent!", Toast.LENGTH_LONG).show();
                                }
                            }
                            else{
                                Log.d("dataSnapshot.exists()", "No username for that currentUserID: " + currentUserID );
                                Log.d("dataSnapshot.toString: ", dataSnapshot.toString());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(ViewOtherUserProfileActivity.this, "Database Error", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ViewOtherUserProfileActivity.this, "Friend request sent!", Toast.LENGTH_LONG).show();
            }
        });
    }
}
