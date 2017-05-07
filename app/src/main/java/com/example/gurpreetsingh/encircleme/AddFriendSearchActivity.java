package com.example.gurpreetsingh.encircleme;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class AddFriendSearchActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference dbRef;
    ListView listView;

    private String currentUserID;
    Comparator userWithImageComparator;

    TextView textViewNoResults;
    ImageView sadFaceIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.add_friends_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        SearchView searchView = new SearchView(AddFriendSearchActivity.this);
        searchView.setLayoutParams(new ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_CLASS_TEXT);
        searchView.setActivated(true);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Enter name or username (case sensitive)");

        RelativeLayout relative=new RelativeLayout(getApplicationContext());
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rlp.setMargins(0,0,30,0);
        relative.setLayoutParams(rlp);
        relative.addView(searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                doMySearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.equals(""))
                    doMySearch(newText);
                else
                    // clear the list of results because the input is empty
                    listView.setAdapter(null);
                return true;
            }
        });
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(relative);
        //actionBar.setTitle("Add Friends");

        // Find the "no results" views
        textViewNoResults = (TextView) findViewById(R.id.no_users_found_textview);
        sadFaceIcon = (ImageView) findViewById(R.id.sad_face_icon);

        // Find the list view for results
        listView = (ListView)findViewById(R.id.friend_search_listview);

        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    public void handleIntent(Intent intent){
        // Get the intent, verify the action and get the query
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("onCreateOptionsMenu", "about to create menu");
        MenuInflater inflater = getMenuInflater();

        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_for_friends_icon:
                // User chose the "Settings" item, show the app settings UI...
                onSearchRequested();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    private void doMySearch(String query){
        DatabaseReference usernamesRef = firebaseDatabase.getReference("usernames");
        //DatabaseReference namesRef = firebaseDatabase.getReference("userIDs_to_names");
        // Log.d("doMySearch", "dbRef created: " + dbRef.toString());

        final ArrayList<HashMap<String, String>> resultsList = new ArrayList<HashMap<String,String>>();
        final ArrayList<String> userIDList = new ArrayList<>();
        final ArrayList<String> usernamesList = new ArrayList<>();
        final ArrayList<NameAndID> userNameAndIDList = new ArrayList<>();
        // List of type HashMap<String,String> for simple results list
        final ArrayList<HashMap<String, String>> nameAndUsernameMap = new ArrayList<>();

        // class for mutable value that indicates whether the results have been returned from a query
        class Result{
            int value;      // -1 --> results not returned yet, 0 --> no results, 1 --> has results
            Result(){value = -1;}
            void setValue(int value){this.value = value;}
            int getValue(){return value;}
        }

        final Result hasUsernameResults = new Result();
        final Result hasNameResults = new Result();

        Log.d("doMySearch", "about to add listener");

        // Query for finding usernames that match the input
        usernamesRef.orderByKey()
                .startAt(query)
                .endAt(query+"\uf8ff")
                .limitToFirst(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d("entering onDataChange", "");
                        Log.d("snapshot of children", dataSnapshot.toString());
                        if(dataSnapshot.exists()) {
                            hasUsernameResults.setValue(1);
                            Log.d("usernames query", "usernames found");
                            for (DataSnapshot user : dataSnapshot.getChildren()) {
                                NameAndID nameAndID = user.getValue(NameAndID.class);
                                if(!userIDList.contains(nameAndID.getID())) {
                                    String username = user.getKey();
                                    Log.d("onDataChange", "Adding child from DB to resultsList");
                                    HashMap<String, String> hashMap = new HashMap<String, String>();
                                    hashMap.put("nameAndUsername", nameAndID.getName() + " (" + username + ")");
                                    // hashMap.put("uid", user.getValue().toString());
                                    //resultsList.add(hashMap);
                                    nameAndUsernameMap.add(hashMap);
                                    Log.d("adding userID", nameAndID.getID());
                                    userIDList.add(nameAndID.getID());
                                    usernamesList.add(user.getKey());
                                    userNameAndIDList.add(nameAndID);
                                }
                            }
                            textViewNoResults.setVisibility(View.GONE);
                            sadFaceIcon.setVisibility(View.GONE);

                        }else{
                            hasUsernameResults.setValue(0);
                            Log.d("usernames query", "no usernames found");

                            // Check if other query has returned results
                            if(hasNameResults.getValue() == 0) {
                                // No results from other query
                                listView.setVisibility(View.GONE);
                                textViewNoResults.setVisibility(View.VISIBLE);
                                sadFaceIcon.setVisibility(View.VISIBLE);
                            }
                        }
                        // Check if other query is completed
                        Log.d("hasNameResults", Integer.toString(hasNameResults.getValue()));
                        if(hasNameResults.getValue() >= 0) {
                            // Query completed --> Show basic listview first
                            displaySimpleResultsList(nameAndUsernameMap, userIDList);
                            // Load profile images for the results
                            loadProfileImages(userNameAndIDList, usernamesList);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        // Query for finding names that match the input
        //namesRef.orderByValue()
        usernamesRef.orderByChild("name")
                .startAt(query)
                .endAt(query+"\uf8ff")
                .limitToFirst(10)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        TextView textViewNoResults = (TextView) findViewById(R.id.no_users_found_textview);
                        ImageView sadFaceIcon = (ImageView) findViewById(R.id.sad_face_icon);

                        Log.d("entering onDataChange", "");
                        Log.d("snapshot of children", dataSnapshot.toString());
                        if(dataSnapshot.exists()) {
                            Log.d("names query", "names found");
                            hasNameResults.setValue(1);
                            for (DataSnapshot user : dataSnapshot.getChildren()) {
                                NameAndID nameAndID = user.getValue(NameAndID.class);
                                if(!userIDList.contains(nameAndID.getID())) {
                                    String username = user.getKey();
                                    HashMap<String, String> hashMap = new HashMap<String, String>();
                                    hashMap.put("nameAndUsername", nameAndID.getName() + " (" + username + ")");
                                    Log.d("onDataChange", "Adding child from DB to resultsList");
                                    // hashMap.put("uid", user.getValue().toString());
                                    //resultsList.add(hashMap);
                                    nameAndUsernameMap.add(hashMap);
                                    Log.d("adding userID", nameAndID.getID());
                                    userIDList.add(nameAndID.getID());
                                    usernamesList.add(user.getKey());
                                    userNameAndIDList.add(nameAndID);
                                }
                            }
                            textViewNoResults.setVisibility(View.GONE);
                            sadFaceIcon.setVisibility(View.GONE);

                        }else{
                            hasNameResults.setValue(0);
                            Log.d("names query", "no names found");

                            // Check if other query has returned results
                            if(hasUsernameResults.getValue() == 0) {
                                // No results from other query
                                listView.setVisibility(View.GONE);
                                textViewNoResults.setVisibility(View.VISIBLE);
                                sadFaceIcon.setVisibility(View.VISIBLE);
                            }
                        }

                        // Check if other query is completed
                        Log.d("hasUsernameResults", Integer.toString(hasUsernameResults.getValue()));
                        if(hasUsernameResults.getValue() >= 0) {
                            // Query completed --> Show basic listview first
                            displaySimpleResultsList(nameAndUsernameMap, userIDList);
                            // Load profile images for the results
                            loadProfileImages(userNameAndIDList, usernamesList);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }


    // Loads all profile images for the users in the NameAndID list and creates a UserWithImage list
    private void loadProfileImages(final ArrayList<NameAndID> userNameAndIDList, ArrayList<String> usernameList) {
        Log.d("loadProfileImages", "entering method");
        // make the lists un-assignable so they can be accessed by the inner classes
        final ArrayList<NameAndID> userNameAndIDResultsList = userNameAndIDList;
        final ArrayList<String> usernameResultsList = usernameList;
        // Create a list for UserWithImage objects that are used in the custom listview adapter
        final ArrayList<UserWithImage> userWithImageList = new ArrayList<>();

        for(int i = 0; i < userNameAndIDResultsList.size(); i++) {
            NameAndID naid = userNameAndIDList.get(i);  // retrieve NameAndID object from list
            final String userID = naid.getID();        // get the user ID from the object
            final String name = naid.getName();        // get the user's name from the object
            final String username = usernameResultsList.get(i); // get the username from the list

            Log.d("load profile pic", "about to load from storage");
            StorageReference profilePicStorageRef = FirebaseStorage.getInstance().getReference("profile_images/" + userID);
            profilePicStorageRef.getBytes(1024 * 1024 * 5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // user has profile pic --> display it
                    Log.d("loadUserProfileImage()", "getBytes successful");
                    byte[] profileImageBytes = bytes;
                    Log.d("loadUserProfileImage()", "convert bytes to bitmap");
                    Bitmap profileImageBitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.length);
                    //profileImage.setImageBitmap(profileImageBitmap);
                    UserWithImage uwi = new UserWithImage(userID, username, name, profileImageBitmap);
                    userWithImageList.add(uwi);
                    Log.d("userWithImageList", "size = " + userWithImageList.size());
                    if (userWithImageList.size() == userNameAndIDResultsList.size())
                        displayListOfResultsWithImages(userWithImageList, userNameAndIDList);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // User has not set profile image
                    Log.d("loadUserProfileImage()", "Firebase storage exception " + exception.getMessage());
                    Toast.makeText(AddFriendSearchActivity.this, "No profile image", Toast.LENGTH_LONG).show();
                    UserWithImage uwi = new UserWithImage(userID, username, name, null);
                    userWithImageList.add(uwi);
                    Log.d("userWithImageList", "size = " + userWithImageList.size());
                    if (userWithImageList.size() == userNameAndIDResultsList.size())
                        displayListOfResultsWithImages(userWithImageList, userNameAndIDList);
                }
            });
        }
    }


    // populates the listview with results that have a default icon. (No image displayed)
    private void displaySimpleResultsList(final ArrayList<HashMap<String,String>> nameAndUsernameMap,
                                          final ArrayList<String> userIDList){
        Log.d("displaySimpleResults", "entering method");
        SimpleAdapter simpleAdapter = new SimpleAdapter(AddFriendSearchActivity.this, nameAndUsernameMap,
                R.layout.friend_requests_list_items, new String[]{"nameAndUsername"}, new int[]{R.id.friend_requests_text_view} );
        listView.setAdapter(simpleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("clicked on item", listView.getItemAtPosition(position).toString());
                HashMap userInfo = nameAndUsernameMap.get(position);
                String username = userInfo.get("nameAndUsername").toString();
                Log.d("clicked on item", "username: " + username);

                //Toast.makeText(AddFriendSearchActivity.this, "Clicked on " + Integer.toString(view.getId()), Toast.LENGTH_LONG).show();
                Toast.makeText(AddFriendSearchActivity.this, "User: " + username, Toast.LENGTH_LONG).show();

                String userID = userIDList.get(position);
                //String uID = resultsList.get(position).get("username");
                Log.d("userID: ", userID);

                if (userID.equals(currentUserID)) {
                    // user selects themself. Take them to their profile.
                    Intent userProfile = new Intent(AddFriendSearchActivity.this, UserProfileActivity.class);
                    startActivity(userProfile);
                } else {
                    // user can view other user profile
                    Intent viewOtherUserProfile = new Intent(AddFriendSearchActivity.this, ViewOtherUserProfileActivity.class);
                    viewOtherUserProfile.putExtra("userID", userID);
                    startActivity(viewOtherUserProfile);
                }
            }
        });
    }


    // Sorts the list of users with images and displays them in the ListView with an adapter
    private void displayListOfResultsWithImages(ArrayList<UserWithImage>userWithImageList, final ArrayList<NameAndID>userNameAndIDList) {
        // Ensure the "no results" views are gone
        textViewNoResults.setVisibility(View.GONE);
        sadFaceIcon.setVisibility(View.GONE);

        final ArrayList<NameAndID>userNameAndIDResultList = userNameAndIDList;
        Log.d("displayListOfFriends()", "entering method");
        // Sort the userWithImages list so that users appear in order of username
        if (userWithImageComparator == null) {
            userWithImageComparator = new Comparator<UserWithImage>() {
                @Override
                public int compare(UserWithImage u1, UserWithImage u2) {
                    int result = u1.getUsername().compareTo(u2.getUsername());
                    if (result < 0) return -1;
                    else if (result > 0) return 1;
                    else return 0;
                }
            };
        }
        Collections.sort(userWithImageList, userWithImageComparator);

        // Initialize the adapter
        UserProfileListAdapter upla = new UserProfileListAdapter(AddFriendSearchActivity.this, userWithImageList);
        //final ListView listView = (ListView) findViewById(R.id.friends_listview);
        listView.setAdapter(upla);
        // Add on click listener to view the user's profile
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NameAndID naid = userNameAndIDList.get(position);
                String otherUserID = naid.getID();

                if (otherUserID.equals(currentUserID)) {
                    // user selects themself. Take them to their profile.
                    Intent userProfile = new Intent(AddFriendSearchActivity.this, UserProfileActivity.class);
                    startActivity(userProfile);
                } else {
                    // user can view other user profile
                    Intent viewOtherUserProfile = new Intent(AddFriendSearchActivity.this, ViewOtherUserProfileActivity.class);
                    viewOtherUserProfile.putExtra("userID", otherUserID);
                    startActivity(viewOtherUserProfile);
                }
            }
        });
        listView.setVisibility(View.VISIBLE);
    }


}
