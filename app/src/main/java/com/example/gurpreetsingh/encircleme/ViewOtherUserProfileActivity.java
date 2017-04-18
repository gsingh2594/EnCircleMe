package com.example.gurpreetsingh.encircleme;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log;

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

import java.util.ArrayList;
import java.util.HashMap;

public class ViewOtherUserProfileActivity extends AppCompatActivity {
    FirebaseDatabase database;
    FirebaseAuth auth;
    String userID;
    String currentUserID;

    DatabaseReference dbRef;
    DatabaseReference dbUserRef;
    DatabaseReference friendsRef;

    private FirebaseStorage fbStorage;
    private StorageReference fbStorageRef;
    private static final long ONE_MEGABYTE = 1024 * 1024;

    private TextView profileName;
    private TextView profileBio;
    private ImageView addFriendIcon, alreadyFriendsIcon;
    private byte[] profileImageBytes;
    private byte[] coverImageBytes;
    private ImageView profileImage;
    private ImageView coverImage;


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
        fbStorage = FirebaseStorage.getInstance();

        profileName = (TextView) findViewById(R.id.user_profile_name);
        profileBio = (TextView) findViewById(R.id.user_profile_bio);
        addFriendIcon = (ImageView) findViewById(R.id.add_friend_icon);
        alreadyFriendsIcon = (ImageView) findViewById(R.id.already_friends_icon);
        profileImage = (ImageView) findViewById(R.id.profile_image);
        coverImage = (ImageView) findViewById(R.id.cover_image);

        loadUserProfile();
        loadUserProfileImage();
        loadUserCoverImage();

        // Check if current user is already friends with this user
        friendsRef = database.getReference("friends");
        friendsRef.child(currentUserID).orderByKey().equalTo(userID)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                        // Current user is already friends with this user
                        showAsFriend();
                    else
                        // Current user is not friends with this user -> Show as addable friend
                        showAsAddableFriend();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(ViewOtherUserProfileActivity.this, "Database error"
                            + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
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
/*                            case R.id.action_alerts:
                                Intent events = new Intent(getApplicationContext(), SearchActivity.class);
                                startActivity(events);
                                break;
                        */}
                return false;
            }
        });
    }

    // Load user profile from DB
    private void loadUserProfile(){
        final LinearLayout interestsLinearLayout = (LinearLayout) findViewById(R.id.interests_linearlayout);
        dbUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                profileName.setText(user.getName());
                if(user.getBio()!=null)
                    profileBio.setText(user.getBio());
                if(user.getInterests()!= null) {
                    ArrayList<String> userInterests = user.getInterests();
                    for (int i = 0; i < userInterests.size(); i++) {
                        // Create and add a new TextView to the LinearLayout
                        TextView interestTextView = new TextView(ViewOtherUserProfileActivity.this);
                        int marginSize = convertDPtoPX(5);
                        LinearLayout.LayoutParams layoutParams =
                                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        layoutParams.setMargins(marginSize, marginSize, marginSize, marginSize);
                        interestTextView.setLayoutParams(layoutParams);
                        interestTextView.setTextSize(16);
                        interestTextView.setText(userInterests.get(i));
                        interestTextView.setElevation((float) convertDPtoPX(4));
                        int paddingSize = convertDPtoPX(20);
                        interestTextView.setPadding(paddingSize, paddingSize, paddingSize, paddingSize);
                        interestTextView.setBackgroundColor(Color.WHITE);
                        interestsLinearLayout.addView(interestTextView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ViewOtherUserProfileActivity.this, "Data could not be retrieved", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // load user profile image from Firebase Storage
    private void loadUserProfileImage() {
        Log.d("load profile pic", "about to load from storage");
        StorageReference profilePicStorageRef = fbStorage.getReference("profile_images/" + userID);
        profilePicStorageRef.getBytes(ONE_MEGABYTE * 5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // user has profile pic --> display it
                Log.d("loadUserProfileImage()", "getBytes successful");
                profileImageBytes = bytes;
                Log.d("loadUserProfileImage()", "convert bytes to bitmap");
                Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.length);
                profileImage.setImageBitmap(profileImageBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // User has not set profile image
                Log.d("loadUserProfileImage()", "Firebase storage exception " + exception.getMessage());
                Toast.makeText(ViewOtherUserProfileActivity.this, "No profile image", Toast.LENGTH_LONG).show();
            }
        });
    }


    // load user cover image from Firebase Storage
    private void loadUserCoverImage() {
        Log.d("load profile pic", "about to load from storage");
        StorageReference profilePicStorageRef = fbStorage.getReference("cover_images/" + userID);
        profilePicStorageRef.getBytes(ONE_MEGABYTE * 5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // user has profile pic --> display it
                Log.d("loadUserCoverImage()", "getBytes successful");
                coverImageBytes = bytes;
                Log.d("loadUserCoverImage()", "convert bytes to bitmap");
                Bitmap coverImageBitmap = BitmapFactory.decodeByteArray(coverImageBytes, 0, coverImageBytes.length);
                coverImage.setImageBitmap(coverImageBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // User has not set cover image
                Log.d("loadUserProfileImage()", "Firebase storage exception " + exception.getMessage());
                Toast.makeText(ViewOtherUserProfileActivity.this, "No cover image", Toast.LENGTH_LONG).show();
            }
        });
    }


    // Method to display the user profile with an addFriendIcon
    private void showAsAddableFriend(){
        addFriendIcon.setVisibility(View.VISIBLE);
        // add friend option when clicked
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
    }



    // Method to display user profile as a current friend, and option to remove friend
    private void showAsFriend(){
        alreadyFriendsIcon.setVisibility(View.VISIBLE);
        // remove friend option when clicked
        alreadyFriendsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder deleteFriendDialog = new AlertDialog.Builder(ViewOtherUserProfileActivity.this);
                deleteFriendDialog.setMessage("Do you want to remove this friend?");
                deleteFriendDialog.setNegativeButton("Keep", new AlertDialog.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User keeps friend -> do nothing
                    }
                });
                deleteFriendDialog.setPositiveButton("Remove", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User removes friend
                        // multipath update to delete the user from both user's friends locations in DB
                        HashMap<String, Object>deleteFriendUpdates = new HashMap<String, Object>();
                        deleteFriendUpdates.put(currentUserID + "/" + userID, null);
                        deleteFriendUpdates.put(userID + "/" + currentUserID, null);

                        friendsRef.updateChildren(deleteFriendUpdates, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(ViewOtherUserProfileActivity.this, "Friend removed!", Toast.LENGTH_LONG).show();

                                // Change the icon to show that the user is no longer a friend
                                alreadyFriendsIcon.setVisibility(View.GONE);
                                addFriendIcon.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                deleteFriendDialog.show();
            }
        });
    }



    // Method to update the friend_requests in the DB
    private void sendFriendRequest(){
        final DatabaseReference friendRequestsRef = database.getReference("friend_requests");
        Log.d("onDataChange", "about to check if friend request exists");

        // First, check if there is already a pending friend request in DB
        friendRequestsRef.child(userID).child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // Request already exists -> Inform user with an alert dialog
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ViewOtherUserProfileActivity.this);
                    alertBuilder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertBuilder.setMessage("You have already sent this user a friend request!");
                    alertBuilder.show();
                }
                else{
                    // No request exists -> Save request in DB
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


    // used to set sizes in dp units programmatically. (Some views set sizes programmtically in px, not dp)
    // We should use this method to make certain views display consistently on different screen densities
    private int convertDPtoPX(int sizeInDP){
        float scale = getResources().getDisplayMetrics().density;       // note that 1dp = 1px on a 160dpi screen
        int dpAsPixels = (int) (sizeInDP * scale + 0.5f);
        return dpAsPixels;  // return the size in pixels
    }
}
