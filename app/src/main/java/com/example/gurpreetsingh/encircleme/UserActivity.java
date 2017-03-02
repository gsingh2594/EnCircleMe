package com.example.gurpreetsingh.encircleme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserActivity extends AppCompatActivity {

    private Button signOutButton;
    private TextView helloUserText;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    ImageButton btnAlerts;
    ImageButton btnMaps;
    ImageButton btnProfile;
    ImageButton friends;
    ImageButton btnSetting;

    //Button
    public void Profile() {
        btnProfile = (ImageButton) findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent time = new Intent(UserActivity.this, UserActivity.class);
                startActivity(time);
            }
        });
    }

    public void Alerts(){
        btnAlerts = (ImageButton) findViewById(R.id.btnAlerts);
        btnAlerts.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent alerts = new Intent(UserActivity.this, PlacePickerActivity.class);
                startActivity(alerts);
            }
        });
    }
    public void Maps(){
        btnMaps = (ImageButton) findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent alerts = new Intent(UserActivity.this, MapsActivity.class);
                startActivity(alerts);
            }
        });
    }
    public void Friends(){
        friends = (ImageButton) findViewById(R.id.friends);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent offMaps = new Intent(UserActivity.this, SearchActivity.class);
                startActivity(offMaps);
            }
        });
    }
    public void Settings(){
        btnSetting = (ImageButton) findViewById(R.id.setting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent setting = new Intent(UserActivity.this, UserActivity.class);
                startActivity(setting);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

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

        Alerts();
        Maps();
        Friends();
        Profile();
        Settings();
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