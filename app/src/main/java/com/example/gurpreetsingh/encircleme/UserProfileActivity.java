package com.example.gurpreetsingh.encircleme;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
    private ImageView imageView;

    private TextView profileName;
    private TextView profileBio;
    private ImageView editIcon;

    private AlertDialog editBioDialog;

    Button btnAlerts;
    Button btnMaps;
    Button btnProfile;
    Button friends;
    Button btnSetting;

    //Button
    public void Profile() {
        btnProfile = (Button) findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent time = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                startActivity(time);
            }
        });
    }

    public void Alerts(){
        btnAlerts = (Button) findViewById(R.id.btnAlerts);
        btnAlerts.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent alerts = new Intent(UserProfileActivity.this, PlacePickerActivity.class);
                startActivity(alerts);
            }
        });
    }
    public void Maps(){
        btnMaps = (Button) findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent alerts = new Intent(UserProfileActivity.this, MapsActivity.class);
                startActivity(alerts);
            }
        });
    }
    public void Friends(){
        friends = (Button) findViewById(R.id.friends);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent offMaps = new Intent(UserProfileActivity.this, FriendsActivity.class);
                startActivity(offMaps);
            }
        });
    }
    public void Settings(){
        btnSetting = (Button) findViewById(R.id.setting);
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
        imageView = (ImageView) findViewById(android.R.id.icon);

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

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = getPath(data.getData());
            imageView.setImageBitmap(bitmap);
        }
    }


    public void selectImage(View view) {
        Intent intent = new Intent();
        intent.setType("image*//*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }*/

    private Bitmap getPath(Uri uri) {

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(column_index);
        // cursor.close();
        // Convert file path into bitmap image using below line.
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        return bitmap;
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


    /*
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    */
}
