package com.javierarboleda.visualtilestogether.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.adapters.TileListPagerAdapter;

import static com.javierarboleda.visualtilestogether.VisualTilesTogetherApp.getFirebaseAuth;
import static com.javierarboleda.visualtilestogether.VisualTilesTogetherApp.resetUserame;

public class TileListActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String LOG_TAG = TileListActivity.class.getSimpleName();

    private GoogleApiClient mGoogleApiClient;

    public static class TileViewholder extends RecyclerView.ViewHolder {
        ImageView ivShape;
        ImageButton ibUpVote;
        ImageButton ibDownVote;
        TextView tvVotesTotal;

        public TileViewholder(View itemView) {
            super(itemView);
            ivShape = (ImageView) itemView.findViewById(R.id.ivShape);
            ibUpVote = (ImageButton) itemView.findViewById(R.id.ibUpVote);
            ibDownVote = (ImageButton) itemView.findViewById(R.id.ibDownVote);
            tvVotesTotal = (TextView) itemView.findViewById(R.id.tvVotesTotal);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_present:
                startActivity(new Intent(this, PresentationActivity.class));
                return true;
            case R.id.menu_item_sign_out:
                getFirebaseAuth().signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                resetUserame();
                startActivity(new Intent(this, SignInActivity.class));
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
}
