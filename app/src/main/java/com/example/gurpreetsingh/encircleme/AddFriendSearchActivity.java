package com.example.gurpreetsingh.encircleme;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class AddFriendSearchActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference dbRef;
    ListView listView;

    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.add_friends_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setTitle("Add Friends");
        actionBar.setDisplayShowTitleEnabled(false);
        auth = FirebaseAuth.getInstance();
        currentUserID = auth.getCurrentUser().getUid();
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
        inflater.inflate(R.menu.add_friend_search_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_for_friends_icon).getActionView();
        // Assumes current activity is the searchable activity
        Log.d("searchView", searchView.toString());
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
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

    //TODO: Try to add search suggestions

    private void doMySearch(String query){
        firebaseDatabase = FirebaseDatabase.getInstance();
        dbRef = firebaseDatabase.getReference("usernames");
        Log.d("doMySearch", "dbRef created: " + dbRef.toString());

        listView = (ListView)findViewById(R.id.friend_search_listview);
        final ArrayList<HashMap<String, String>> resultsList = new ArrayList<HashMap<String,String>>();
        final ArrayList<String> userIDList = new ArrayList<String>();

        Log.d("doMySearch", "about to add listener");

        dbRef.orderByKey()
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
                    Log.d("snapshot exists", "");
                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                        Log.d("onDataChange", "Adding child from DB to resultsList");
                        HashMap<String, String> hashMap = new HashMap<String, String>();
                        hashMap.put("username", user.getKey());
                        // hashMap.put("uid", user.getValue().toString());
                        resultsList.add(hashMap);
                        Log.d("adding userID", user.getValue().toString());
                        userIDList.add(user.getValue().toString());
                    }
                    textViewNoResults.setVisibility(View.GONE);
                    sadFaceIcon.setVisibility(View.GONE);

                    SimpleAdapter simpleAdapter = new SimpleAdapter(AddFriendSearchActivity.this, resultsList,
                            R.layout.friends_search_list_items, new String[]{"username"}, new int[]{R.id.friends_search_text_view} );
                    listView.setAdapter(simpleAdapter);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Log.d("clicked on item", listView.getItemAtPosition(position).toString());
                            HashMap userInfo = resultsList.get(position);
                            String username = userInfo.get("username").toString();
                            Log.d("clicked on item", "username: " + username);

                            Toast.makeText(AddFriendSearchActivity.this, "Clicked on " + Integer.toString(view.getId()), Toast.LENGTH_LONG).show();
                            Toast.makeText(AddFriendSearchActivity.this, "Username: " + username, Toast.LENGTH_LONG).show();

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
                    listView.setVisibility(View.VISIBLE);
                }else{
                    Log.d("creating text view", "no users found");
                    /*
                    TextView textView = new TextView(AddFriendSearchActivity.this);
                    textView.setText("No results found");
                    textView.setTextSize(R.dimen.text_large);
                    //textView.setPadding(16, 16, 0, 0);
                    textView.setId(R.id.no_results_found);
                    Log.d("creating text view", "textView id has been set");
                    listView.addFooterView(textView);
                    */
                    listView.setVisibility(View.GONE);
                    textViewNoResults.setVisibility(View.VISIBLE);
                    sadFaceIcon.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
