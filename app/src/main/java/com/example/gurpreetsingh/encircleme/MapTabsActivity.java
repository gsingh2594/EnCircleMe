package com.example.gurpreetsingh.encircleme;

/**
 * Created by GurpreetSingh on 4/2/17.
 */

import android.os.Bundle;
        import android.support.design.widget.TabLayout;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;

public class MapTabsActivity extends AppCompatActivity{


    Toolbar toolbar;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_tabs_layout);
        initInstancesDrawer();
    }

    private void initInstancesDrawer() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Tab One"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab Two"));
    }
}