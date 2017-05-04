package com.example.gurpreetsingh.encircleme;

/**
 * Created by GurpreetSingh on 4/26/17.
 */
        import android.support.v4.app.Fragment;
        import android.support.v4.app.FragmentManager;
        import android.support.v4.app.FragmentStatePagerAdapter;

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                EventListActivity tab1 = new EventListActivity();
                return tab1;
            case 1:
                UserEventActivity tab2 = new UserEventActivity();
                return tab2;
            case 2:
                TabFragment1 tab3 = new TabFragment1();
                return tab3;
            case 3:
                TabFragment1 tab4 = new TabFragment1();
                return tab4;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}