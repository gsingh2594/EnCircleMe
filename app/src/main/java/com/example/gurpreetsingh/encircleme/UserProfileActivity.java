package com.example.gurpreetsingh.encircleme;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;



public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private DatabaseReference dbUserRef;
    private User user;
    private ImageView imageView;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private ImageView btnSelectProfile;
    private ImageView btnSelectCover;
    private ImageView ivImage;
    private ImageView coverImage;
    private String userChoosenTask;

    private TextView profileName;
    private TextView profileBio;
    private ImageView editIcon;

    private AlertDialog editBioDialog;

    Button btnAlerts;
    Button btnMaps;
    Button btnProfile;
    Button friends;
    Button btnSetting;

    //Button
    public void Profile() {
        btnProfile = (Button) findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent time = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                startActivity(time);
            }
        });
    }

    public void Alerts(){
        btnAlerts = (Button) findViewById(R.id.btnAlerts);
        btnAlerts.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent alerts = new Intent(UserProfileActivity.this, PlacePickerActivity.class);
                startActivity(alerts);
            }
        });
    }
    public void Maps(){
        btnMaps = (Button) findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent alerts = new Intent(UserProfileActivity.this, MapsActivity.class);
                startActivity(alerts);
            }
        });
    }
    public void Friends(){
        friends = (Button) findViewById(R.id.friends);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent offMaps = new Intent(UserProfileActivity.this, FriendsActivity.class);
                startActivity(offMaps);
            }
        });
    }
    public void Settings(){
        btnSetting = (Button) findViewById(R.id.setting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent setting = new Intent(UserProfileActivity.this, UserActivity.class);
                startActivity(setting);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        imageView = (ImageView) findViewById(android.R.id.icon);
        final LinearLayout interestsLinearLayout = (LinearLayout) findViewById(R.id.interests_linearlayout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();        // root directory of the DB
        dbUserRef = dbRef.child("users").child(uid);

        profileName = (TextView) findViewById(R.id.user_profile_name);
        profileBio = (TextView) findViewById(R.id.user_profile_bio);
        editIcon = (ImageView) findViewById(R.id.edit_bio_icon);

        btnSelectProfile = (ImageView) findViewById(R.id.ivImage);
        btnSelectProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v ) {
                selectImage();
            }
        });
        ivImage = (ImageView) findViewById(R.id.ivImage);

        /*btnSelectCover = (ImageView) findViewById(R.id.header_cover_image);
        btnSelectCover.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        coverImage = (ImageView) findViewById(R.id.header_cover_image);*/

        editIcon.setOnClickListener(this);

        // load user profile from DB
        dbUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                profileName.setText(user.getName());
                // display bio if it has been set
                if(user.getBio()!=null)
                    profileBio.setText(user.getBio());
                // load and display interests if they exist
                if(user.getInterests()!= null){
                    ArrayList<String>userInterests = user.getInterests();
                    for(int i = 0; i < userInterests.size(); i++){
                        // Create and add a new TextView to the LinearLayout
                        TextView interestTextView = new TextView(UserProfileActivity.this);
                        int marginSize = convertDPtoPX(5);
                        LinearLayout.LayoutParams layoutParams =
                                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        layoutParams.setMargins(marginSize,marginSize,marginSize,marginSize);
                        interestTextView.setLayoutParams(layoutParams);
                        interestTextView.setTextSize(16);
                        interestTextView.setText(userInterests.get(i));
                        interestTextView.setElevation((float)convertDPtoPX(4));
                        int paddingSize = convertDPtoPX(20);
                        interestTextView.setPadding(paddingSize,paddingSize,paddingSize,paddingSize);
                        interestTextView.setBackgroundColor(Color.WHITE);
                        interestsLinearLayout.addView(interestTextView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UserProfileActivity.this, "Data could not be retrieved", Toast.LENGTH_SHORT).show();
            }
        });

        Alerts();
        Maps();
        Friends();
        Profile();
        Settings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_editprofile) {
            Intent modifySettings=new Intent(UserProfileActivity.this,CreateUserProfileActivity.class);
            startActivity(modifySettings);
        }
        if (id == R.id.settings){
            Intent modifySettings=new Intent(UserProfileActivity.this,UserActivity.class);
            startActivity(modifySettings);
        }
        if (id == R.id.logout) {
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Would you like to logout?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            startActivity(new Intent(UserProfileActivity.this, MainActivity.class));
                            //finish();
                        }
                    }).create().show();

        }
        return super.onOptionsItemSelected(item);
    }
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bitmap = getPath(data.getData());
            imageView.setImageBitmap(bitmap);
        }
    }


    public void selectImage(View view) {
        Intent intent = new Intent();
        intent.setType("image*//*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(userChoosenTask.equals("Take Photo"))
                        cameraIntent();
                    else if(userChoosenTask.equals("Choose from Library"))
                        galleryIntent();
                } else {
                    //code for deny
                }
                break;
        }
    }

    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(UserProfileActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result=Utility.checkPermission(UserProfileActivity.this);

                if (items[item].equals("Take Photo")) {
                    userChoosenTask ="Take Photo";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask ="Choose from Library";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Remove")) {
/*                    userChoosenTask ="Remove";
                    if(result)
                        galleryIntent();*/
                    dialog.dismiss();

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);
    }

    private void cameraIntent()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE)
                onSelectFromGalleryResult(data);
            else if (requestCode == REQUEST_CAMERA)
                onCaptureImageResult(data);
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File destination = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ivImage.setImageBitmap(bitmap);
    }


    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ivImage.setImageBitmap(bm);
    }

    private Bitmap getPath(Uri uri) {

        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(column_index);
        // cursor.close();
        // Convert file path into bitmap image using below line.
        Bitmap bitmap = BitmapFactory.decodeFile(filePath);

        return bitmap;
    }

    public void showEditDialog(){
        final AlertDialog.Builder editBioDialogBuilder = new AlertDialog.Builder(UserProfileActivity.this);
        editBioDialogBuilder.setTitle("Edit Bio");
        EditText textBox = new EditText(this);
        textBox.setId(R.id.edit_bio_text);
        textBox.setText(user.getBio());
        editBioDialogBuilder.setView(textBox);
        editBioDialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int ID){
                // User clicked save button
                saveUserBio();
                Intent refreshProfile = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                startActivity(refreshProfile);
            }
        });
        editBioDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        editBioDialog = editBioDialogBuilder.create();
        editBioDialog.show();
    }

    public void saveUserBio(){
        EditText dialogTextBox = (EditText) editBioDialog.findViewById(R.id.edit_bio_text);
        String bio = dialogTextBox.getText().toString().trim();
        user.setBio(bio);

        // dbUserRef.setValue(user);    --> Unnecessary because it rewrites the whole User object to the DB
        // use this line instead to update only the "bio" value
        dbUserRef.child("bio").setValue(bio);
    }


    @Override
    public void onClick(View v) {
        if(v == editIcon){
            showEditDialog();
        }
    }


    // used to set sizes in dp units programmatically. (Some views set sizes programmtically in px, not dp)
    // We should use this method to make certain views display consistently on different screen densities
    private int convertDPtoPX(int sizeInDP){
        float scale = getResources().getDisplayMetrics().density;       // note that 1dp = 1px on a 160dpi screen
        int dpAsPixels = (int) (sizeInDP * scale + 0.5f);
        return dpAsPixels;  // return the size in pixels
    }


    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
