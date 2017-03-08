package com.example.gurpreetsingh.encircleme;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private DatabaseReference dbUserRef;
    private User user;

    private TextView profileName;
    private TextView profileBio;
    private ImageView editIcon;

    private AlertDialog editBioDialog;

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
                Intent time = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                startActivity(time);
            }
        });
    }

    public void Alerts(){
        btnAlerts = (ImageButton) findViewById(R.id.btnAlerts);
        btnAlerts.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent alerts = new Intent(UserProfileActivity.this, PlacePickerActivity.class);
                startActivity(alerts);
            }
        });
    }
    public void Maps(){
        btnMaps = (ImageButton) findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent alerts = new Intent(UserProfileActivity.this, MapsActivity.class);
                startActivity(alerts);
            }
        });
    }
    public void Friends(){
        friends = (ImageButton) findViewById(R.id.friends);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent offMaps = new Intent(UserProfileActivity.this, FriendsActivity.class);
                startActivity(offMaps);
            }
        });
    }
    public void Settings(){
        btnSetting = (ImageButton) findViewById(R.id.setting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent setting = new Intent(UserProfileActivity.this, UserActivity.class);
                startActivity(setting);
            }
        });
    }





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();        // root directory of the DB
        dbUserRef = dbRef.child("users").child(uid);

        profileName = (TextView) findViewById(R.id.user_profile_name);
        profileBio = (TextView) findViewById(R.id.user_profile_bio);
        editIcon = (ImageView) findViewById(R.id.edit_bio_icon);
        editIcon.setOnClickListener(this);

        dbUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                profileName.setText(user.getName());
                if(user.getBio()!=null)
                    profileBio.setText(user.getBio());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UserProfileActivity.this, "Data could not be retrieved", Toast.LENGTH_SHORT).show();
            }
        });

        //access different activity in ImageButton
        Alerts();
        Maps();
        Friends();
        Profile();
        Settings();
    }

    public void showEditDialog(){
        final AlertDialog.Builder editBioDialogBuilder = new AlertDialog.Builder(UserProfileActivity.this);
        editBioDialogBuilder.setTitle("Edit Bio");
        EditText textBox = new EditText(this);
        textBox.setId(R.id.edit_bio_text);
        textBox.setText(user.getBio());
        editBioDialogBuilder.setView(textBox);
        editBioDialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int ID){
                // User clicked save button
                saveUserBio();
                Intent refreshProfile = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                startActivity(refreshProfile);
            }
        });
        editBioDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        editBioDialog = editBioDialogBuilder.create();
        editBioDialog.show();
    }

    public void saveUserBio(){
        EditText dialogTextBox = (EditText) editBioDialog.findViewById(R.id.edit_bio_text);
        String bio = dialogTextBox.getText().toString().trim();
        user.setBio(bio);

        // dbUserRef.setValue(user);    --> Unnecessary because it rewrites the whole User object to the DB
        // use this line instead to update only the "bio" value
        dbUserRef.child("bio").setValue(bio);
    }

    @Override
    public void onClick(View v) {
        if(v == editIcon){
            showEditDialog();
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
