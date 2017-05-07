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
                EncircledEventActivity tab3 = new EncircledEventActivity();
                return tab3;
            case 3:
                EventInvitesFragment tab4 = new EventInvitesFragment();
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