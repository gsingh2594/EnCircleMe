package com.example.gurpreetsingh.encircleme;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;



public class PlacePickerActivity extends AppCompatActivity {
    private static final int PLACE_PICKER_REQUEST = 1;
    private TextView mName;
    private TextView mAddress;
    private TextView mLatLng;
    private TextView mAttributions;
    private static final LatLngBounds newyork = new LatLngBounds(
            new LatLng(40.758879, -73.985110),
            new LatLng(40.758879, -73.985110));

    ImageButton btnAlerts;
    ImageButton btnMaps;
    ImageButton btnProfile;
    ImageButton friends;
    ImageButton btnSetting;


    //Button
    public void Profile() {
        btnProfile = (ImageButton) findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent time = new Intent(PlacePickerActivity.this, UserProfileActivity.class);
                startActivity(time);
            }
        });
    }

    public void Alerts() {
        btnAlerts = (ImageButton) findViewById(R.id.btnAlerts);
        btnAlerts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alerts = new Intent(PlacePickerActivity.this, PlacePickerActivity.class);
                startActivity(alerts);
            }
        });
    }

    public void Maps() {
        btnMaps = (ImageButton) findViewById(R.id.btnMaps);
        btnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent alerts = new Intent(PlacePickerActivity.this, MapsActivity.class);
                startActivity(alerts);
            }
        });
    }

    public void Friends() {
        friends = (ImageButton) findViewById(R.id.friends);
        friends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent offMaps = new Intent(PlacePickerActivity.this, SearchActivity.class);
                startActivity(offMaps);
            }
        });
    }

    public void Settings() {
        btnSetting = (ImageButton) findViewById(R.id.setting);
        btnSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent setting = new Intent(PlacePickerActivity.this, UserActivity.class);
                startActivity(setting);
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);
        mName = (TextView) findViewById(R.id.textView);
        mAddress = (TextView) findViewById(R.id.textView2);
        mLatLng = (TextView) findViewById(R.id.textView8);
        mAttributions = (TextView) findViewById(R.id.textView3);
        Button pickerButton = (Button) findViewById(R.id.pickerButton);
        pickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                    intentBuilder.setLatLngBounds(newyork);
                    Intent intent = intentBuilder.build(PlacePickerActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);

                } catch (GooglePlayServicesRepairableException
                        | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(this, data);
            final CharSequence name = place.getName();
            final CharSequence address = place.getAddress();
            final String latlng = place.getLatLng().toString();
            String attributions = (String) place.getAttributions();
            if (attributions == null) {
                attributions = "";
            }

            mName.setText(name);
            mAddress.setText(address);
            mLatLng.setText(latlng);
            mAttributions.setText(Html.fromHtml(attributions));


        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}