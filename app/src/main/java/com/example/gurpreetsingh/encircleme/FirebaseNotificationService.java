package com.example.gurpreetsingh.encircleme;

import android.app.NotificationManager;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;

import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.IntDef;
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

/**
 * Created by Brayden on 4/11/2017.
 */

public class FirebaseNotificationService extends Service {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private String userID;

    @Override
    public void onCreate() {
        super.onCreate();

        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();
        if(userID != null) {
            // Create new thread to make sure the main (UI) thread is not blocked
            new Thread(new Runnable() {
                public void run() {
                    // a potentially  time consuming task
                    database = FirebaseDatabase.getInstance();
                    addFriendNotificationListener();
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
    public void onDestroy(){
        super.onDestroy();
    }


    private void addFriendNotificationListener(){
        final DatabaseReference friendsRequestsRef = database.getReference("friend_requests/" + userID);
        // List for storing all previously received requests. That way only new requests will create notifications
        final List<String> previousFriendRequestsList = new ArrayList<String>();

        friendsRequestsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot friendRequest : dataSnapshot.getChildren()){
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

                        // Check if friend request is new or not
                        if( !previousFriendRequestsList.contains(usernameOfSender)){
                            // new friend request
                            Log.d("onChildAdded", "New friend request --> usernameOfSender = " + usernameOfSender);
                            // Create notification to be displayed
                            createNewFriendRequestNotification(usernameOfSender);
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


    private void createNewFriendRequestNotification(String usernameOfSender){

        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_new) // notification icon
                .setContentTitle("NEW FRIEND REQUEST") // title for notification
                .setContentText("You have a new friend request. Click now to check who it is") // message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(this, FriendRequestsActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        mBuilder.setContentIntent(pi);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());




    }
}