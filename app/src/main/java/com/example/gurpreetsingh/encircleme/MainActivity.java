package com.example.gurpreetsingh.encircleme;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button btnLogin, btnLinkToSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private EditText loginInputEmail, loginInputPassword;
    private TextInputLayout loginInputLayoutEmail, loginInputLayoutPassword;
    private FirebaseAuth.AuthStateListener authListener;

    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private String uid;
    private User user;

    private static final String PREFS_NAME = "SavedPrefs";
    // SharedPreferences used to save whether a user has already created their profile
    // If a value is saved, then there is no reason to check the DB if a profile has been created
    private SharedPreferences savedPrefs;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();

        authListener = new FirebaseAuth.AuthStateListener() {
            /**onAuthStateChanged gets invoked in the UI thread on changes in the authentication state:
             - Right after the listener has been registered
             - When a user is signed in
             - When the current user is signed out
             - When the current user changes
             - When there is a change in the current user's token */
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                savedPrefs = getSharedPreferences(PREFS_NAME, 0);
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is already signed in
                    Log.d(TAG, "onAuthStateChanged: signed_in:" + user.getUid());

                    // Check preferences to see if user profile is already created
                    if(savedPrefs != null) {
                        boolean profileIsCreated = savedPrefs.getBoolean("profileIsCreated", false); // returns false if "profileIsCreated" doesn't exist
                        Log.d(TAG, "savedPrefs: profileIsCreated = " + profileIsCreated);
                        if (profileIsCreated) {
                            startActivity(new Intent(MainActivity.this, MapsActivity.class));
                        }
                    }
                    else{
                        // SharedPreferences not set --> check DB to see if profile is created
                        Log.d(TAG, "onAuthStateChanged: Checking DB to see if profile is created");
                        loadUserFromDB();  // starts CreateProfileActivity if user profile doesn't exist
                    }
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: signed_out");
                    // Set SharedPreferences profileIsCreated value to false after sign out
                    // --> when user signs back in the DB will be checked
                    savedPrefs.edit().putBoolean("profileIsCreated", false).apply();
                    Log.d(TAG, "onAuthStateChanged: updated SharedPreferences -> profileIsCreated = false");

                }
                // ...
            }
        };

        loginInputLayoutEmail = (TextInputLayout) findViewById(R.id.login_input_layout_email);
        loginInputLayoutPassword = (TextInputLayout) findViewById(R.id.login_input_layout_password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        loginInputEmail = (EditText) findViewById(R.id.login_input_email);
        loginInputPassword = (EditText) findViewById(R.id.login_input_password);

        btnLogin = (Button) findViewById(R.id.btn_login);
        btnLinkToSignUp = (Button) findViewById(R.id.btn_link_signup);
        btnResetPassword = (Button) findViewById(R.id.btn_reset_password);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm();
            }
        });



        btnLinkToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ResetPasswordActivity.class));
            }
        });
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

    /**
     * Validating form
     */
    private void submitForm() {
        String email = loginInputEmail.getText().toString().trim();
        String password = loginInputPassword.getText().toString().trim();

        if (!checkEmail()) {
            return;
        }
        if (!checkPassword()) {
            return;
        }
        loginInputLayoutEmail.setErrorEnabled(false);
        loginInputLayoutPassword.setErrorEnabled(false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("One moment please...");
        progressDialog.show();
        // progressBar.setVisibility(View.VISIBLE);

        //authenticate user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                        // progressBar.setVisibility(View.GONE);
                        progressDialog.hide();

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "You are logged in!",
                                    Toast.LENGTH_SHORT).show();
                            // Check if user has already created a profile
                            loadUserFromDB();
                        }
                    }
                });
    }

    // Obtains the user profile in the DB and load the correct activity
    private void loadUserFromDB(){
        database = database.getInstance();
        uid = auth.getCurrentUser().getUid();
        Log.d(TAG, "loading user from DB\nuid: " + uid);
        dbRef = database.getReference();
        dbRef = dbRef.child("users").child(uid);

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                Log.d(TAG, "onDataChange\nuser: " + user);

                if(profileIsCreated())
                {   // User profile is already made
                    // Save this in the preferences for future reference
                    savedPrefs.edit().putBoolean("profileIsCreated", true).apply();
                    Log.d(TAG, "savedPrefs: saving profileIsCreated in SharedPreferences");
                    Intent nextActivity = new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(nextActivity);
                }else{
                    // user profile not created yet
                    Intent createProfile = new Intent(MainActivity.this, CreateUserProfileActivity.class);
                    startActivity(createProfile);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Database Error");
            }

            private boolean profileIsCreated(){
                if(user == null) return false;
                else return true;
            }
        });
    }


    private boolean checkEmail() {
        String email = loginInputEmail.getText().toString().trim();
        if (email.isEmpty() || !isEmailValid(email)) {

            loginInputLayoutEmail.setErrorEnabled(true);
            loginInputLayoutEmail.setError(getString(R.string.err_msg_email));
            loginInputEmail.setError(getString(R.string.err_msg_required));
            requestFocus(loginInputEmail);
            return false;
        }
        loginInputLayoutEmail.setErrorEnabled(false);
        return true;
    }

    private boolean checkPassword() {
        String password = loginInputPassword.getText().toString().trim();
        if (password.isEmpty() || !isPasswordValid(password)) {

            loginInputLayoutPassword.setError(getString(R.string.err_msg_password));
            loginInputPassword.setError(getString(R.string.err_msg_required));
            requestFocus(loginInputPassword);
            return false;
        }
        loginInputLayoutPassword.setErrorEnabled(false);
        return true;
    }

    private static boolean isEmailValid(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private static boolean isPasswordValid(String password) {
        return (password.length() >= 6);
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
