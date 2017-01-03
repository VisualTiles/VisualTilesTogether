package com.javierarboleda.visualtilestogether.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.adapters.ShowChannelPagerAdapter;
import com.javierarboleda.visualtilestogether.fragments.ShowChannelFragment;

public class ShowChannelActivity extends BaseVisualTilesActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean isModerator = ((VisualTilesTogetherApp) getApplication()).isChannelModerator();

        setContentView(isModerator?
                R.layout.activity_show_channel_moderator
                : R.layout.activity_show_channel);
        super.setTopViewGroup((ViewGroup) findViewById(isModerator?
                R.id.activity_show_qr_code_moderator
                : R.id.activity_show_qr_code));

        String uniqueName = app.getChannel().getUniqueName();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(app.getChannel().getName());
            actionBar.setSubtitle(uniqueName);
        }

        if (isModerator) {
            if (savedInstanceState == null) {
                ViewPager mViewPager = (ViewPager) findViewById(R.id.vpShowChannelContainer);
                mViewPager.setAdapter(new ShowChannelPagerAdapter(getSupportFragmentManager(), uniqueName));
                TabLayout tabLayout = (TabLayout) findViewById(R.id.tlTabs);
                tabLayout.setupWithViewPager(mViewPager);
            }
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.show_channel_container,
                            ShowChannelFragment.newInstance(uniqueName))
                    .commit();
        }
    }
}