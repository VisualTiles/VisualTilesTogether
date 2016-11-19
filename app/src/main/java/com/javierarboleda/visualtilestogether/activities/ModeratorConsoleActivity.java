package com.javierarboleda.visualtilestogether.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.adapters.ModeratorConsolePagerAdapter;
import com.javierarboleda.visualtilestogether.databinding.ActivityModeratorConsoleBinding;
import com.javierarboleda.visualtilestogether.fragments.PresentationFragment;
import com.javierarboleda.visualtilestogether.models.Tile;

/**
 * Created on 11/15/16.
 */

public class ModeratorConsoleActivity extends AppCompatActivity
                implements PresentationFragment.PresentationFragmentListener {

    ActivityModeratorConsoleBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_moderator_console);

        setUpToolbar();

        setUpTabLayout();

        PresentationFragment mPresentationFragment = PresentationFragment.newInstance(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolderMc, mPresentationFragment)
                .commit();

    }

    private void setUpTabLayout() {

        ViewPager viewPager = binding.viewPager;
        ModeratorConsolePagerAdapter pagerAdapter =
                new ModeratorConsolePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        binding.tabLayout.setupWithViewPager(viewPager);
    }

    private void setUpToolbar() {
        setSupportActionBar(binding.toolbar);
    }

    @Override
    public void onTileTapped(int position, Tile tile) {

    }

//    @Override
//    protected void onResumeFragments() {
//        super.onResumeFragments();
//        resizeFragment(mPresentationFragment, 100, 100);
//    }
}
