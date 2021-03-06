package com.example.gurpreetsingh.encircleme;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;



public class FirebaseNotificationService extends Service {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String userID;

    @Override
    public void onCreate() {
        super.onCreate();

        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();
        if (userID != null) {
            // Create new thread to make sure the main (UI) thread is not blocked
            new Thread(new Runnable() {
                public void run() {
                    // a potentially  time consuming task
                    database = FirebaseDatabase.getInstance();
                    addFriendNotificationListener();
                    acceptedFriendNotificationListener();
                    addEventNotificationListener();
                    eventInviteListener();
                }
            }).start(); // Run the thread
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // START_STICKY makes sense run for services running for arbitrary periods of time,
        // such as a service performing background music playback.
        return Service.START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Service should not be bound --> return null
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void addFriendNotificationListener() {
        final DatabaseReference friendsRequestsRef = database.getReference("friend_requests/" + userID);
        // List for storing all previously received requests. That way only new requests will create notifications
        final List<String> previousFriendRequestsList = new ArrayList<String>();

        friendsRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot friendRequest : dataSnapshot.getChildren()) {
                    String username = friendRequest.getValue().toString();
                    Log.d("onDataChange", "Existing friend request --> username = " + username);
                    previousFriendRequestsList.add(username);
                }

                // listen for new friend requests in DB
                friendsRequestsRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        // Get username of the user that sent the friend request
                        String usernameOfSender = dataSnapshot.getValue().toString();
                        String userIDOfSender = dataSnapshot.getKey().toString();

                        // Check if friend request is new or not
                        if (!previousFriendRequestsList.contains(usernameOfSender)) {
                            // new friend request
                            Log.d("onChildAdded", "New friend request --> usernameOfSender = " + usernameOfSender);
                            // Create notification to be displayed
                            createNewFriendRequestNotification(usernameOfSender, userIDOfSender);
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        String usernameOfSender = dataSnapshot.getValue().toString();
                        Log.d("onChildRemoved", "Friend request removed --> usernameOfSender = " + usernameOfSender);
                        previousFriendRequestsList.remove(usernameOfSender);

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("NotificationService", "DB error: " + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("NotificationService", "DB error: " + databaseError.getMessage());
            }
        });
    }


    public boolean createNewFriendRequestNotification(String usernameOfSender, String userIDOfSender) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setSmallIcon(R.drawable.ic_request); // notification icon
        mBuilder.setContentTitle("New Friend Request"); // title for notification
        mBuilder.setContentText("You have a new friend request from " + usernameOfSender); // message for notification
        mBuilder.setAutoCancel(true); // clear notification after click
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH); // setting priority in order to bring it up on the notification screen
        mBuilder = mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}); // setting vibrate for a notification
        mBuilder.setLights(Color.BLUE, 500, 500); // light for notification display
        mBuilder.setDefaults(Notification.DEFAULT_SOUND); // setting the notification sound to default device sound

        Intent intent = new Intent(this, FriendRequestsActivity.class);
        //intent.putExtra("userID",userIDOfSender);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());

        return true;

    }

    public void acceptedFriendNotificationListener() {
        final DatabaseReference acceptedFriendsRequestsRef = database.getReference("friends/" + userID);
        // List for storing all friends. Only newly accepted friends would be notified
        final List<String> previousFriendsList = new ArrayList<String>();

        acceptedFriendsRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot acceptedRequest : dataSnapshot.getChildren()) {
                    String username = acceptedRequest.getValue().toString();
                    Log.d("onDataChange", "Existing friend request --> username = " + username);
                    previousFriendsList.add(username);
                }

                // listen for new accepted friends in DB
                acceptedFriendsRequestsRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        // Get username of the user that sent the friend request
                        String usernameOfSender = dataSnapshot.getValue().toString();

                        // Check if friend is new or not
                        if (!previousFriendsList.contains(usernameOfSender)) {
                            // Accepted Friend Request
                            Log.d("onChildAdded", "New friend request --> usernameOfSender = " + usernameOfSender);
                            // Create notification to be displayed
                            acceptedFriendRequestNotification(usernameOfSender);
                        }
                    }


                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        String usernameOfSender = dataSnapshot.getValue().toString();
                        Log.d("onChildRemoved", "Friend request removed --> usernameOfSender = " + usernameOfSender);
                        previousFriendsList.remove(usernameOfSender);

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("NotificationService", "DB error: " + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("NotificationService", "DB error: " + databaseError.getMessage());
            }
        });
    }

    public boolean acceptedFriendRequestNotification(String usernameOfSender) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setSmallIcon(R.drawable.ic_request); // notification icon
        mBuilder.setContentTitle("New Friend"); // title for notification
        mBuilder.setContentText("Congrats! You have a new friend"); // message for notification
        mBuilder.setAutoCancel(true); // clear notification after click
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH); // setting priority in order to bring it up on the notification screen
        mBuilder = mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}); // setting vibrate for a notification
        mBuilder.setLights(Color.BLUE, 500, 500); // light for notification display
        mBuilder.setDefaults(Notification.DEFAULT_SOUND); // setting the notification sound to default device sound

        Intent intent = new Intent(this, FriendsActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());

        return true;


    }

    public void addEventNotificationListener() {
        final DatabaseReference friendsRef = database.getReference("friends/" + userID);
        // List for storing all friends.
        final List<String> previousFriendsList = new ArrayList<String>();

        friendsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot acceptedRequest : dataSnapshot.getChildren()) {
                    String userID = acceptedRequest.getKey().toString();
                    Log.d("onDataChange", "Existing friends --> userID = " + userID);
                    previousFriendsList.add(userID);
                }

                friendsRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        String userIDOfSender = dataSnapshot.getKey().toString();
                        Log.d("onChildRemoved", "Friend request removed --> usernameOfSender = " + userIDOfSender);
                        previousFriendsList.remove(userIDOfSender);

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



                final DatabaseReference addEventRef = database.getReference("events/user_created_events/");
                // List for storing all previously created events. That way only new events will create notifications
                final List<String> previousEventsList = new ArrayList<String>();

                addEventRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot userID : dataSnapshot.getChildren()) {
                            for(DataSnapshot userEvent : userID.getChildren()) {
                                String eventKey = userEvent.getKey().toString();
                                Log.d("onDataChange", "Existing event --> eventKey = " + eventKey);
                                previousEventsList.add(eventKey);
                            }
                        }
                        Log.d("previousEventsList", previousEventsList.toString());


                        // listen for new event creation in DB
                        addEventRef.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                // Get username of the user that creates the event
                                String usernameOfCreator = dataSnapshot.getValue().toString();
                                String userIDOfCreator = dataSnapshot.getKey().toString();

                                Iterable<DataSnapshot> allEvents = dataSnapshot.getChildren();
                                //DataSnapshot newEvent = Iterables.getLast(allEvents);
                                ArrayList<String> userEventsList = new ArrayList<String>();
                                for(DataSnapshot event : allEvents){
                                    userEventsList.add(event.getKey());
                                }
                                String lastEventKey = userEventsList.get(userEventsList.size() - 1);

                                // Check if the event created is new or not
                                if (previousFriendsList.contains(userIDOfCreator) && !previousEventsList.contains(lastEventKey)) {
                                    // new event created
                                    Log.d("onChildAdded", "New event created --> usernameOfCreator = " + usernameOfCreator);
                                    // Create notification to be displayed
                                    createNewEventCreatedNotification(userIDOfCreator,lastEventKey );
                                }
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                // Get username of the user that creates the event
                                String usernameOfCreator = dataSnapshot.getValue().toString();
                                String userIDOfCreator = dataSnapshot.getKey().toString();

                                Log.d("onChildAdded", "New event created onChildChanged --> usernameOfCreator = " + usernameOfCreator);

                                Iterable<DataSnapshot> allEvents = dataSnapshot.getChildren();
                                //DataSnapshot newEvent = Iterables.getLast(allEvents);
                                ArrayList<String> userEventsList = new ArrayList<String>();
                                for(DataSnapshot event : allEvents){
                                    userEventsList.add(event.getKey());
                                }
                                String lastEventKey = userEventsList.get(userEventsList.size() - 1);


                                if (previousFriendsList.contains(userIDOfCreator) && !previousEventsList.contains(lastEventKey)) {


                                // Create notification to be displayed
                                createNewEventCreatedNotification(userIDOfCreator,lastEventKey);
                                }

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {
                                String userIDOfSender = dataSnapshot.getKey().toString();
                                Log.d("onChildRemoved", "Friend request removed --> usernameOfSender = " + userIDOfSender);

                                Iterable<DataSnapshot> allEvents = dataSnapshot.getChildren();

                                ArrayList<String> userEventsList = new ArrayList<String>();
                                for(DataSnapshot event : allEvents){
                                    userEventsList.add(event.getKey());
                                }
                                String lastEventKey = userEventsList.get(userEventsList.size() - 1);
                                previousEventsList.remove(lastEventKey);


                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d("NotificationService", "DB error: " + databaseError.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onCancelled (DatabaseError databaseError){

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("NotificationService", "DB error: " + databaseError.getMessage());
            }

        });

    }



    public boolean createNewEventCreatedNotification(String userIDOfCreator, String lastEventKey) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setSmallIcon(R.drawable.ic_request); // notification icon
        mBuilder.setContentTitle("New Event Created"); // title for notification
        mBuilder.setContentText("Click to check out the new event"); // message for notification
        mBuilder.setAutoCancel(true); // clear notification after click
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH); // setting priority in order to bring it up on the notification screen
        mBuilder = mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}); // setting vibrate for a notification
        mBuilder.setLights(Color.BLUE, 500, 500); // light for notification display
        mBuilder.setDefaults(Notification.DEFAULT_SOUND); // setting the notification sound to default device sound

        Intent intent = new Intent(this, EventInfoActivity.class);
        intent.putExtra("eventKey",lastEventKey);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());

        return true;


    }
    public void eventInviteListener() {
        final DatabaseReference eventInviteRef = database.getReference("event_invites/" + userID);


        final List<String> previousInvitesList = new ArrayList<String>();

        eventInviteRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot previousEvents : dataSnapshot.getChildren())
                {
                    String eventKey = previousEvents.getKey().toString();
                    Log.d("onDataChange", "Existing invite --> username = " + eventKey);
                    previousInvitesList.add(eventKey);
                }


                // listen for new accepted friends in DB
                eventInviteRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        String eventKey = dataSnapshot.getKey().toString();


                        if (!previousInvitesList.contains(eventKey)) {

                            Log.d("onChildAdded", "New Event Invite --> eventKey = " + eventKey);
                            // Create notification to be displayed
                            newEventInviteNotification(eventKey);
                        }
                    }


                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s)
                    {

                        /*String eventKey = dataSnapshot.getKey().toString();
                        Log.d("onChildChanged", "New Event Invite --> userID = " +eventKey);
                        if(!previousInvitesList.contains(eventKey))
                        {

                            newEventInviteNotification(eventKey);
                        }
                        */


                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("NotificationService", "DB error: " + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("NotificationService", "DB error: " + databaseError.getMessage());
            }
        });
    }

    public boolean newEventInviteNotification(String eventKey)
    {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        mBuilder.setSmallIcon(R.drawable.ic_request); // notification icon
        mBuilder.setContentTitle("Event Invite"); // title for notification
        mBuilder.setContentText("Click now to check out the event you have been invited to"); // message for notification
        mBuilder.setAutoCancel(true); // clear notification after click
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH); // setting priority in order to bring it up on the notification screen
        mBuilder = mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000}); // setting vibrate for a notification
        mBuilder.setLights(Color.BLUE, 500, 500); // light for notification display
        mBuilder.setDefaults(Notification.DEFAULT_SOUND); // setting the notification sound to default device sound

        Intent intent = new Intent(this, EventInfoActivity.class);
        intent.putExtra("eventKey",eventKey);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());

        return true;


    }

}