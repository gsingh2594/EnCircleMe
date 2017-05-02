package com.example.gurpreetsingh.encircleme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Brayden on 4/28/2017.
 *
 * This class should be used to display user's in a ListView where
 * each row contains a cropped circle of their profile image with their
 * name and username displayed on the right side. If names are not provided,
 */

public class UserProfileListAdapter extends BaseAdapter {
    List<UserWithImage> usersList;
    Context context;

    public UserProfileListAdapter(Context context, List<UserWithImage> usersList) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("getView", "entering");
        View row = convertView;
        if (row == null) {
            Log.d("getView", "creating row");
            // inflate the layout used for each row in the list
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.friend_requests_list_items, parent, false);
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
            profileImageView.setBackground(null);
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
        return row;
    }

    // Returns a bitmap as a cropped circle
    public static Bitmap getClip(Bitmap bitmap) {
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
