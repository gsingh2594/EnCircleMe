package com.example.gurpreetsingh.encircleme;

/**
 * Created by GurpreetSingh on 4/18/17.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

public class ChatActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<ChatMessage> adapter;
    private String username;
    private String name;
    private String userID;
    private boolean usernameIsLoaded = false;
    private BottomBar bottomBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        setTitle("Event Chat");

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        loadUserName();

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input);
                if(usernameIsLoaded) {
                    // Read the input field and push a new instance
                    // of ChatMessage to the Firebase database
                        if(input.getText().toString().equals("")){
                           // input.setError("Type a message");
                        }
                        else {
                            FirebaseDatabase.getInstance()
                                    .getReference("chat")
                                    .push()
                                    .setValue(new ChatMessage(input.getText().toString(), username));
                            // Clear the input
                            input.setText("");
                        }
                }
                else{
                    ProgressDialog progressDialog = new ProgressDialog(ChatActivity.this);
                    progressDialog.setMessage("One moment please");
                    while(!usernameIsLoaded){
                        Log.d("onClick", "Entering while loop because username is not loaded");
                        progressDialog.show();
                    }
                    progressDialog.hide();
                    if(input.getText().toString().equals("")){
                        //input.setError("Type a message");
                    }
                    else {
                        // Save message in DB
                        FirebaseDatabase.getInstance()
                                .getReference("chat")
                                .push()
                                .setValue(new ChatMessage(input.getText().toString(), username));
                        // Clear the input
                        input.setText("");
                    }
                }
            }
        });

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_chats);
        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                if (tabId == R.id.tab_profile) {
                    Intent profile = new Intent(getApplicationContext(), UserProfileActivity.class);
                    startActivity(profile);
                } else if (tabId == R.id.tab_friends) {
                    Intent friends = new Intent(getApplicationContext(), FriendsActivity.class);
                    startActivity(friends);
                } else if (tabId == R.id.tab_map) {
                    Intent map = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(map);
                } else if (tabId == R.id.tab_alerts) {
                    Intent events = new Intent(getApplicationContext(), EventsTabActivity.class);
                    startActivity(events);
/*                } else if (tabId == R.id.tab_chats) {
                    Intent events = new Intent(getApplicationContext(), ChatActivity.class);
                    startActivity(events);*/
                }
            }
        });
    }

    private void loadUserName(){
        DatabaseReference usernamesRef = FirebaseDatabase.getInstance().getReference("usernames");
        usernamesRef.orderByChild("id").equalTo(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 1) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        username = ds.getKey().toString();
                        usernameIsLoaded = true;
                        //Toast.makeText(ChatActivity.this, "Welcome " + username, Toast.LENGTH_LONG).show();
                        displayChatMessages();
                    }
                }
                else{
                    Log.d("Error", "user somehow has more than 1 username!");
                    throw new IllegalStateException("user has more than 1 username!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayChatMessages() {
        final ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference("chat")) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageText.setMovementMethod(LinkMovementMethod.getInstance());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("MM/dd/yy (hh:mma)",
                        model.getMessageTime()));
            }
        };
        listOfMessages.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomBar.setDefaultTab(R.id.tab_chats);
    }
}