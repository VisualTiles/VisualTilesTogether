package com.javierarboleda.visualtilestogether.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DatabaseError;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.adapters.TileListPagerAdapterModerator;
import com.javierarboleda.visualtilestogether.adapters.TileListPagerAdapterUser;
import com.javierarboleda.visualtilestogether.fragments.TileListFragment;
import com.javierarboleda.visualtilestogether.models.Tile;

import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.normalizeDb;

public class TileListActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        TileListFragment.TileListFragmentListener,
        VisualTilesTogetherApp.VisualTilesListenerInterface {
    private static final String LOG_TAG = TileListActivity.class.getSimpleName();
    private static final String CHANNEL_NAME = "channel name";

    private GoogleApiClient mGoogleApiClient;
    private VisualTilesTogetherApp app;

    private Menu menu;
    private Toolbar mToolbar;
    private DrawerLayout mDrawer;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_list);

        setUpToolbar();

        setUpNavDrawer();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(TileListActivity.this, TileCreationActivity.class));
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

        switch (menuItem.getItemId()) {
            case R.id.nav_present:
                startActivity(new Intent(this, PresentationActivity.class));
                break;
            case R.id.nav_console:
                startActivity(new Intent(this, ModeratorConsoleActivity.class));
                break;
            case R.id.nav_normalize_db:
                normalizeDb();
                break;
            case R.id.nav_show_code:
                final Snackbar snackBar = Snackbar.make(findViewById(R.id.clMainLayout),
                        app.getChannelId(), Snackbar.LENGTH_INDEFINITE);
                snackBar.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackBar.dismiss();
                    }
                });
                snackBar.show();
                break;
            case R.id.nav_leave_channel:
                app.leaveChannel();
                startActivity(new Intent(this, CreateJoinActivity.class));
                finish();
                break;
            case R.id.nav_sign_out:
                app.getFirebaseAuth().signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                startActivity(new Intent(this, SignInActivity.class));
                finish();
                break;
        }

        mDrawer.closeDrawers();
    }

    private void setUpToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        app = (VisualTilesTogetherApp) getApplication();
        app.addListener(this);

        actionBar.setTitle(app.getChannel().getName());
    }

    @Override
    protected void onDestroy() {
        app.removeListener(this);
        super.onDestroy();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.main_menu, menu);
//        this.menu = menu;
//        menu.setGroupVisible(R.id.menu_moderator_controls, app.isChannelModerator());
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getGroupId() == R.id.menu_item_normalize_db && !app.isChannelModerator()) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.menu_item_present:
                startActivity(new Intent(this, PresentationActivity.class));
                return true;
            case R.id.menu_item_moderator_console:
                startActivity(new Intent(this, ModeratorConsoleActivity.class));
                return true;
            case R.id.menu_item_leave_channel:
                app.leaveChannel();
                startActivity(new Intent(this, CreateJoinActivity.class));
                finish();
                return true;
            case R.id.menu_item_sign_out:
                app.getFirebaseAuth().signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.menu_item_show_code:
                final Snackbar snackBar = Snackbar.make(findViewById(R.id.clMainLayout), app.getChannelId(), Snackbar.LENGTH_INDEFINITE);

                snackBar.setAction("Dismiss", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackBar.dismiss();
                    }
                });
                snackBar.show();
                return true;
            case R.id.menu_item_normalize_db:
                normalizeDb();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateSelectedTile(Tile tile) {
        // Doing nothing here
    }

    @Override
    public void onChannelUpdated() {
        if (app.getChannel() == null) {
            startActivity(new Intent(this, CreateJoinActivity.class));
            finish();
        }
//        menu.setGroupVisible(R.id.menu_moderator_controls, app.isChannelModerator());
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


    @Override
    public void onError(DatabaseError error) {

    }

    @Override
    public void onTilesUpdated() {

    }

    @Override
    public void onUserUpdated() {
        if (app.getUser() == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
    }
}
