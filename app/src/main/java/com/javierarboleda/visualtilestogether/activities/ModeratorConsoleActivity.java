package com.javierarboleda.visualtilestogether.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.adapters.ModeratorConsolePagerAdapter;
import com.javierarboleda.visualtilestogether.databinding.ActivityModeratorConsole1Binding;
import com.javierarboleda.visualtilestogether.fragments.EffectSelectFragment;
import com.javierarboleda.visualtilestogether.fragments.PresentationFragment;
import com.javierarboleda.visualtilestogether.fragments.TileListFragment;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.TileEffect;

import java.util.ArrayList;

/**
 * Created on 11/15/16.
 */

public class ModeratorConsoleActivity extends AppCompatActivity
                implements PresentationFragment.PresentationFragmentListener,
                    TileListFragment.TileListFragmentListener,
                    EffectSelectFragment.EffectSelectFragmentListener{

    private ActivityModeratorConsole1Binding binding;
    private VisualTilesTogetherApp app;

    private int mPagePosition = 0;
    private Tile mSelectedTile;
    private String mSelectedEffect;
    private boolean mMultiTile;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check user is moderator.
        app = (VisualTilesTogetherApp) getApplication();
        if (!app.isChannelModerator()) finish();

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
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPagePosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        binding.tabLayout.setupWithViewPager(viewPager);
    }

    private void setUpToolbar() {
        setSupportActionBar(binding.toolbar);
    }

    // run a transaction to to update the tileId of the position in channel
    private void updateDbPositionToTileId(DatabaseReference channelRef, final Tile tile,
                                          final int position) {

        channelRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Channel channel = mutableData.getValue(Channel.class);
                if (channel == null) {
                    return Transaction.success(mutableData);
                }

                ArrayList<String> positionToTileIds = channel.getPositionToTileIds();

                if (positionToTileIds == null) {
                    positionToTileIds = new ArrayList<>();
                    channel.setPositionToTileIds(positionToTileIds);
                }
                try {
                    positionToTileIds.get(position);
                } catch ( IndexOutOfBoundsException e ) {

                    for (int i = 0; i <= position; i++) {
                        try {
                            positionToTileIds.get(i);
                        } catch (IndexOutOfBoundsException ex) {
                            positionToTileIds.add(i, "");
                        }
                    }

                } finally {
                    channel.getPositionToTileIds().set(position, mSelectedTile.getTileId());
                }

                mutableData.setValue(channel);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
//                Log.d(LOG_TAG, "tileTransaction:onComplete: " + databaseError);
            }
        });
    }

    // run a transaction to to update the tileId of the position in channel
    private void updateDbTileEffect(DatabaseReference channelRef) {

        channelRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Tile tile = mutableData.getValue(Tile.class);
                if (tile == null) {
                    return Transaction.success(mutableData);
                }

                if (tile.getTileEffect() == null) {
                    TileEffect tileEffect = new TileEffect();
                    tileEffect.setEffectDurationPct(1.0);
                    tileEffect.setEffectOffsetPct(0.0);
                    tileEffect.setStartTimeMillis(1L);
                    tileEffect.setEffectType(mSelectedEffect);
                    tile.setTileEffect(tileEffect);
                } else {
                    tile.getTileEffect().setEffectType(mSelectedEffect);
                }

                mutableData.setValue(tile);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
//                Log.d(LOG_TAG, "tileTransaction:onComplete: " + databaseError);
            }
        });
    }

    @Override
    public void onTileTapped(int position, Tile tile) {

        if (mSelectedTile != null && mPagePosition == 0) {

            final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
            final DatabaseReference channelRef = dbRef.child(Channel.TABLE_NAME)
                    .child(app.getChannelId());

            updateDbPositionToTileId(channelRef, tile, position);
        } else if (mMultiTile) {

            for (int i = 0; i < app.getChannel().getPositionToTileIds().size(); i++) {
                initiateAndUpdateTileEffect(i);
            }

        } else if (mSelectedEffect != null && !mSelectedEffect.isEmpty() && mPagePosition == 1) {

            initiateAndUpdateTileEffect(position);
        }
    }

    private void initiateAndUpdateTileEffect(int position) {
        String tileKey = app.getChannel().getPositionToTileIds().get(position);

        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference channelRef = dbRef.child(Tile.TABLE_NAME)
                .child(tileKey);

        updateDbTileEffect(channelRef);
    }

    @Override
    public void updateSelectedTile(Tile tile) {
        mSelectedTile = tile;
    }

    @Override
    public void updateSelectedEffect(String effect) {
        mSelectedEffect = effect;
    }

    @Override
    public void updateMultiTile(boolean multiTile) {
        mMultiTile = multiTile;
    }
}
