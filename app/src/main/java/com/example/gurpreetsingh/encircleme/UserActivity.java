package com.example.gurpreetsingh.encircleme;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class UserActivity extends AppCompatActivity {

    private Button signOutButton;
    private Button resetPassword;
    private Button editProfile;
    private Button notifications;
    private TextView helloUserText;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private String userID;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private DatabaseReference dbUserRef;
    private FirebaseStorage fbStorage;
    private StorageReference fbStorageRef;
    private User user;
    Button btnAlerts;
    Button btnMaps;
    Button btnProfile;
    Button friends;
    Button btnSetting;

    private static final long ONE_MEGABYTE = 1024 * 1024;
    private byte[] profileImageBytes;

    private TextView textProfile;
    private TextView textFriends;
    private TextView textMap;
    private TextView textAlerts;
    private TextView profileName;
    private ImageView profileImage;


/*    //Button
    public void Profile() {
        btnProfile = (Button) findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent time = new Intent(UserActivity.this, UserProfileActivity.class);
                startActivity(time);
            }
        });
    }

    public void Alerts() {
        btnAlerts = (Button) findViewById(R.id.btnAlerts);
        btnAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alerts = new Intent(UserActivity.this, PlacePickerActivity.class);
                startActivity(alerts);
            }
        });
    }

    public void Maps() {
        btnMaps = (Button) findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alerts = new Intent(UserActivity.this, MapsActivity.class);
                startActivity(alerts);
            }
        });
    }

    public void Friends() {
        friends = (Button) findViewById(R.id.friends);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent offMaps = new Intent(UserActivity.this, FriendsActivity.class);
                startActivity(offMaps);
            }
        });
    }

    public void Settings() {
        btnSetting = (Button) findViewById(R.id.setting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent setting = new Intent(UserActivity.this, UserActivity.class);
                startActivity(setting);
            }
        });
    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();        // root directory of the DB
        dbUserRef = dbRef.child("users").child(userID);

        fbStorage = FirebaseStorage.getInstance();
        fbStorageRef = fbStorage.getReference();

        profileName = (TextView) findViewById(R.id.profile_name);
        profileImage = (ImageView) findViewById(R.id.profile_image);

        loadUserProfile();
        loadUserProfileImage();

        /*textProfile = (TextView) findViewById(R.id.text_profile);
        textFriends = (TextView) findViewById(R.id.text_friends);
        textMap = (TextView) findViewById(R.id.text_map);
        textAlerts = (TextView) findViewById(R.id.text_alerts);


        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_profile:
                                textProfile.setVisibility(View.VISIBLE);
                                textFriends.setVisibility(View.GONE);
                                textMap.setVisibility(View.GONE);
                                textAlerts.setVisibility(View.GONE);
                                break;
                            case R.id.action_friends:
                                textProfile.setVisibility(View.GONE);
                                textFriends.setVisibility(View.VISIBLE);
                                textMap.setVisibility(View.GONE);
                                textAlerts.setVisibility(View.GONE);
                                break;
                            case R.id.action_map:
                                textProfile.setVisibility(View.GONE);
                                textFriends.setVisibility(View.GONE);
                                textMap.setVisibility(View.VISIBLE);
                                textAlerts.setVisibility(View.GONE);
                                break;
                            case R.id.action_alerts:
                                textProfile.setVisibility(View.GONE);
                                textFriends.setVisibility(View.GONE);
                                textMap.setVisibility(View.GONE);
                                textAlerts.setVisibility(View.VISIBLE);
                                break;
                        }
                        return false;
                    }
                });
*/
        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        signOutButton = (Button) findViewById(R.id.sign_out);
        resetPassword = (Button) findViewById(R.id.password);
        editProfile = (Button) findViewById(R.id.edit_userprofile);
        notifications = (Button) findViewById(R.id.notifications);

        helloUserText = (TextView) findViewById(R.id.text_user);

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // if user is null launch login activity
                    startActivity(new Intent(UserActivity.this, MainActivity.class));
                    finish();
                }else{
                    helloUserText.setText("Logged in as: " + user.getEmail());
                }
            }
        };

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutButton();
            }
        });
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfile();
            }
        });
        notifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifications();
            }
        });

/*        Alerts();
        Maps();
        Friends();
        Profile();
        Settings();*/


        /*BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_profile:
                        *//*textProfile.setVisibility(View.VISIBLE);
                        textFriends.setVisibility(View.GONE);
                        textMap.setVisibility(View.GONE);*//*
                        Intent profile = new Intent(getApplicationContext(), UserProfileActivity.class);
                        startActivity(profile);
                        break;
                    case R.id.action_friends:
                        *//*textProfile.setVisibility(View.GONE);
                        textFriends.setVisibility(View.VISIBLE);
                        textMap.setVisibility(View.GONE);*//*
                        Intent friends = new Intent(getApplicationContext(), FriendsActivity.class);
                        startActivity(friends);
                        break;
                    case R.id.action_map:
                        *//*textProfile.setVisibility(View.GONE);
                        textFriends.setVisibility(View.GONE);
                        textMap.setVisibility(View.VISIBLE);*//*
                        Intent map = new Intent(getApplicationContext(), MapsActivity.class);
                        startActivity(map);
                        break;
*//*                            case R.id.action_alerts:
                                Intent events = new Intent(getApplicationContext(), SearchActivity.class);
                                startActivity(events);
                                break;
                        *//*
                }
                return false;
            }
        });*/
    }

    private void notifications() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", getPackageName());
            intent.putExtra("app_uid", getApplicationInfo().uid);
            startActivity(intent);
        }
    }

    private void editProfile() {
        Intent edit_profile = new Intent(UserActivity.this, EditUserProfileActivity.class);
        startActivity(edit_profile);
    }

    private void resetPassword() {
        Intent reset_password = new Intent(UserActivity.this, ResetPasswordActivity.class);
        startActivity(reset_password);
    }

    // load user profile from DB
    private void loadUserProfile() {
        final LinearLayout interestsLinearLayout = (LinearLayout) findViewById(R.id.interests_linearlayout);
        dbUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                profileName.setText(user.getName());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                // User has not set profile image or storage error
                Log.d("loadUserProfileImage()", "Firebase storage exception " + exception.getMessage());
                Toast.makeText(UserActivity.this, "No profile image", Toast.LENGTH_LONG).show();
            }
        });
    }


    //sign out method
    public void signOutButton() {
        auth.signOut();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

}