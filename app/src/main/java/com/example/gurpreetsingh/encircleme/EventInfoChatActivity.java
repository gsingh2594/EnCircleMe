package com.example.gurpreetsingh.encircleme;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.common.logger.Log;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by GurpreetSingh on 4/30/17.
 */

public class EventInfoChatActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST_CODE = 1;
    private FirebaseListAdapter<ChatMessageEvent> adapter;
    private String username;
    private String name;
    private boolean usernameIsLoaded = false;

    private String eventKey;
    private String userID;
    private Event event;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        android.util.Log.d("onCreate", "method started");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventinfo_chat_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.eventchat_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        setTitle("Event Chat");

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        eventKey = getIntent().getStringExtra("eventKey");
        loadUserName();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input_event);

                if(usernameIsLoaded) {
                    // Read the input field and push a new instance
                    // of ChatMessage to the Firebase database
                    if(input.getText().toString().equals("")){
                        // input.setError("Type a message");
                    }
                    else {
                        FirebaseDatabase.getInstance()
                                .getReference("event_chats/" + eventKey)
                                .push()
                                .setValue(new ChatMessageEvent(input.getText().toString(), username));

                        // Clear the input
                        input.setText("");
                    }
                }
                else{
                    ProgressDialog progressDialog = new ProgressDialog(EventInfoChatActivity.this);
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
                                .getReference("event_chats/" + eventKey)
                                .push()
                                .setValue(new ChatMessageEvent(input.getText().toString(), username));

                        // Clear the input
                        input.setText("");
                    }
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
                        //Toast.makeText(EventInfoChatActivity.this, "Welcome " + username, Toast.LENGTH_LONG).show();
                        displayChatMessages();
                    }
                }
                else{
                    android.util.Log.d("Error", "user somehow has more than 1 username!");
                    throw new IllegalStateException("user has more than 1 username!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayChatMessages() {
        ListView listOf_Messages = (ListView) findViewById(R.id.list_of_eventmessages);

        adapter = new FirebaseListAdapter<ChatMessageEvent>(this, ChatMessageEvent.class,
                R.layout.message_event, FirebaseDatabase.getInstance().getReference("event_chats/" +eventKey)) {
            @Override
            protected void populateView(View v, ChatMessageEvent model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);
                Log.d("populate view", "getting msgs");

                // Set their text
                messageText.setText(model.getMessageText());
                messageText.setMovementMethod(LinkMovementMethod.getInstance());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("MM/dd/yy (hh:mma)",
                        model.getMessageTime()));
            }
        };
        listOf_Messages.setAdapter(adapter);
    }

    private void loadEventInfoFromDB(){
        DatabaseReference eventRef = FirebaseDatabase.getInstance().getReference("events/all_events/" + eventKey);
        eventRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                event = dataSnapshot.getValue(Event.class);
                android.util.Log.d("event", event.toString());
                //displayEventInfo();
                //showEventInMap();
                //loadEventCreatorName();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                android.util.Log.d("onCancelled", "Database Error: " + databaseError.getMessage());
                Toast.makeText(EventInfoChatActivity.this, "Could not load event", Toast.LENGTH_LONG).show();
            }
        });
    }

}
