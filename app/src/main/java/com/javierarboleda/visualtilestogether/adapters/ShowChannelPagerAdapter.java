package com.javierarboleda.visualtilestogether.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.javierarboleda.visualtilestogether.fragments.ShowChannelFragment;
import com.javierarboleda.visualtilestogether.fragments.UserListFragment;

public class ShowChannelPagerAdapter extends FragmentPagerAdapter {

    private final String mUniqueName;

    public ShowChannelPagerAdapter(FragmentManager fm, String uniqueName) {
        super(fm);
        mUniqueName = uniqueName;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                return ShowChannelFragment.newInstance(mUniqueName);
            }
            case 1: {
                return new UserListFragment();
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Channel";
            case 1:
                return "Users";
        }
        return null;
    }
}