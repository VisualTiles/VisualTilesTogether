package com.javierarboleda.visualtilestogether.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.adapters.TileListPagerAdapter;
import com.javierarboleda.visualtilestogether.fragments.TileListFragment;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.Tile;

import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.normalizeDb;

public class TileListActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        TileListFragment.TileListFragmentListener,
        VisualTilesTogetherApp.VisualTilesListenerInterface {
    private static final String LOG_TAG = TileListActivity.class.getSimpleName();
    private static final String CHANNEL_NAME = "channel name";

    private GoogleApiClient mGoogleApiClient;
    private String mChannelName;
    private VisualTilesTogetherApp app;

    private Menu menu;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        app = (VisualTilesTogetherApp) getApplication();
        app.addListener(this);

        // we want the channel name in the action bar.
        // either we already have it or we need to go get it.
        if (savedInstanceState != null) {
            mChannelName = savedInstanceState.getString(CHANNEL_NAME);
            if (mChannelName != null) {
                actionBar.setTitle(mChannelName);
            }
        } else {
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            DatabaseReference channelRef = dbRef.child(Channel.TABLE_NAME);
            channelRef.child(app.getChannelId()).child(Channel.CHANNEL_NAME)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mChannelName = dataSnapshot.getValue(String.class);
                    if (mChannelName != null) {
                        actionBar.setTitle(mChannelName);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Do something?
                }
            });
        }

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
            TileListPagerAdapter tileListPagerAdapter = new TileListPagerAdapter(getSupportFragmentManager());
            ViewPager mViewPager = (ViewPager) findViewById(R.id.vpContainer);
            mViewPager.setAdapter(tileListPagerAdapter);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.tlTabs);
            tabLayout.setupWithViewPager(mViewPager);
        }
    }

    @Override
    protected void onDestroy() {
        app.removeListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        this.menu = menu;
        menu.setGroupVisible(R.id.menu_moderator_controls, app.isChannelModerator());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
            case R.id.menu_item_normalize_db:
                normalizeDb();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(CHANNEL_NAME, mChannelName);
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
        menu.setGroupVisible(R.id.menu_moderator_controls, app.isChannelModerator());
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
