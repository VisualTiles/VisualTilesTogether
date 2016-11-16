package com.javierarboleda.visualtilestogether.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.javierarboleda.visualtilestogether.fragments.TileListFragmentApproved;
import com.javierarboleda.visualtilestogether.fragments.TileListFragmentInChannel;

public class TileListPagerAdapter extends FragmentPagerAdapter {

    public TileListPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                return new TileListFragmentInChannel();
            }
            case 1: {
                return new TileListFragmentApproved();
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
                return "All";
            case 1:
                return "Now Playing";
        }
        return null;
    }
}