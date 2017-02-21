package com.example.gurpreetsingh.encircleme;

/**
 * Created by Brayden on 2/18/2017.
 */

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class CreateUserProfileActivity extends AppCompatActivity implements View.OnClickListener{
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;

    private Button saveButton;
    private EditText signUpName, signUpPhone;
    private LinearLayout linearLayout;
    private String uid;

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
        saveButton.setOnClickListener(this);
    }

    public void saveUserNameAndPhone(){
        String name = signUpName.getText().toString().trim();
        String phone = signUpPhone.getText().toString().trim();

        if(name.length() < 3)
            signUpName.setError("Please enter a name at least 3 characters long");
        else if(phone.length() < 10)
            signUpPhone.setError("Please enter a 10 digit phone number");
        else {
            // set DB reference to the path /users/uid
            dbRef = dbRef.child("users").child(uid);
            // create a User Java object to save all user attributes in the same directory at once
            User newUser = new User(name, phone);
            // save values in DB
            dbRef.setValue(newUser);

            // Example for saving data without a Java object.
            // Note that each setValue() will call the onDataChange method upon completion
            /*
            dbRef.child("users").child(uid).child("name").setValue(name);
            dbRef.child("users").child(uid).child("phone").setValue(phone);
            */
            dbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Toast.makeText(CreateUserProfileActivity.this, "Data saved", Toast.LENGTH_SHORT).show();

                    // Example for retrieving data from DB that is not stored as a Java object
                    /*
                    DataSnapshot returnedData = dataSnapshot.child(uid);
                    String name = returnedData.child("name").getValue().toString();
                    String phone = returnedData.child("phone").getValue().toString();
                    */
                    User savedUserInfo = dataSnapshot.getValue(User.class);
                    Log.d("CreateUserProfile", "Saved in DB:\nName: " + savedUserInfo.getName() + "\nPhone: " + savedUserInfo.getPhone());

                    Intent showProfile = new Intent(CreateUserProfileActivity.this, MapsActivity.class);
                    startActivity(showProfile);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(CreateUserProfileActivity.this, "Database error", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if(v == saveButton)
            saveUserNameAndPhone();
    }
}
