package com.example.gurpreetsingh.encircleme;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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

    private TextView profileName;
    private TextView profileBio;
    private ImageView editIcon;

    private AlertDialog editBioDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
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
}
