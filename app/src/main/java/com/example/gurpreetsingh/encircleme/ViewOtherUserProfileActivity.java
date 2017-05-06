package com.example.gurpreetsingh.encircleme;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
    private TextView profileUsername;
    private TextView profileEmail;
    private TextView profilePhone;
    private ImageView addFriendIcon, alreadyFriendsIcon, acceptFriendRequestIcon;
    private CardView emailCard, phoneCard;
    private byte[] profileImageBytes;
    private byte[] coverImageBytes;
    private ImageView profileImage;
    private ImageView coverImage;
    private BottomBar bottomBar;
    private OnTabSelectListener tabSelectListener;


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
        profileEmail = (TextView) findViewById(R.id.email);
        profilePhone = (TextView) findViewById(R.id.phone_number);
        addFriendIcon = (ImageView) findViewById(R.id.add_friend_icon);
        alreadyFriendsIcon = (ImageView) findViewById(R.id.already_friends_icon);
        acceptFriendRequestIcon = (ImageView)findViewById(R.id.accept_friend_request_icon);
        profileImage = (ImageView) findViewById(R.id.profile_image);
        coverImage = (ImageView) findViewById(R.id.cover_image);
        profileUsername = (TextView) findViewById(R.id.username);

        emailCard = (CardView) findViewById(R.id.card_email);
        phoneCard = (CardView) findViewById(R.id.card_phone);

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
                            // Current user is not friends with this user
                            // Check if current user has a pending friend request from this user
                            checkForPendingFriendRequest();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(ViewOtherUserProfileActivity.this, "Database error"
                                + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Load user profile from DB
    private void loadUserProfile(){
        final LinearLayout interestsLinearLayout = (LinearLayout) findViewById(R.id.interests_linearlayout);
        final LinearLayout interestsLinearLayout2 = (LinearLayout) findViewById(R.id.interests_linearlayout2);
        dbUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                profileName.setText(user.getName());
                profileEmail.setText(user.getEmail());
                profilePhone.setText(user.getPhone());
                profileUsername.setText("Username: " + user.getUsername());
                if(user.getBio()!=null)
                    profileBio.setText(user.getBio());
                if(user.getInterests()!= null) {
                    ArrayList<String> userInterests = user.getInterests();
                    for (int i = 0; i < userInterests.size(); i++) {
                        // Create and add a new TextView to the LinearLayout
                        ImageView interestImageView= new ImageView(ViewOtherUserProfileActivity.this);
                        interestImageView.getMeasuredHeight();
                        int marginSize = convertDPtoPX(1);
                        RelativeLayout.LayoutParams layoutParams =
                                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(marginSize, marginSize, marginSize, marginSize);
                        interestImageView.setLayoutParams(layoutParams);
                        /*interestImageView.setTextSize(15);
                        interestImageView.setTextColor(Color.BLACK);
                        interestImageView.setText(userInterests.get(i));*/
                        interestImageView.setElevation((float) convertDPtoPX(2));
                        int paddingSize = convertDPtoPX(10);
                        interestImageView.setPadding(paddingSize, paddingSize, paddingSize, paddingSize);
                        interestImageView.setBackgroundColor(Color.WHITE);
                        switch (userInterests.get(i)) {
                            case "Movie Theatres":
                                interestImageView.setBackgroundResource(R.drawable.movies);
                                break;
                            case "Art Gallery":
                                interestImageView.setBackgroundResource(R.drawable.artgallery);
                                break;
                            case "Cafe":
                                interestImageView.setBackgroundResource(R.drawable.cafe);
                                break;
                            case "Bars":
                                interestImageView.setBackgroundResource(R.drawable.bars);
                                break;
                            case "Restaurants":
                                interestImageView.setBackgroundResource(R.drawable.restaurants);
                                break;
                            case "Department Stores":
                                interestImageView.setBackgroundResource(R.drawable.deptstores);
                                break;
                        }
                        if (i < 3){
                            interestsLinearLayout.addView(interestImageView);
                        }
                        else
                            interestsLinearLayout2.addView(interestImageView);
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

    // Check to see if current user has received a pending friend request from this user
    private void checkForPendingFriendRequest(){
        DatabaseReference friendRequestsRef = database.getReference("friend_requests/" + currentUserID + "/" + userID);
        friendRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    // Pending friend request exists --> Display icon for accepting it
                    showAsAcceptableFriendRequest();
                }
                else
                    // No friend request --> show as an addable friend (can send a friend request)
                    // because user is not a friend and no pending friend request received
                    showAsAddableFriend();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // Method to display the user profile with an addFriendIcon for sending a friend request
    private void showAsAddableFriend(){
        // Hide other icons if visible
        acceptFriendRequestIcon.setVisibility(View.GONE);
        alreadyFriendsIcon.setVisibility(View.GONE);
        emailCard.setVisibility(View.GONE);
        phoneCard.setVisibility(View.GONE);
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
        //Hide other icons if visible
        addFriendIcon.setVisibility(View.GONE);
        acceptFriendRequestIcon.setVisibility(View.GONE);
        emailCard.setVisibility(View.VISIBLE);
        phoneCard.setVisibility(View.VISIBLE);
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

                                /*// Change the icon to show that the user is no longer a friend
                                alreadyFriendsIcon.setVisibility(View.GONE);
                                addFriendIcon.setVisibility(View.VISIBLE); */      // visibility handled in showAsAddableFriend()
                                showAsAddableFriend(); // Show as addable friend to send a friend request again
                            }
                        });
                    }
                });
                deleteFriendDialog.show();
            }
        });
    }


    private void showAsAcceptableFriendRequest() {
        acceptFriendRequestIcon.setVisibility(View.VISIBLE);
        acceptFriendRequestIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder acceptDeclineDialog = new AlertDialog.Builder(ViewOtherUserProfileActivity.this);

                // User accepts friend request
                acceptDeclineDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Retrieve current user's username
                        DatabaseReference usernamesRef = database.getReference("usernames");
                        Log.d("usernamesRef.toString()", usernamesRef.toString());
                        usernamesRef.orderByChild("id").equalTo(currentUserID)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String currentUsername = "";
                                        Log.d("onDataChange", "about to check for dataSnapshot.exists()");
                                        if (dataSnapshot.hasChildren()) {
                                            Log.d("onDataChange", "snapshot exists!");

                                            for (DataSnapshot user : dataSnapshot.getChildren())
                                                // retrieve username
                                                currentUsername = user.getKey();

                                            final String currentUsernameFinal = currentUsername;
                                            Log.d("onDataChange", "stored username to variable");

                                            // Retrieve the viewed user's username
                                            DatabaseReference otherUsernameRef = database.getReference("usernames");
                                            otherUsernameRef.orderByChild("id").equalTo(userID)
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        String otherUserName;

                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.hasChildren()) {
                                                                Log.d("onDataChange", "snapshot exists!");
                                                                for (DataSnapshot user : dataSnapshot.getChildren())
                                                                    // store the other username
                                                                    otherUserName = user.getKey();

                                                                // Update DB in both users' "friends" location and delete the request
                                                                DatabaseReference updatesRef = database.getReference();
                                                                HashMap<String, Object> friendsUpdates = new HashMap<String, Object>();
                                                                friendsUpdates.put("friends/" + currentUserID + "/" + userID, otherUserName);
                                                                friendsUpdates.put("friends/" + userID + "/" + currentUserID, currentUsernameFinal);
                                                                friendsUpdates.put("friend_requests/" + currentUserID + "/" + userID, null);    // null value deletes the request
                                                                updatesRef.updateChildren(friendsUpdates, new DatabaseReference.CompletionListener() {
                                                                    @Override
                                                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                                        Toast.makeText(ViewOtherUserProfileActivity.this, "You are now friends!", Toast.LENGTH_LONG).show();
                                                                        // Show as a friend now that the request is accepted
                                                                        showAsFriend();
                                                                    }
                                                                });
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                        } else {
                                            Log.d("retrieve username", "could not load usernme");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }
                });

                // User declines friend request
                acceptDeclineDialog.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Delete friend request from DB
                        DatabaseReference friendRequestsRef = database.getReference("friend_requests");
                        friendRequestsRef.child(currentUserID).child(userID).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                Toast.makeText(ViewOtherUserProfileActivity.this, "Friend request removed.", Toast.LENGTH_LONG).show();
                                // Show as addable friend to send a friend request
                                showAsAddableFriend();

                            }
                        });
                    }
                });
                acceptDeclineDialog.setMessage("Accept friend request from " + profileName.getText() + "?");
                acceptDeclineDialog.create();
                acceptDeclineDialog.show();
            }
        });
    }


    // Method to update the friend_requests in the DB
    private void sendFriendRequest(){
        final DatabaseReference friendRequestsRef = database.getReference("friend_requests");
        Log.d("onDataChange", "about to check if friend request exists");

        // First, check if a friend request has already been sent to this user and is pending (still stored in DB)
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
                    usernamesRef.orderByChild("id").equalTo(currentUserID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.d("onDataChange", "about to check for dataSnapshot.exists()");
                                    if (dataSnapshot.hasChildren()) {
                                        Log.d("onDataChange", "snapshot exists!");
                                        // variable to store current user's username
                                        String currentUserUsername = null;

                                        for(DataSnapshot user :  dataSnapshot.getChildren())
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
