package com.javierarboleda.visualtilestogether.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.adapters.ModeratorConsolePagerAdapter;
import com.javierarboleda.visualtilestogether.databinding.ActivityModeratorConsole1Binding;
import com.javierarboleda.visualtilestogether.fragments.PresentationFragment;
import com.javierarboleda.visualtilestogether.fragments.TileListFragment;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.Tile;

/**
 * Created on 11/15/16.
 */

public class ModeratorConsoleActivity extends AppCompatActivity
                implements PresentationFragment.PresentationFragmentListener,
                    TileListFragment.TileListFragmentListener {

    ActivityModeratorConsole1Binding binding;

    Tile mSelectedTile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_moderator_console_1);

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

    // run a transaction to to update the tileId of the position in channel
    private void updatePositionToTileId(DatabaseReference channelRef, final Tile tile,
                               final int position) {

        channelRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Channel channel = mutableData.getValue(Channel.class);
                if (channel == null) {
                    return Transaction.success(mutableData);
                }

                channel.getPositionToTileIds().add(position, tile.getChannelId());

                mutableData.setValue(channel);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
//                Log.d(LOG_TAG, "tileTransaction:onComplete: " + databaseError);
            }
        });
    }

    @Override
    public void onTileTapped(int position, Tile tile) {

        if (mSelectedTile != null) {

            final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            final DatabaseReference channelRef = dbRef.child(Channel.TABLE_NAME)
                    .child(tile.getChannelId());

            updatePositionToTileId(channelRef, tile, position);
        }

        Toast.makeText(this, "position:" + position + " tileId:" + tile.getTileId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateSelectedTile(Tile tile) {
        mSelectedTile = tile;
    }
}
