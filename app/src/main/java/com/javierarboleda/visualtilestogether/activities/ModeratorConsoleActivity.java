package com.javierarboleda.visualtilestogether.activities;

import android.animation.Animator;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.databinding.ActivityModeratorConsoleBinding;
import com.javierarboleda.visualtilestogether.fragments.ColorSelectFragment;
import com.javierarboleda.visualtilestogether.fragments.EffectSelectFragment;
import com.javierarboleda.visualtilestogether.fragments.LayoutSelectFragment;
import com.javierarboleda.visualtilestogether.fragments.PresentationFragment;
import com.javierarboleda.visualtilestogether.fragments.SpeedSelectFragment;
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
                    LayoutSelectFragment.LayoutSelectFragmentListener,
                    SpeedSelectFragment.SpeedSelectFragmentListener,
                    ViewAnimator.ViewAnimatorListener {
    private static final String TAG = ModeratorConsoleActivity.class.getSimpleName();
    private ActivityModeratorConsoleBinding binding;
    private VisualTilesTogetherApp app;

    private int mPagePosition = 0;
    private Tile mSelectedTile;
    private String mSelectedEffect;
    private boolean mMultiTile;

    private HashMap<String, Object> stagedChanges = new HashMap<>();
    private boolean isStagingChanges = false;
    private MenuItem menuItemStaging = null;

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
    private LinearLayout leftDrawerLayout;

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
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        leftDrawerLayout = (LinearLayout) findViewById(R.id.left_drawer);

        setActionBar();

        getSupportActionBar().setTitle(""); // Empty string.
        binding.toolbarTitle.setText(R.string.title_tile_select);
        contentFragment = new TileSelectFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentHolder, (Fragment) contentFragment)
                .commit();

        buildSlideMenu();
        viewAnimator = new ViewAnimator<>(this, list, contentFragment, drawerLayout, this);
    }

    private void updateDbPositionToTileId(DatabaseReference channelRef, final Tile tile,
                                          final int position) {
        ArrayList<String> positionToTileIds = app.getChannel().getPositionToTileIds();
        if (positionToTileIds == null) {
            positionToTileIds = new ArrayList<>();
            app.getChannel().setPositionToTileIds(positionToTileIds);
        }
        try {
            positionToTileIds.get(position);
        } catch ( IndexOutOfBoundsException e ) {
            for (int i = 0; i <= position; i++) {
                try {
                    positionToTileIds.get(i);
                } catch (IndexOutOfBoundsException ex) {
                    positionToTileIds.add(i, "");
                    stagedChanges.put(Channel.TABLE_NAME + "/" + Channel.POS_TO_TILE_IDS + "/" + i,
                            "");
                }
            }
        } finally {
            positionToTileIds.set(position, mSelectedTile.getTileId());
            stagedChanges.put(Channel.TABLE_NAME + "/" + Channel.POS_TO_TILE_IDS + "/" + position,
                    positionToTileIds.get(position));
        }
        app.notifyChannelUpdated();
        maybeCommitChanges();
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
            stagedChanges.put(Tile.TABLE_NAME + "/" + tileKey + "/" + Tile.TILE_EFFECT, tileEffect);
            // Update local cache.
            Tile tile = app.getTileObservableArrayMap().get(tileKey);
            tile.setTileEffect(tileEffect);
            app.getTileObservableArrayMap().put(tileKey, tile);
        }
        maybeCommitChanges();
    }


    private void initiateAndUpdateTileColor(final int start, final int end) {
        if (app.getChannel().getPositionToTileIds() == null)
            return;
        HashMap<String, Object> effectMap = new HashMap<>();
        for (int i = start; i < end; i++) {
            String tileKey = app.getChannel().getPositionToTileIds().get(i);
            if (tileKey == null) continue;
            stagedChanges.put(Tile.TABLE_NAME + "/" + tileKey + "/" + Tile.TILE_COLOR,
                    selectedColor);
            // Update local cache.
            Tile tile = app.getTileObservableArrayMap().get(tileKey);
            tile.setTileColor(selectedColor);
            app.getTileObservableArrayMap().put(tileKey, tile);
        }
        maybeCommitChanges();
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
                app.notifyChannelUpdated();
                stagedChanges.put(Channel.TABLE_NAME + "/" + app.getChannelId() + "/" +
                        Channel.DEFAULT_TILE_COLOR, selectedColor);
                maybeCommitChanges();
                break;
            case BACKGROUND:
                app.getChannel().setChannelBackgroundColor(selectedColor);
                app.notifyChannelUpdated();
                stagedChanges.put(Channel.TABLE_NAME + "/" + app.getChannelId() + "/" +
                        Channel.CHANNEL_BACKGROUND_COLOR, selectedColor);
                maybeCommitChanges();
                break;
        }
    }

    private void buildSlideMenu() {
        list.add(new SlideMenuItem("tiles", R.drawable.ic_grid_white));
        list.add(new SlideMenuItem("effects", R.drawable.ic_magic_wand));
        list.add(new SlideMenuItem("colors", R.drawable.ic_color_fill));
        list.add(new SlideMenuItem("speed", R.drawable.ic_speedometer_white));
        list.add(new SlideMenuItem("layout", R.drawable.ic_layout_white));
        list.get(0).setSelected(true);
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
                leftDrawerLayout.removeAllViews();
                leftDrawerLayout.invalidate();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                if (slideOffset > 0.6 && leftDrawerLayout.getChildCount() == 0)
                    viewAnimator.showMenuContent();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        binding.toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawers();
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });
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
        leftDrawerLayout.addView(view);
    }

    @Override
    public ScreenShotable onSwitch(Resourceble slideMenuItem, ScreenShotable screenShotable,
                                   int position) {
        switch (slideMenuItem.getName()) {
            case "close":
                return screenShotable;
            case "tiles":
                mPagePosition = 0;
                animateTitleChange(R.string.title_tile_select);
                return replaceFragment(screenShotable, new TileSelectFragment(), position);
            case "effects":
                mPagePosition = 1;
                animateTitleChange(R.string.title_effect_editor);
                return replaceFragment(screenShotable, new EffectSelectFragment(), position);
            case "colors":
                mPagePosition = 2;
                animateTitleChange(R.string.title_color_editor);
                return replaceFragment(screenShotable, new ColorSelectFragment(), position);
            case "speed":
                mPagePosition = 3;
                animateTitleChange(R.string.title_animation_speed);
                return replaceFragment(screenShotable, new SpeedSelectFragment(), position);
            case "layout":
                mPagePosition = 4;
                animateTitleChange(R.string.title_layout_switch);
                return replaceFragment(screenShotable, new LayoutSelectFragment(), position);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch(item.getItemId()) {
            case R.id.menu_item_staging:
                toggleStagingItem();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleStagingItem() {
        isStagingChanges = !isStagingChanges;
        if (menuItemStaging == null)
            return;
        menuItemStaging.setTitle(isStagingChanges ? R.string.end_staging : R.string.begin_staging);
        menuItemStaging.setIcon(isStagingChanges ? R.drawable.ic_un_hold: R.drawable.ic_hold);
        maybeCommitChanges();
    }

    private void animateTitleChange(final int newTitleId) {
        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(250);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (getSupportActionBar() == null)
                    return;
                binding.toolbarTitle.setText(newTitleId);
                AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
                fadeIn.setDuration(250);
                binding.toolbarTitle.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        binding.toolbarTitle.startAnimation(fadeOut);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_moderator, menu);
        menuItemStaging = menu.findItem(R.id.menu_item_staging);
        return true;
    }

    @Override
    public void updateChannelLayout(String layoutName) {
        app.getChannel().setLayoutId(layoutName);
        app.notifyChannelUpdated();
        stagedChanges.put(
                Channel.TABLE_NAME + "/" + app.getChannelId() + "/" + Channel.LAYOUT_NAME,
                layoutName);
        maybeCommitChanges();
    }

    @Override
    public void updateAnimationSpeed(int speedMs) {
        // Update local table (fake it for preview ).
        app.getChannel().setMasterEffectDuration((long) speedMs);
        // Fake notify.
        app.notifyChannelUpdated();
        stagedChanges.put(
                Channel.TABLE_NAME + "/" + app.getChannelId() + "/" + Channel.EFFECT_DURATION,
                speedMs);
        maybeCommitChanges();
    }

    public void maybeCommitChanges() {
        if (isStagingChanges || stagedChanges.size() == 0) {
            return;
        }
        FirebaseDatabase.getInstance().getReference().updateChildren(stagedChanges)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        stagedChanges.clear();
                    }
                });
    }

    @Override
    public void updateColorTransition(boolean enabled, int speedMs) {

    }
}
