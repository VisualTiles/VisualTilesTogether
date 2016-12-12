package com.javierarboleda.visualtilestogether.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.adapters.TileListPagerAdapterModerator;
import com.javierarboleda.visualtilestogether.adapters.TileListPagerAdapterUser;
import com.javierarboleda.visualtilestogether.fragments.TileListFragment;
import com.javierarboleda.visualtilestogether.models.Tile;

import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.normalizeDb;

public class TileListActivity extends BaseVisualTilesActivity
        implements TileListFragment.TileListFragmentListener {
    private static final String LOG_TAG = TileListActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private FloatingActionButton fab;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_list);
        super.setTopViewGroup((ViewGroup) findViewById(R.id.drawer_layout));
        setUpToolbar();

        setUpNavDrawer();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TileListActivity.this, TileCreationActivity.class);
                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeSceneTransitionAnimation(TileListActivity.this, fab, "fabanimation");
                startActivity(intent, options.toBundle());
            }
        });

        if (savedInstanceState == null) {
            ViewPager mViewPager = (ViewPager) findViewById(R.id.vpContainer);
            mViewPager.setAdapter(app.isChannelModerator()?
                    new TileListPagerAdapterModerator(getSupportFragmentManager())
                    : new TileListPagerAdapterUser(getSupportFragmentManager()));
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tlTabs);
            tabLayout.setupWithViewPager(mViewPager);
        }
    }

    private void setUpNavDrawer() {

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = setupDrawerToggle();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.addDrawerListener(mDrawerToggle);

        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.drawer_open,
                R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.getMenu()
                .setGroupVisible(R.id.nav_item_moderator_console, app.isChannelModerator());
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    private void selectDrawerItem(MenuItem menuItem) {
        if (menuItem.getGroupId() == R.id.nav_item_moderator_console && !app.isChannelModerator()) {
            return;
        }
        switch (menuItem.getItemId()) {
            case R.id.nav_present:
                startActivity(new Intent(this, PresentationActivity.class));
                break;
            case R.id.nav_console:
                startActivity(new Intent(this, ModeratorConsoleActivity.class));
                break;
            case R.id.nav_normalize_db:
                normalizeDb(getApplicationContext());
                break;
            case R.id.nav_user_invite:
                sendAppInvite();
                break;
            case R.id.nav_show_code:
                startActivity(new Intent(this, ShowQrCodeActivity.class));
                break;
            case R.id.nav_leave_channel:
                app.leaveChannel();
                launchChannelCreateActivity();
                exitCircularReveal(fab, true);
                break;
            case R.id.nav_sign_out:
                signOut();
                exitCircularReveal(fab, true);
                break;
            case android.R.id.home:
                exitCircularReveal(fab, true);
                break;
        }
        mDrawer.closeDrawers();
    }

    private void setUpToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();

        actionBar.setTitle(app.getChannel().getName());
        actionBar.setSubtitle(app.getChannel().getUniqueName());
    }

    private void goToJoinActivity() {
        final Intent intent = new Intent(this, CreateJoinActivity.class);
        Display display = getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        int cx = size.x / 2;
        int cy = size.y / 2;

        intent.putExtra(CreateJoinActivity.CX_KEY, cx);
        intent.putExtra(CreateJoinActivity.CY_KEY, cy);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
            }
        }, 350);

        exitCircularReveal(fab, true);
    }

    private void enterCircularReveal(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            // get the center for the clipping circle
            int cx = view.getMeasuredWidth() / 2;
            int cy = view.getMeasuredHeight() / 2;

            // get the final radius for the clipping circle
            int finalRadius = Math.max(view.getWidth(), view.getHeight()) / 2;

            // create the animator for this view (the start radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);

            // make the view visible and start the animation
            view.setVisibility(View.VISIBLE);
            anim.start();
        }
    }

    private void exitCircularReveal(final View view, final boolean finish) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            // get the center for the clipping circle
            int cx = view.getMeasuredWidth() / 2;
            int cy = view.getMeasuredHeight() / 2;

            // get the initial radius for the clipping circle
            int initialRadius = view.getWidth() / 2;

            // create the animation (the final radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    view.setVisibility(View.INVISIBLE);
                    if (finish) {
                        supportFinishAfterTransition();
                    }
                }
            });

            // start the animation
            anim.start();
        }
    }

    @Override
    public void onBackPressed() {
        exitCircularReveal(fab, true);
    }

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        enterCircularReveal(fab);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateSelectedTile(Tile tile) {
        // Doing nothing here
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}
