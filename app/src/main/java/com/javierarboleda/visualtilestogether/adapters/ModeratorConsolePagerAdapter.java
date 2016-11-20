package com.javierarboleda.visualtilestogether.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.javierarboleda.visualtilestogether.fragments.ColorSelectFragment;
import com.javierarboleda.visualtilestogether.fragments.EffectSelectFragment;
import com.javierarboleda.visualtilestogether.fragments.TileSelectFragment;

/**
 * Created on 11/15/16.
 */

public class ModeratorConsolePagerAdapter extends FragmentPagerAdapter {

    public ModeratorConsolePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return TileSelectFragment.newInstance();
            case 1:
                return new EffectSelectFragment();
//            case 2:
//                return new ColorSelectFragment();
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
                return "Tiles";
            case 1:
                return "Effects";
//            case 2:
//                return "Color";
        }

        return super.getPageTitle(position);
    }
}
