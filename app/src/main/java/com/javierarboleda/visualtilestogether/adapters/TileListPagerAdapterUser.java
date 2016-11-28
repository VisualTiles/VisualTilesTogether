package com.javierarboleda.visualtilestogether.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.javierarboleda.visualtilestogether.fragments.TileListFragmentInChannel;
import com.javierarboleda.visualtilestogether.fragments.TileListFragmentMine;
import com.javierarboleda.visualtilestogether.fragments.TileListFragmentNowPlaying;

public class TileListPagerAdapterUser extends FragmentPagerAdapter {

    public TileListPagerAdapterUser(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: {
                return new TileListFragmentInChannel();
            }
            case 1: {
                return new TileListFragmentMine();
            }
            case 2: {
                return new TileListFragmentNowPlaying();
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "All";
            case 1:
                return "Mine";
            case 2:
                return "Now Playing";
        }
        return null;
    }
}