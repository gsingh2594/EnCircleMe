package com.example.gurpreetsingh.encircleme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class EnCircleFriendsActivity extends AppCompatActivity {
    private ListView listView;

    ImageView neutralFace;
    TextView noFriendsTextView;
    Button findFriendsButton, enCircleFriendsButton;

    FirebaseAuth auth;
    String currentUserID;
    FirebaseDatabase database;
    DatabaseReference dbRef;

    long numOfFriends;
    final ArrayList<HashMap<String, String>> friendsList = new ArrayList<HashMap<String,String>>();
    final ArrayList<String> friendsIDList = new ArrayList<>();
    final HashMap<String, HashMap<String, String>> friendsMap = new HashMap<>();
    final HashMap<String, String> usernameToIDMap = new HashMap<>();
    final ArrayList<HashMap<String, String>> friendsNameUsernameList = new ArrayList<>();
    final ArrayList<String> enCircledFriendsList = new ArrayList<>();

    SimpleAdapter simpleAdapter;
    byte[] profileImageBytes;
    ArrayList<UserWithImage> userWithImageList = new ArrayList<>();
    Comparator userWithImageComparator;

    String eventKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encircle_friends);

        Toolbar toolbar = (Toolbar) findViewById(R.id.encircle_friends_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Select Friends to EnCircle");

        listView = (ListView) findViewById(R.id.encircle_friends_listview);

        neutralFace = (ImageView) findViewById(R.id.neutral_face_icon);
        noFriendsTextView = (TextView) findViewById(R.id.no_friends_textview);
        findFriendsButton = (Button) findViewById(R.id.find_friends_button);

        enCircleFriendsButton = (Button)findViewById(R.id.encircle_friends_button);

        currentUserID = auth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();

        eventKey = getIntent().getStringExtra("eventKey");
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d("onStart", "starting the activity");
        loadFriendsList();
        if(simpleAdapter != null)
            simpleAdapter.notifyDataSetChanged();
    }


    // load and display list of the current user's friends
    private void loadFriendsList(){
        // Clear all elements in the lists so they may be reloaded
        friendsMap.clear();
        usernameToIDMap.clear();
        friendsIDList.clear();
        friendsList.clear();
        friendsNameUsernameList.clear();
        userWithImageList.clear();

        Log.d("loadFriendsList", "method started");
        DatabaseReference friendsRef = database.getReference("friends");
        friendsRef.child(currentUserID).orderByValue().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    numOfFriends = dataSnapshot.getChildrenCount();
                    Log.d("numOfFriends", Long.toString(numOfFriends));
                    // User has friends -> store the info for each friend
                    for(DataSnapshot friend : dataSnapshot.getChildren()){
                        if(!friendsMap.containsKey(friend.getKey())) {
                            // Friend has not been loaded yet --> store the info
                            HashMap<String, String> friendInfo = new HashMap<String, String>();
                            String userID = friend.getKey();
                            String username = friend.getValue().toString();
                            friendInfo.put("userID", userID);
                            friendInfo.put("username", username);
                            friendsMap.put(friend.getKey(), friendInfo);
                            friendsIDList.add(userID);
                            friendsList.add(friendInfo);
                            loadUserNameAndID(username);
                            //loadUserProfileImage(userID, username);
                        }
                    }
                    Log.d("loadFriendsList", "current user has friends!");
                    // Hide the "no friends" views
                    neutralFace.setVisibility(View.GONE);
                    noFriendsTextView.setVisibility(View.GONE);
                    findFriendsButton.setVisibility(View.GONE);
                    /*
                    // Find listview from layout and initialize with an adapter
                    final ListView listView = (ListView) findViewById(R.id.friends_listview);
                    simpleAdapter = new SimpleAdapter(FriendsActivity.this, friendsList,
                            R.layout.friend_requests_list_items, new String[]{"username"}, new int[]{R.id.friend_requests_text_view});
                    listView.setAdapter(simpleAdapter);
                    simpleAdapter.notifyDataSetChanged();

                    // Show friend's user profile when clicked
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String otherUserID = friendsList.get(position).get("userID");
                            Intent viewOtherUserProfile = new Intent(FriendsActivity.this, ViewOtherUserProfileActivity.class);
                            viewOtherUserProfile.putExtra("userID", otherUserID);
                            startActivity(viewOtherUserProfile);
                        }
                    }); */
                }
                else{
                    // User does not have friends
                    neutralFace.setVisibility(View.VISIBLE);
                    noFriendsTextView.setVisibility(View.VISIBLE);
                    findFriendsButton.setVisibility(View.VISIBLE);
                    findFriendsButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent addFriendSearch = new Intent(EnCircleFriendsActivity.this, AddFriendSearchActivity.class);
                            startActivity(addFriendSearch);
                        }
                    });
                    // Set listview to be empty to prevent showing friends who have been deleted
                    final ListView listView = (ListView) findViewById(R.id.friends_listview);
                   /* simpleAdapter = new SimpleAdapter(FriendsActivity.this, friendsList,    // friendsList empty in this case
                            R.layout.friend_requests_list_items, new String[]{"username"}, new int[]{R.id.friend_requests_text_view} );
                    listView.setAdapter(simpleAdapter); */
                    listView.setAdapter(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("loadFriendsList", "error loading friends " + databaseError.getMessage());
            }
        });
    }


    // Loads a user's name and id in a NameAndID object for a given username
    private void loadUserNameAndID(final String username){
        Log.d("loadUserNameAndID", "username = " + username);
        DatabaseReference usernamesRef= database.getReference("usernames");
        usernamesRef.orderByKey().equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot result : dataSnapshot.getChildren()) {
                        NameAndID userNameAndID = result.getValue(NameAndID.class);
                        HashMap<String,String> hashMap = new HashMap<>();
                        hashMap.put("nameAndUsername", userNameAndID.getName() + " (" + username + ")");
                        hashMap.put("id", userNameAndID.getID());
                        hashMap.put("username", username);
                        friendsNameUsernameList.add(hashMap);
                        usernameToIDMap.put(username, userNameAndID.getID());
                        Log.d("loadUserNameAndID", "name and ID loaded: " + userNameAndID.toString());
                        /*if(friendsNameUsernameList.size() == numOfFriends)
                            displaySimpleResultsList(friendsNameUsernameList, friendsIDList);*/
                        loadUserProfileImage(userNameAndID, username);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    // Loads a profile image for a specified userID
    private void loadUserProfileImage(NameAndID userNameAndID, final String username){
        final String userID = userNameAndID.getID();
        final String name = userNameAndID.getName();
        Log.d("loadUserProfileImage", "userID = " + userID + "\nname = " + name + "\nusername = " + username);
        Log.d("load profile pic", "about to load from storage");
        StorageReference profilePicStorageRef = FirebaseStorage.getInstance().getReference("profile_images/" + userID);
        profilePicStorageRef.getBytes(1024*1024*5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // user has profile pic --> display it
                Log.d("loadUserProfileImage()", "getBytes successful");
                profileImageBytes = bytes;
                Log.d("loadUserProfileImage()", "convert bytes to bitmap");
                Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.length);
                //profileImage.setImageBitmap(profileImageBitmap);
                Log.d("Image loaded", "UserWithID: \nuserID = " + userID + "\nname = " + name + "\nusername = " + username);
                UserWithImage uwi = new UserWithImage(userID, username, name, profileImageBitmap);
                userWithImageList.add(uwi);
                Log.d("userWithImageList", "size = " + userWithImageList.size());
                if(userWithImageList.size() == numOfFriends)
                    displayListOfFriends();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // User has not set profile image
                Log.d("loadUserProfileImage()", "Firebase storage exception " + exception.getMessage());
                Toast.makeText(EnCircleFriendsActivity.this, "No profile image", Toast.LENGTH_LONG).show();
                UserWithImage uwi = new UserWithImage(userID, username, name, null);
                userWithImageList.add(uwi);
                Log.d("userWithImageList", "size = " + userWithImageList.size());
                if(userWithImageList.size() == numOfFriends)
                    displayListOfFriends();
            }
        });
    }


    // Sorts the list of users by name with images and displays them in the ListView with an adapter
    private void displayListOfFriends(){
        Log.d("displayListOfFriends()", "entering method");
        // Sort the userWithImages list so that users appear in order of name
        if(userWithImageComparator == null) {
            userWithImageComparator = new Comparator<UserWithImage>() {
                @Override
                public int compare(UserWithImage u1, UserWithImage u2) {
                    int result = u1.getName().compareTo(u2.getName());
                    if (result < 0) return -1;
                    else if (result > 0) return 1;
                    else return 0;
                }
            };
        }
        Log.d("diplayListOfFriends", "sorting userWithImagesList");
        Collections.sort(userWithImageList, userWithImageComparator);

        // Initialize the custom list adapter
        EnCircleUserListAdapter eula = new EnCircleUserListAdapter(EnCircleFriendsActivity.this, userWithImageList);
        listView.setAdapter(eula);

        // Add click listener to list so clicking anywhere on a row will select the user
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkBox = (CheckBox)view.findViewById(R.id.checkbox_encircle);
                if(checkBox.isChecked()){
                    checkBox.setChecked(false);
                }
                else{
                    checkBox.setChecked(true);
                }
                // Note that the onCheckedListener handles the adding/removing of userIDs to ArrayList

                Log.d("onItemClick", "position = " + position + "\n name = " + userWithImageList.get(position).getName()
                        + "\nuser ID = " + userWithImageList.get(position).getUserID());
            }
        });

        // Show the encircle friends button now that the list is loaded.
        enCircleFriendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendEventInvites();
            }
        });
    }


    // Updates the DB under "event_invites" for every friend selected in the list
    private void sendEventInvites(){
        DatabaseReference eventInvitesRef = FirebaseDatabase.getInstance().getReference("event_invites");

        // Create a HashMap for multipath update in DB so each selected friend will receive the invite
        HashMap<String, Object>invitesUpdates = new HashMap<>();
        for(int i = 0; i < enCircledFriendsList.size(); i++){
            String receiverUserID = enCircledFriendsList.get(i);
            invitesUpdates.put(receiverUserID + "/" + eventKey, currentUserID);
        }
        eventInvitesRef.updateChildren(invitesUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null){
                    // DB updated successfully --> notify user and return back to event info
                    Toast.makeText(EnCircleFriendsActivity.this, "Invites sent!", Toast.LENGTH_LONG).show();
                    finish();
                }
                else{
                    // DB error --> notify user
                    Toast.makeText(EnCircleFriendsActivity.this, "DB error", Toast.LENGTH_LONG).show();
                    Log.d("inviteUpdates", "DB error: " + databaseError.getMessage());
                }
            }
        });
    }




    /**
     * Created by Brayden on 5/5/2017.
     *
     * This class is used to display user's in a ListView where
     * each row contains a cropped circle of their profile image with their
     * name and username displayed on the right side. Also, a checkbox is displayed
     * on the right side of every row for selection of that user.
     */

    public class EnCircleUserListAdapter extends BaseAdapter {
        List<UserWithImage> usersList;
        Context context;

        public EnCircleUserListAdapter(Context context, List<UserWithImage> usersList) {
            super();
            this.usersList = usersList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return usersList.size();
        }

        @Override
        public Object getItem(int position) {
            return usersList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Log.d("getView", "entering");
            View row = convertView;
            if (row == null) {
                Log.d("getView", "creating row");
                // inflate the layout used for each row in the list
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.encircle_friends_list_items, parent, false);
            }
            // Retrieve the UserWithImage object from the list
            UserWithImage thisUser = usersList.get(position);
            Bitmap profileImage = thisUser.getProfileImage();
            if(profileImage != null){
                // User has a profile image --> display it in the row
                Log.d("getView", "setting image bitmap to ImageView");
                ImageView profileImageView = (ImageView) row.findViewById(R.id.friend_requests_item_image);
                //profileImageView.setLayoutParams(new RelativeLayout.LayoutParams(convertDPtoPX(40), convertDPtoPX(40)));
                // Get cropped circle Bitmap of profile image
                profileImage = getClip(thisUser.getProfileImage());
                profileImageView.setImageBitmap(profileImage);
                profileImageView.setBackground(null);   // Removes default icon
            }else{
                // No profile image --> default icon displayed in the list (defined in xml layout)
            }
            // Set the TextView text inside the row
            String name = thisUser.getName();
            String username = thisUser.getUsername();
            TextView nameView = (TextView) row.findViewById(R.id.friend_requests_text_view);
            if(name != null && username != null)
                nameView.setText(name + " (" + username + ")");
            else if(username!=null)
                nameView.setText(username);

            // Get the checkbox and use a check listener to determine whether the corresponding
            // userID should be added/removed from the ArrayList of selected friends
            final CheckBox checkBox = (CheckBox) row.findViewById(R.id.checkbox_encircle);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(checkBox.isChecked()) {
                        enCircledFriendsList.add(usersList.get(position).getUserID());
                        Log.d("CheckBox clicked", usersList.get(position).getName() + " userID added to list");
                    }
                    else {
                        enCircledFriendsList.remove(usersList.get(position).getUserID());
                        Log.d("CheckBox clicked", usersList.get(position).getName() + " userID removed from list");
                    }
                }
            });

            row.setTag(thisUser);

            /*row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CheckBox checkBox = (CheckBox)v.findViewById(R.id.checkbox_encircle);
                    if(checkBox.isChecked()){

                    }
                }
            });*/
            return row;
        }

        // Returns a bitmap as a cropped circle
        public Bitmap getClip(Bitmap bitmap) {
            Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);

            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                    (bitmap.getWidth() < bitmap.getHeight())?
                            bitmap.getWidth()/ 2 : bitmap.getHeight()/2,
                    paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
            return output;
        }

        // used to set sizes in dp units programmatically. (Some views set sizes programmtically in px, not dp)
        // We should use this method to make certain views display consistently on different screen densities
        private int convertDPtoPX(int sizeInDP){
            float scale = context.getResources().getDisplayMetrics().density;       // note that 1dp = 1px on a 160dpi screen
            int dpAsPixels = (int) (sizeInDP * scale + 0.5f);
            return dpAsPixels;  // return the size in pixels
        }
    }

}
