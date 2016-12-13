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
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.adapters.TileListPagerAdapterModerator;
import com.javierarboleda.visualtilestogether.adapters.TileListPagerAdapterUser;
import com.javierarboleda.visualtilestogether.fragments.TileListFragment;
import com.javierarboleda.visualtilestogether.models.Tile;

import java.text.NumberFormat;

import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.normalizeDb;

public class TileListActivity extends BaseVisualTilesActivity
        implements TileListFragment.TileListFragmentListener {
    private static final String LOG_TAG = TileListActivity.class.getSimpleName();

    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private FloatingActionButton fab;
    private TextView tvUsersOnline;
    private View usersOnlineHolder;

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

        initChannelUserCounter();
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
        /*
        LayoutInflater mInflater= LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.toolbar_activity_main, null);
        mToolbar.addView(mCustomView);
        */
        setSupportActionBar(mToolbar);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        tvUsersOnline = (TextView) mToolbar.findViewById(R.id.tvUsersOnline);
        usersOnlineHolder = mToolbar.findViewById(R.id.usersOnline);
        /*
        actionBar.setTitle(app.getChannel().getName());
        actionBar.setSubtitle(app.getChannel().getUniqueName());
        */
        TextView tvTitle = (TextView) mToolbar.findViewById(R.id.tvTitle);
        tvTitle.setText(app.getChannel().getName());

        // Open drawer on title click.
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mDrawer.isDrawerOpen(GravityCompat.START)) {
                    mDrawer.closeDrawers();
                } else {
                    mDrawer.openDrawer(GravityCompat.START);
                }
            }
        });
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

    private void updateOnlineTextView() {
        if (usersOnlineHolder == null || tvUsersOnline == null)
            return;
        if (activeUsers == -1 && joinedUsers == -1) {
            tvUsersOnline.setVisibility(View.GONE);
            return;
        }
        tvUsersOnline.setVisibility(View.VISIBLE);
        StringBuilder result = new StringBuilder();
        if (activeUsers != -1) {
            result.append("<B>");
            result.append(NumberFormat.getInstance().format(activeUsers));
            result.append("</B>");
        }
        if (joinedUsers != -1) {
            if (activeUsers != -1) {
                result.append(" of ");
            }
            result.append(NumberFormat.getInstance().format(joinedUsers));
        }
        tvUsersOnline.setText(Html.fromHtml(result.toString()));
    }

    private int joinedUsers = -1;
    private void updateJoinedUsers(int count) {
        joinedUsers = count;
        updateOnlineTextView();
    }
    private int activeUsers = -1;
    private void updateActiveUsers(int count) {
        activeUsers = count;
        updateOnlineTextView();
    }
        private ValueEventListener usersInChannelListener =
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    updateActiveUsers((int) dataSnapshot.getChildrenCount());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    updateActiveUsers(-1);
                    Log.e(LOG_TAG, "The read failed: " + databaseError.getMessage());
                }
            };
    private void initChannelUserCounter() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        // firebaseListeners will manage onDestroy automatically.
        Query tmp = ref.child("/usersInChannel/").child(app.getChannelId());
        tmp.addValueEventListener(usersInChannelListener);
        firebaseListeners.put(usersInChannelListener, tmp);
        updateJoinedUsers(app.getChannel().getUserList().size());
        updateOnlineTextView();
    }

    @Override
    public void onChannelUpdated() {
        super.onChannelUpdated();
        if (app.getChannel() != null) {
            if (app.getChannel().getUserList() != null)
                updateJoinedUsers(app.getChannel().getUserList().size());
        } else {
            updateJoinedUsers(-1);
        }
    }
}
