package com.example.gurpreetsingh.encircleme;

import android.annotation.SuppressLint;
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
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.google.firebase.storage.UploadTask;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth auth;
    private String userID;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private DatabaseReference dbUserRef;
    private User user;

    private FirebaseStorage fbStorage;
    private StorageReference fbStorageRef;
    private static final long ONE_MEGABYTE = 1024 * 1024;

    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private ImageView profileImage;
    private ImageView coverImage;
    private boolean profileImageSelected;
    private boolean coverImageSelected;
    private byte[] profileImageBytes;
    private byte[] coverImageBytes;
    private Bitmap profileImageBitmap, coverImageBitmap;

    private String userChoosenTask;

    private TextView profileName;
    private TextView profileBio;
    private TextView profileEmail;
    private TextView profilePhone;
    private TextView profileUsername;
    private ImageView editIcon;
    private AlertDialog editBioDialog;

    private BottomBar bottomBar;
    private OnTabSelectListener tabSelectListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Log.d("onCreate()", "UserProfileActivity created");

        Toolbar toolbar = (Toolbar) findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);
        setTitle("Profile");
        ActionBar actionBar = getSupportActionBar();

        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();        // root directory of the DB
        dbUserRef = dbRef.child("users").child(userID);

        fbStorage = FirebaseStorage.getInstance();
        fbStorageRef = fbStorage.getReference();

        profileName = (TextView) findViewById(R.id.user_profile_name);
        profileBio = (TextView) findViewById(R.id.user_profile_bio);
        editIcon = (ImageView) findViewById(R.id.edit_bio_icon);
        profileEmail = (TextView) findViewById(R.id.email);
        profilePhone = (TextView) findViewById(R.id.phone_number);
        profileUsername = (TextView) findViewById(R.id.username);

        profileImage = (ImageView) findViewById(R.id.profile_image);
        coverImage = (ImageView) findViewById(R.id.cover_image);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileImageSelected = true;
                selectImage();
            }
        });
        coverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                coverImageSelected = true;
                selectImage();
            }
        });
        profileImageSelected = false;
        coverImageSelected = false;

        editIcon.setOnClickListener(this);

        loadUserProfile();
        loadUserProfileImage();
        loadUserCoverImage();

        bottomBar = (BottomBar) findViewById(R.id.bottomBar);
        bottomBar.setDefaultTab(R.id.tab_profile);
        tabSelectListener = new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                /*if (tabId == R.id.tab_profile) {
                    Intent profile = new Intent(UserProfileActivity.this, UserProfileActivity.class);
                    startActivity(profile); */
                if (tabId == R.id.tab_friends) {
                    Log.d("bottomBar", "friends clicked");
                    Intent friends = new Intent(UserProfileActivity.this, FriendsActivity.class);
                    startActivity(friends);
                } else if (tabId == R.id.tab_map) {
                    Intent map = new Intent(UserProfileActivity.this, MapsActivity.class);
                    startActivity(map);
                } else if (tabId == R.id.tab_alerts) {
                    Intent events = new Intent(UserProfileActivity.this, EventsTabActivity.class);
                    startActivity(events);
                } else if (tabId == R.id.tab_chats) {
                    Intent events = new Intent(getApplicationContext(), ChatActivity.class);
                    startActivity(events);
                }
            }
        };
        bottomBar.setOnTabSelectListener(tabSelectListener);
    }

    @Override
    protected void onDestroy() {
        Log.d("onDestroy()", "UserProfileActivity destroyed");
        super.onDestroy();
    }

    // load user profile from DB
    private void loadUserProfile() {
        final LinearLayout interestsLinearLayout = (LinearLayout) findViewById(R.id.interests_linearlayout);
        final LinearLayout interestsLinearLayout2 = (LinearLayout) findViewById(R.id.interests_linearlayout2);
        dbUserRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                profileName.setText(user.getName());
                profileEmail.setText(user.getEmail());
                profilePhone.setText(user.getPhone());
                profileUsername.setText("Username: " + user.getUsername());
                // display bio if it has been set
                if (user.getBio() != null)
                    profileBio.setText(user.getBio());

                // load and display interests in a grid if they exist
                if (user.getInterests() != null) {
                    ArrayList<String> userInterests = user.getInterests();

                    int interestsGridMax = userInterests.size() >= 3? 6 : 3;
                    int imageSizeinPX = convertDPtoPX(120);

                    for (int i = 0; i < interestsGridMax; i++) {
                        // Create and add a new TextView to the LinearLayout
                        ImageView interestImageView= new ImageView(UserProfileActivity.this);
                        interestImageView.getMeasuredHeight();
                        int marginSize = convertDPtoPX(4);
                        LinearLayout.LayoutParams layoutParams =
                               /* new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT, 1);*/
                                new LinearLayout.LayoutParams(imageSizeinPX, imageSizeinPX);
                        layoutParams.setMargins(marginSize, marginSize, marginSize, marginSize);
                        interestImageView.setLayoutParams(layoutParams);

                        /*interestImageView.setTextSize(15);
                        interestImageView.setTextColor(Color.BLACK);
                        interestImageView.setText(userInterests.get(i));
                        interestImageView.setElevation((float) convertDPtoPX(2));
                        interestImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        int paddingSize = convertDPtoPX(10);
                        interestImageView.setPadding(paddingSize, paddingSize, paddingSize, paddingSize);*/
                        interestImageView.setBackgroundColor(Color.WHITE);

                        if(i < userInterests.size()) {
                            switch (userInterests.get(i)) {
                                case "Movie Theatres":
                                    interestImageView.setImageResource(R.drawable.movies);
                                    break;
                                case "Art Gallery":
                                    interestImageView.setImageResource(R.drawable.artgallery);
                                    break;
                                case "Cafe":
                                    interestImageView.setImageResource(R.drawable.cafe);
                                    break;
                                case "Bars":
                                    interestImageView.setImageResource(R.drawable.bars);
                                    break;
                                case "Restaurants":
                                    interestImageView.setImageResource(R.drawable.restaurants);
                                    break;
                                case "Department Stores":
                                    interestImageView.setImageResource(R.drawable.deptstores);
                                    break;
                            }
                        }
                        if (i < 3){
                            interestsLinearLayout.addView(interestImageView);
                        }
                        else
                            interestsLinearLayout2.addView(interestImageView);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UserProfileActivity.this, "Data could not be retrieved", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // load user profile image from Firebase Storage
    private void loadUserProfileImage() {
        Log.d("load profile pic", "about to load from storage");
        StorageReference profilePicStorageRef = fbStorage.getReference("profile_images/" + userID);
        profilePicStorageRef.getBytes(ONE_MEGABYTE * 5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // user has profile pic --> display it
                Log.d("loadUserProfileImage()", "getBytes successful");
                profileImageBytes = bytes;
                Log.d("loadUserProfileImage()", "convert bytes to bitmap");
                profileImageBitmap = BitmapFactory.decodeByteArray(profileImageBytes, 0, profileImageBytes.length);
                profileImage.setImageBitmap(profileImageBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // User has not set profile image or storage error
                Log.d("loadUserProfileImage()", "Firebase storage exception " + exception.getMessage());
                //Toast.makeText(UserProfileActivity.this, "No profile image", Toast.LENGTH_LONG).show();
            }
        });
    }


    // load user cover image from Firebase Storage
    private void loadUserCoverImage() {
        Log.d("load profile pic", "about to load from storage");
        StorageReference profilePicStorageRef = fbStorage.getReference("cover_images/" + userID);
        profilePicStorageRef.getBytes(ONE_MEGABYTE * 5).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // user has profile pic --> display it
                Log.d("loadUserCoverImage()", "getBytes successful");
                coverImageBytes = bytes;
                Log.d("loadUserCoverImage()", "convert bytes to bitmap");
                coverImageBitmap = BitmapFactory.decodeByteArray(coverImageBytes, 0, coverImageBytes.length);
                coverImage.setImageBitmap(coverImageBitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // User has not set cover image
                Log.d("loadUserProfileImage()", "Firebase storage exception " + exception.getMessage());
                //Toast.makeText(UserProfileActivity.this, "No cover image", Toast.LENGTH_LONG).show();
            }
        });
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
            Intent modifySettings=new Intent(UserProfileActivity.this,EditUserProfileActivity.class);
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
                            FirebaseAuth.getInstance().signOut();
                            stopService(MapsActivity.notificationService);
                            startActivity(new Intent(UserProfileActivity.this, MainActivity.class));
                            //finish();
                        }
                    }).create().show();

        }
        return super.onOptionsItemSelected(item);
    }

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
                    profileImageSelected = false;
                    coverImageSelected = false;
                    dialog.dismiss();
                }
            }
        });

        builder.show();
    }


    private void galleryIntent()
    {
        /*Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"),SELECT_FILE);*/
        Intent pickImageIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        pickImageIntent.setType("image/*");
        pickImageIntent.putExtra("crop", "true");
        pickImageIntent.putExtra("outputX", 450);
        pickImageIntent.putExtra("outputY", 450);
        pickImageIntent.putExtra("aspectX", 1);
        pickImageIntent.putExtra("aspectY", 1);
        pickImageIntent.putExtra("scale", true);
        pickImageIntent.putExtra(MediaStore.EXTRA_OUTPUT,  fileUri);
        pickImageIntent.putExtra("outputFormat",

                Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(pickImageIntent, SELECT_FILE);
    }

    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    @SuppressLint("SimpleDateFormat")
    private static File getOutputMediaFile(int type){

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "EnCircleMe");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("EnCircleMe", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
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
        if(profileImageSelected) {
            uploadProfileImageToFirebaseStorage(bitmap);
            // NOTE: any code after this line might run before the image has been uploaded
            profileImageSelected = false;
        }
        else if(coverImageSelected){
            uploadCoverImageToFirebaseStorage(bitmap);
            // NOTE: any code after this line might run before the image has been uploaded
            coverImageSelected = false;
        }
    }


    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                Log.d("selectFromGallery", "creating bitmap");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                Log.d("selectFromGallery", "scaling bitmap");
                //int imageRatio = bm.getWidth() / bm.getHeight();
                //bm = Bitmap.createScaledBitmap(bm, 450 * imageRatio, 450 , false);
                bm.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d("About to upload image", "profileImageSelected: " + profileImageSelected
                + "\n" + "coverImageSelected: " + coverImageSelected);
        if(profileImageSelected) {
            uploadProfileImageToFirebaseStorage(bm);
            // NOTE: any code after this line might run before the image has been uploaded
            profileImageSelected = false;
        }
        else if(coverImageSelected){
            uploadCoverImageToFirebaseStorage(bm);
            // NOTE: any code after this line might run before the image has been uploaded
            coverImageSelected = false;
        }
    }


    // upload bitmap profile image to Firebase Storage. Displays profile image if successful
    private void uploadProfileImageToFirebaseStorage(final Bitmap bitmap) {
        /* // Get the data from an ImageView as bytes
        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        Log.d("profile image size ", Integer.toString(data.length) + " bytes");
        // Check if compressed image size is larger than 5 MB
        if (data.length > ONE_MEGABYTE * 5)
            alertImageSizeTooLarge();
        else {
            StorageReference profilePicStorageRef = fbStorage.getReference("profile_images/" + userID);

            Log.d("uploadProfileImage()", "about to putBytes()");
            UploadTask uploadTask = profilePicStorageRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d("uploadProfileImage()", "upload failed");
                    Toast.makeText(UserProfileActivity.this, "Upload failed", Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Upload successful
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    // Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(UserProfileActivity.this, "Upload Successful", Toast.LENGTH_LONG).show();
                    // display profile image
                    profileImage.setImageBitmap(bitmap);
                }
            });
        }
    }


    // upload bitmap cover image to Firebase Storage. Displays cover image if successful
    private void uploadCoverImageToFirebaseStorage(final Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        Log.d("cover image size ", Integer.toString(data.length) + " bytes");
        // Check if compressed image size is larger than 5 MB
        if (data.length > ONE_MEGABYTE * 5)
            alertImageSizeTooLarge();
        else {
            StorageReference profilePicStorageRef = fbStorage.getReference("cover_images/" + userID);

            Log.d("uploadCoverImage()", "about to putBytes()");
            UploadTask uploadTask = profilePicStorageRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Log.d("uploadCoverImage()", "upload failed");
                    Toast.makeText(UserProfileActivity.this, "Upload failed", Toast.LENGTH_LONG).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Upload successful
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    // Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(UserProfileActivity.this, "Upload Successful", Toast.LENGTH_LONG).show();
                    // display cover image
                    coverImage.setImageBitmap(bitmap);
                }
            });
        }
    }


    private void alertImageSizeTooLarge(){
        AlertDialog.Builder alert = new AlertDialog.Builder(UserProfileActivity.this);
        alert.setMessage("Image size is too large! Please select an image less than 5MB");
        alert.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alert.show();
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
        final String bio = dialogTextBox.getText().toString().trim();

        // dbUserRef.setValue(user);    --> Unnecessary because it rewrites the whole User object to the DB
        // use this line instead to update only the "bio" value
        dbUserRef.child("bio").setValue(bio, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                profileBio.setText(bio);
                Toast.makeText(UserProfileActivity.this, "Saved", Toast.LENGTH_LONG).show();
            }
        });
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
    protected void onResume() {
        super.onResume();
        // bottomBar.removeOnTabReselectListener();
        bottomBar.setDefaultTab(R.id.tab_profile);
        // bottomBar.setOnTabSelectListener(tabSelectListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //profileImageBitmap.recycle();
        //coverImageBitmap.recycle();
    }
}
