package com.example.gurpreetsingh.encircleme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Arani Hasan on 3/28/17.
 */

public class EditUserProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private DatabaseReference dbUserRef;
    private String userID;
    private User user;


    private Button applyChanges;
    private EditText editName, editPhone;
    private TextView editEmail, editUsername;
    private ProgressDialog progressDialog;
    private String uid;
    private String username;
    private ArrayList<String> interestsList = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);
        setTitle("Additional Info");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        uid = auth.getCurrentUser().getUid();
        userID = auth.getCurrentUser().getUid();
        dbUserRef = dbRef.child("users").child(userID);

        applyChanges = (Button) findViewById(R.id.apply_changes);
        editName = (EditText) findViewById(R.id.edit_name);
        editPhone = (EditText) findViewById(R.id.edit_phone);
        editUsername = (TextView) findViewById(R.id.edit_username);
        editEmail = (TextView) findViewById(R.id.edit_email);

        //signUpUsername = (EditText) findViewById(R.id.signUpUsername);
        applyChanges.setOnClickListener(this);
        loadUserProfile();

    }

    // load user profile from DB
    private void loadUserProfile() {
        dbUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                editEmail.setText("Email: " + user.getEmail());
                editUsername.setText("Username: " + user.getUsername());
                // display bio if it has been set
                if (user.getName() != null)
                    editName.setText(user.getName());
                if (user.getName() != null)
                    editPhone.setText(user.getPhone());
/*                if (user.getInterests() != null)
                    editInterest.setText(user.getInterests());*/
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditUserProfileActivity.this, "Data could not be retrieved", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void onCheckboxClicked(View view) {
        // Is the view now checked?
        boolean checked = ((CheckBox) view).isChecked();

        // Check which checkbox was clicked
        switch (view.getId()) {
            case R.id.checkbox_movies:
                if (checked) {
                    interestsList.add("Movie Theatres");
                } else {
                    interestsList.remove("Movie Theatres");
                }
                break;

            case R.id.checkbox_artgallery:
                if (checked) {
                    interestsList.add("Art Gallery");
                } else {
                    interestsList.remove("Art Gallery");
                }
                break;

            case R.id.checkbox_cafe:
                if (checked) {
                    interestsList.add("Cafe");
                } else {
                    interestsList.remove("Cafe");
                }
                break;
            case R.id.checkbox_bars:
                if (checked) {
                    interestsList.add("Bars");
                } else {
                    interestsList.remove("Bars");
                }
                break;
            case R.id.checkbox_restaurants:
                if (checked) {
                    interestsList.add("Restaurants");
                } else {
                    interestsList.remove("Restaurants");
                }
                break;
            case R.id.checkbox_deptstores:
                if (checked) {
                    interestsList.add("Department Stores");
                } else {
                    interestsList.remove("Department Stores");
                }
                break;
        }
    }

    public void saveUserProfile() {
        final String name = editName.getText().toString().trim();
        final String phone = editPhone.getText().toString().trim();

        if (name.length() < 3)
            editName.setError("Please enter a name at least 3 characters long");
        else if (phone.length() != 10)
            editPhone.setError("Please enter a 10 digit phone number");
            //else if(username.length() < 3) {
            //    signUpUsername.setError("Username must be at least 3 characters long");}
        else {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("One moment please...");
            progressDialog.show();

            /*// check if username already exists in DB
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
                            // create a User Java object to save all user attributes in the same DB directory at once
                            User newUser = new User(name, phone, email, username, interestsList);

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
                                        Toast.makeText(EditUserProfileActivity.this, "Data saved", Toast.LENGTH_SHORT).show();
                                        Log.d("userProfileUpdates", "user profile saved in the following pathways: \n"
                                                + databaseReference.child("users").child(uid).toString() + "\n"
                                                + databaseReference.child("usernames").child(username).toString());

                                        Intent showProfile = new Intent(EditUserProfileActivity.this, MapsActivity.class);
                                        startActivity(showProfile);
                                    }
                                }
                            });
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(EditUserProfileActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                        Log.d("usernameExists", "Database error: " + databaseError.getMessage() + "\n" + databaseError.getDetails());
                    }
                });
            }
            catch (Exception e){
                e.printStackTrace();
            }*/
        }
    }

    @Override
    public void onClick(View v) {
        if (v == applyChanges)
            saveUserProfile();
        Intent refreshProfile = new Intent(EditUserProfileActivity.this, UserProfileActivity.class);
        startActivity(refreshProfile);
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