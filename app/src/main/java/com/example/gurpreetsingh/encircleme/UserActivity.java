package com.example.gurpreetsingh.encircleme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserActivity extends AppCompatActivity {

    private Button signOutButton;
    private TextView helloUserText;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    Button btnAlerts;
    Button btnMaps;
    Button btnProfile;
    Button friends;
    Button btnSetting;

    private TextView textProfile;
    private TextView textFriends;
    private TextView textMap;
    private TextView textAlerts;


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
                    helloUserText.setText("Hello  " + user.getEmail() +"");
                }
            }
        };

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutButton();
            }
        });

/*        Alerts();
        Maps();
        Friends();
        Profile();
        Settings();*/


        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_profile:
                        /*textProfile.setVisibility(View.VISIBLE);
                        textFriends.setVisibility(View.GONE);
                        textMap.setVisibility(View.GONE);*/
                        Intent profile = new Intent(getApplicationContext(), UserProfileActivity.class);
                        startActivity(profile);
                        break;
                    case R.id.action_friends:
                        /*textProfile.setVisibility(View.GONE);
                        textFriends.setVisibility(View.VISIBLE);
                        textMap.setVisibility(View.GONE);*/
                        Intent friends = new Intent(getApplicationContext(), FriendsActivity.class);
                        startActivity(friends);
                        break;
                    case R.id.action_map:
                        /*textProfile.setVisibility(View.GONE);
                        textFriends.setVisibility(View.GONE);
                        textMap.setVisibility(View.VISIBLE);*/
                        Intent map = new Intent(getApplicationContext(), MapsActivity.class);
                        startActivity(map);
                        break;
/*                            case R.id.action_alerts:
                                Intent events = new Intent(getApplicationContext(), SearchActivity.class);
                                startActivity(events);
                                break;
                        */
                }
                return false;
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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }


}