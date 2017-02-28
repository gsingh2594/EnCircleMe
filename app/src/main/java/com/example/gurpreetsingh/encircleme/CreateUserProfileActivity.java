package com.example.gurpreetsingh.encircleme;

/**
 * Created by Brayden on 2/18/2017.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class CreateUserProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;

    private Button saveButton;
    private EditText signUpName, signUpPhone, signUpUsername;
    private ProgressDialog progressDialog;
    private String uid;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user_profile);
        setTitle("Additional Info");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        uid = auth.getCurrentUser().getUid();

        saveButton = (Button) findViewById(R.id.saveNameAndPhone);
        signUpName = (EditText) findViewById(R.id.signUpName);
        signUpPhone = (EditText) findViewById(R.id.signUpPhone);
        signUpUsername = (EditText) findViewById(R.id.signUpUsername);
        saveButton.setOnClickListener(this);
    }

    public void saveUserProfile(){
        final String name = signUpName.getText().toString().trim();
        final String phone = signUpPhone.getText().toString().trim();
        username = signUpUsername.getText().toString().trim();

        if(name.length() < 3)
            signUpName.setError("Please enter a name at least 3 characters long");
        else if(phone.length() != 10)
            signUpPhone.setError("Please enter a 10 digit phone number");
        else if(username.length() < 3) {
            signUpUsername.setError("Username must be at least 3 characters long");
        }else{
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("One moment please...");
            progressDialog.show();

            // check if username already exists in DB
            try {
                DatabaseReference usernameRef = dbRef.child("usernames").child(username);
                Log.d("usernameExists", usernameRef.toString());

                // add one time listener to retrieve data at DatabaseReference pathway
                usernameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("usernameExists", "onDataChange: \n" + dataSnapshot.toString());
                        // Check if username exists
                        if (dataSnapshot.exists()) {        // basically the same as (dataSnapshot.getValue() !== null)
                            // username already exists -> display warnings
                            signUpUsername.setError("That username already exists!");
                            progressDialog.hide();
                        } else {
                            // username is available -> save user profile in DB
                            // create a User Java object to save all user attributes in the same directory at once
                            User newUser = new User(name, phone, username);

                            // use a Map for multiple path updates in one trip to database
                            Map<String, Object> userProfileUpdates = new HashMap<String, Object>();
                            userProfileUpdates.put("users/" + uid, newUser);
                            userProfileUpdates.put("usernames/" + username, uid);

                            // use updateChildren instead of setValue! setValue causes an error for pathway to the key
                            dbRef.updateChildren(userProfileUpdates, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    if(databaseError != null){
                                        Log.d("userProfileUpdates", "user profile could not be saved: \n" + databaseError.getMessage());
                                    }else{
                                        // data saved successfully
                                        Toast.makeText(CreateUserProfileActivity.this, "Data saved", Toast.LENGTH_SHORT).show();
                                        Log.d("userProfileUpdates", "user profile saved in the following pathways: \n"
                                                + databaseReference.child("users").child(uid).toString() + "\n"
                                                + databaseReference.child("usernames").child(username).toString());

                                        Intent showProfile = new Intent(CreateUserProfileActivity.this, MapsActivity.class);
                                        startActivity(showProfile);
                                    }
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(CreateUserProfileActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                        Log.d("usernameExists", "Database error: " + databaseError.getMessage() + "\n" + databaseError.getDetails());
                    }
                });
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if(v == saveButton)
            saveUserProfile();
    }
}


/* -----DB saving and retrieving------
Example for saving data without a Java object.
Note that each setValue() will call the onDataChange method upon completion
    dbRef.child("users").child(uid).child("name").setValue(name);
    dbRef.child("users").child(uid).child("phone").setValue(phone);

Example for retrieving data from DB that is not stored as a Java object
    DataSnapshot returnedData = dataSnapshot.child(uid);
    String name = returnedData.child("name").getValue().toString();
    String phone = returnedData.child("phone").getValue().toString();
*/