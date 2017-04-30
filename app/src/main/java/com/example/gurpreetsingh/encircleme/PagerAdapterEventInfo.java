package com.example.gurpreetsingh.encircleme;

/**
 * Created by GurpreetSingh on 4/26/17.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapterEventInfo extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapterEventInfo(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                EventInfoActivity tab1 = new EventInfoActivity();
                return tab1;
            case 1:
                TabFragment2 tab2 = new TabFragment2();
                return tab2;
            /*case 2:
                TabFragment1 tab3 = new TabFragment1();
                return tab3;*/
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}