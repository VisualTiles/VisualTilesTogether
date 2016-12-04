package com.javierarboleda.visualtilestogether.activities;

import android.animation.Animator;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.databinding.ActivityModeratorConsoleBinding;
import com.javierarboleda.visualtilestogether.fragments.ColorSelectFragment;
import com.javierarboleda.visualtilestogether.fragments.EffectSelectFragment;
import com.javierarboleda.visualtilestogether.fragments.PresentationFragment;
import com.javierarboleda.visualtilestogether.fragments.TileListFragment;
import com.javierarboleda.visualtilestogether.fragments.TileSelectFragment;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.TileEffect;
import com.javierarboleda.visualtilestogether.util.sidemenu.interfaces.Resourceble;
import com.javierarboleda.visualtilestogether.util.sidemenu.interfaces.ScreenShotable;
import com.javierarboleda.visualtilestogether.util.sidemenu.model.SlideMenuItem;
import com.javierarboleda.visualtilestogether.util.sidemenu.util.ViewAnimator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.codetail.animation.ViewAnimationUtils;

/**
 * Created on 11/15/16.
 */

public class ModeratorConsoleActivity extends AppCompatActivity
                implements PresentationFragment.PresentationFragmentListener,
                    TileListFragment.TileListFragmentListener,
                    EffectSelectFragment.EffectSelectFragmentListener,
                    ColorSelectFragment.ColorSelectFragmentListener,
                    ViewAnimator.ViewAnimatorListener {
    private static final String TAG = ModeratorConsoleActivity.class.getSimpleName();
    private ActivityModeratorConsoleBinding binding;
    private VisualTilesTogetherApp app;

    private int mPagePosition = 0;
    private Tile mSelectedTile;
    private String mSelectedEffect;
    private boolean mMultiTile;

    private ColorSelectFragment.ColorFillMode colorFillMode =
            ColorSelectFragment.ColorFillMode.SINGLE_TILE;
    private Integer selectedColor = null;

    private ScreenShotable contentFragment;

    /* Experiment slide menu drawer layout UI Elements */
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private List<SlideMenuItem> list = new ArrayList<>();
    private ViewAnimator viewAnimator;
    private int res = R.drawable.ic_box_24dp;
    private LinearLayout linearLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check user is moderator.
        app = (VisualTilesTogetherApp) getApplication();
        if (!app.isChannelModerator()) finish();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_moderator_console);

        PresentationFragment mPresentationFragment = PresentationFragment.newInstance(true);
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragmentHolderMc, mPresentationFragment)
                .commit();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerLayout.setScrimColor(Color.TRANSPARENT);
        drawerLayout.setDrawerElevation(25f);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        linearLayout = (LinearLayout) findViewById(R.id.left_drawer);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
            }
        });

        contentFragment = new TileSelectFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentHolder, (Fragment) contentFragment)
                .commit();
        setActionBar();
        buildSlideMenu();
        viewAnimator = new ViewAnimator<>(this, list, contentFragment, drawerLayout, this);
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

    private void buildSlideMenu() {
        list.add(new SlideMenuItem("tiles", R.drawable.ic_grid_white));
        list.add(new SlideMenuItem("effects", R.drawable.ic_magic_wand));
        list.add(new SlideMenuItem("colors", R.drawable.ic_color_fill));
        list.add(new SlideMenuItem("speed", R.drawable.ic_speedometer_white));
        list.add(new SlideMenuItem("layout", R.drawable.ic_layout_white));
        list.add(new SlideMenuItem("close", R.drawable.ic_cancel_white_24px));
    }
    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                binding.toolbar,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                linearLayout.removeAllViews();
                linearLayout.invalidate();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (slideOffset > 0.6 && linearLayout.getChildCount() == 0)
                    viewAnimator.showMenuContent();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void addViewToContainer(View view) {
        linearLayout.addView(view);
    }

    @Override
    public ScreenShotable onSwitch(Resourceble slideMenuItem, ScreenShotable screenShotable,
                                   int position) {
        switch (slideMenuItem.getName()) {
            case "close":
                return screenShotable;
            case "tiles":
                mPagePosition = 0;
                return replaceFragment(screenShotable, new TileSelectFragment(), position);
            case "effects":
                mPagePosition = 1;
                return replaceFragment(screenShotable, new EffectSelectFragment(), position);
            case "colors":
                mPagePosition = 2;
                return replaceFragment(screenShotable, new ColorSelectFragment(), position);
        }
        return screenShotable;
    }

    private ScreenShotable replaceFragment(ScreenShotable oldFragment, ScreenShotable newFragment,
                                           int position) {
        if (!(contentFragment instanceof Fragment)) {
            Log.e(TAG, "Screenshotable must be a fragment!");
            return oldFragment;
        }
        View view = binding.fragmentHolder;
        int finalRadius = Math.max(view.getWidth(), view.getHeight());
        Animator animator = ViewAnimationUtils.createCircularReveal(view, 0, position, 0,
                finalRadius);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(ViewAnimator.CIRCULAR_REVEAL_ANIMATION_DURATION);

        binding.contentOverlay.setBackgroundDrawable(
                new BitmapDrawable(getResources(), oldFragment.getBitmap()));
        animator.start();
        contentFragment = newFragment;
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentHolder, (Fragment) newFragment)
                .commit();
        return newFragment;
    }

    @Override
    public void disableHomeButton() {
        getSupportActionBar().setHomeButtonEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void enableHomeButton() {
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout.closeDrawers();
    }
}
