package com.javierarboleda.visualtilestogether.activities;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.percent.PercentFrameLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.javierarboleda.visualtilestogether.BuildConfig;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.PresentLayout;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.User;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by chris on 11/10/16.
 */

public class PresentationActivity extends AppCompatActivity implements
        PresentLayout.PresentLayoutListener {
    private static final String TAG = PresentationActivity.class.getSimpleName();
    public static final String PARAM_CHANNEL_ID = "channelId";

    private PercentFrameLayout mainLayout;
    private PercentFrameLayout viewContainer;
    private HashMap<Integer, ImageView> images;
    private HashMap<Integer, ImageView> tiles;

    private String channelId = "";
    private PresentLayout layout;
    private DatabaseReference database = null;
    private Query dbTiles;
    private ValueEventListener channelTileListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (layout == null) return;
            long tileCount = dataSnapshot.getChildrenCount();
            HashMap<Integer, Tile> loadedTiles = new HashMap<>();
            int tilePos = 0;
            for (DataSnapshot record : dataSnapshot.getChildren()) {
                loadedTiles.put(tilePos++, record.getValue(Tile.class));
            }
            Log.i(TAG, "Tile count: " + tileCount);
            // No tiles = no update.
            if (tilePos == 0)
                return;
            for (int i = 0; i < layout.getTileCount(); i++) {
                int pos = (int) (i % tileCount);
                Log.i(TAG, "Loading tile index " + i + " with tile pos " + pos);
                if (loadedTiles.get(pos) != null) {
                    layout.setTile(i, loadedTiles.get(pos));
                } else {
                    Log.e(TAG, "WTF Tile is null!?: " + pos);
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        images = new HashMap<>();
        tiles = new HashMap<>();

        setContentView(R.layout.activity_presentation);

        if (VisualTilesTogetherApp.getUser() == null ||
            VisualTilesTogetherApp.getChannel() ==  null) {
            finish();
        }
        if (layout == null) {
            layout = PresentLayout.createDemoLayout();
            /*
            if (database != null && !channelId.isEmpty()) {
                database.setValue(layout);
            }*/
        }

        layout.setListener(this);
        viewContainer = (PercentFrameLayout) findViewById(R.id.viewContainer);
        mainLayout = (PercentFrameLayout) findViewById(R.id.mainLayout);
        mainLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                makeFullscreen();
                return true;
            }
        });
        makeFullscreen();
    }



    private void makeFullscreen() {
        setFullscreen();
        if (Build.VERSION.SDK_INT > 10) {
            registerSystemUiVisibility();
        }
    }

    private void setFullscreen() {
        if (Build.VERSION.SDK_INT > 10) {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_FULLSCREEN;

            if (isImmersiveAvailable()) {
                flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public void exitFullscreen() {
        if (Build.VERSION.SDK_INT > 10) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void registerSystemUiVisibility() {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            setFullscreen();
                        }
                    }
                });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void unregisterSystemUiVisibility() {
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT > 10) {
            unregisterSystemUiVisibility();
        }
        exitFullscreen();
        if (dbTiles != null && channelTileListener != null)
            dbTiles.removeEventListener(channelTileListener);
    }

    public static boolean isImmersiveAvailable() {
        return android.os.Build.VERSION.SDK_INT >= 19;
    }

    private void updateLayout(PresentLayout layout, int transitionMs) {
        this.layout.setListener(this);
        this.layout = layout;
        // TODO(chris): This does a transition between current and new, and does drawing.
    }

    @Override
    protected void onResume() {
        super.onResume();
        drawLayout();
        // beginFunAnimation();
    }

    public PresentLayout stepLayout(PresentLayout left, PresentLayout right, float stepPercent) {
        // TODO(chris): This enables animation.
        return null;
    }

    private void drawLayout() {
        if (layout == null) return;
        for (int i = 0; i < layout.getTileCount(); i++) {
            ImageView view = null;
            if (tiles.get(i) == null) {
                view = buildTileView();
                viewContainer.addView(view);
                tiles.put(i, view);
            }
            moveRelativeView(view, layout.getTilePositions()[i]);
            loadTile(i, view, 0);
        }
        // Delete stale tiles.
        for (int i = layout.getTileCount(); i < tiles.size(); i++) {
            viewContainer.removeView(tiles.get(i));
        }
        for (int i = 0; i < layout.getImageCount(); i++) {
            ImageView view = null;
            if (images.get(i) == null) {
                view = buildTileView();
                viewContainer.addView(view);
                images.put(i, view);
            }
            moveRelativeView(view, layout.getImagePositions()[i]);
            Glide.with(this).load(layout.getImageUrls()[i])
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(view);
        }
        // Delete stale images.
        for (int i = layout.getImageCount(); i < images.size(); i++) {
            viewContainer.removeView(images.get(i));
        }
        Glide.with(this).load(layout.getBackgroundUrl()).asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(
                new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap>
                            glideAnimation) {
                        mainLayout.setBackground(new BitmapDrawable(resource));
                    }
                }
        );
        beginListeningForTiles();
    }
    private void beginListeningForTiles() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbTiles = dbRef.child(Tile.TABLE_NAME)
                .orderByChild(Tile.CHANNEL_ID)
                .equalTo(VisualTilesTogetherApp.getUser().getChannelId());
        dbTiles.addValueEventListener(channelTileListener);
    }

    private ImageView buildTileView() {
        return (ImageView) getLayoutInflater().inflate(R.layout.iv_tile, null);
    }

    private int lastColor = 0;
    private Handler tintAnimationHandler = new Handler();
    private Runnable tintAnimation = new Runnable() {
        @Override
        public void run() {
            Random random = new Random();
            int colorTo = Color.argb(255, random.nextInt(256), random.nextInt(256), random
                    .nextInt(256));
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                    lastColor, colorTo);
            colorAnimation.setDuration(1000);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    for (ImageView iv : tiles.values()) {
                        iv.setColorFilter((int) valueAnimator.getAnimatedValue());
                    }
                }
            });
            colorAnimation.start();
            colorAnimation.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    // Start another.
                    tintAnimationHandler.postDelayed(tintAnimation, 1000);
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            lastColor = colorTo;
        }
    };
    private void beginFunAnimation() {
        tintAnimationHandler.post(tintAnimation);
    }

    private void moveRelativeView(View view, Rect bounds) {
        if (BuildConfig.DEBUG && !(view.getLayoutParams()
                instanceof PercentFrameLayout.LayoutParams)) {
            throw new RuntimeException("View is not within a PercentRelativeLayout");
        }
        PercentFrameLayout.LayoutParams params = (PercentFrameLayout.LayoutParams)
                view.getLayoutParams();
        Point max = layout.getLayoutSize();
        params.getPercentLayoutInfo().topMarginPercent = bounds.top / (float) max.y;
        params.getPercentLayoutInfo().leftMarginPercent = bounds.left / (float) max.x;
        params.getPercentLayoutInfo().heightPercent = (bounds.bottom - bounds.top) / (float) max.y;
        params.getPercentLayoutInfo().widthPercent = (bounds.right - bounds.left) / (float) max.x;
        view.invalidate();
    }

    private void loadTile(int position, ImageView view, int durationMs) {
        if (view == null) {
            Log.e(TAG, "Null imageview reference in loadTile for position " + position);
            return;
        }
        if (layout == null) {
            Log.e(TAG, "Null layout reference in loadTile for position " + position);
            return;
        }
        Tile tile = layout.getTiles()[position];
        if (tile == null) {
            Log.e(TAG, "Null tile reference in loadTile for position " + position);
            return;
        }
        if (tile.getShapeUrl() == null) {
            Log.e(TAG, "Null getShapeUrl reference in loadTile for position " + position);
            return;
        }
        Glide.with(this).load(tile.getShapeUrl())
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .crossFade(durationMs)
                .into(view);
    }

    @Override
    public void updateTile(int position, Tile tile, int transitionMs) {
        loadTile(position, tiles.get(position), transitionMs);
    }
}
