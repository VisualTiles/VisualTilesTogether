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
import com.javierarboleda.visualtilestogether.fragments.ColorSelectFragment;
import com.javierarboleda.visualtilestogether.fragments.EffectSelectFragment;
import com.javierarboleda.visualtilestogether.fragments.PresentationFragment;
import com.javierarboleda.visualtilestogether.fragments.TileListFragment;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.TileEffect;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created on 11/15/16.
 */

public class ModeratorConsoleActivity extends AppCompatActivity
                implements PresentationFragment.PresentationFragmentListener,
                    TileListFragment.TileListFragmentListener,
                    EffectSelectFragment.EffectSelectFragmentListener,
                    ColorSelectFragment.ColorSelectFragmentListener {
    private ActivityModeratorConsole1Binding binding;
    private VisualTilesTogetherApp app;

    private int mPagePosition = 0;
    private Tile mSelectedTile;
    private String mSelectedEffect;
    private boolean mMultiTile;

    private ColorSelectFragment.ColorFillMode colorFillMode =
            ColorSelectFragment.ColorFillMode.SINGLE_TILE;
    private Integer selectedColor = null;

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

    @Override
    public void onTileTapped(int position, Tile tile) {
        switch(mPagePosition) {
            case 0:
                if (mSelectedTile != null) {
                    final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                    final DatabaseReference channelRef = dbRef.child(Channel.TABLE_NAME)
                            .child(app.getChannelId());
                    updateDbPositionToTileId(channelRef, tile, position);
                }
                break;
            case 1:
                if (mMultiTile) {
                    initiateAndUpdateTileEffect(0, app.getChannel().getPositionToTileIds().size());
                } else if (mSelectedEffect != null && !mSelectedEffect.isEmpty()) {
                    initiateAndUpdateTileEffect(position, position + 1);
                }
                break;
            case 2:
                switch (colorFillMode) {
                    case MULTI_TILE:
                        initiateAndUpdateTileColor(
                                0, app.getChannel().getPositionToTileIds().size());
                        break;
                    case SINGLE_TILE:
                        initiateAndUpdateTileColor(position, position + 1);
                        break;
                    case BACKGROUND:
                        // Background switches don't change on tile tap.
                        break;
                }
                break;
        }
    }

    private void initiateAndUpdateTileEffect(final int start, final int end) {
        if (app.getChannel().getPositionToTileIds() == null)
            return;
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child(Tile.TABLE_NAME);
        HashMap<String, Object> effectMap = new HashMap<>();
        TileEffect tileEffect = new TileEffect();
        tileEffect.setEffectDurationPct(1.0);
        tileEffect.setEffectOffsetPct(0.0);
        tileEffect.setStartTimeMillis(0L);
        tileEffect.setEffectType(mSelectedEffect);
        for (int i = start; i < end; i++) {
            String tileKey = app.getChannel().getPositionToTileIds().get(i);
            if (tileKey == null) continue;
            effectMap.put(tileKey + "/" + Tile.TILE_EFFECT, tileEffect);
        }
        if (effectMap.size() > 0)
            dbRef.updateChildren(effectMap);
    }


    private void initiateAndUpdateTileColor(final int start, final int end) {
        if (app.getChannel().getPositionToTileIds() == null)
            return;
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                .child(Tile.TABLE_NAME);
        HashMap<String, Object> effectMap = new HashMap<>();
        for (int i = start; i < end; i++) {
            String tileKey = app.getChannel().getPositionToTileIds().get(i);
            if (tileKey == null) continue;
            effectMap.put(tileKey + "/" + Tile.TILE_COLOR, selectedColor);
        }
        if (effectMap.size() > 0)
            dbRef.updateChildren(effectMap);
    }


    @Override
    public void updateSelectedTile(Tile tile) {
        mSelectedTile = tile;
    }

    @Override
    public void updateSelectedEffect(boolean broadcastToTilesNow, String effect) {
        mSelectedEffect = effect;
        if (broadcastToTilesNow) {
            if (app.getChannel().getPositionToTileIds() == null)
                return;
            initiateAndUpdateTileEffect(0, app.getChannel().getPositionToTileIds().size());
        }
    }

    @Override
    public void updateMultiTile(boolean multiTile) {
        mMultiTile = multiTile;
    }

    @Override
    public void updateFillMode(ColorSelectFragment.ColorFillMode mode) {
        colorFillMode = mode;
    }

    @Override
    public void updateSelectedColor(ColorSelectFragment.ColorFillMode mode, Integer color) {
        colorFillMode = mode;
        selectedColor = color;
        // Broadcast now for selected modes.
        switch (mode) {
            case MULTI_TILE:
                if (app.getChannel().getPositionToTileIds() != null) {
                    // Remove tile color.
                    selectedColor = null;
                    initiateAndUpdateTileColor(0, app.getChannel().getPositionToTileIds().size());
                    selectedColor = color;
                }
                app.getChannel().setDefaultTileColor(selectedColor);
                FirebaseDatabase.getInstance().getReference()
                        .child(Channel.TABLE_NAME).child(app.getChannelId())
                        .setValue(app.getChannel());
                break;
            case BACKGROUND:
                app.getChannel().setChannelBackgroundColor(selectedColor);
                FirebaseDatabase.getInstance().getReference()
                        .child(Channel.TABLE_NAME).child(app.getChannelId())
                        .setValue(app.getChannel());
                break;
        }
    }
}
