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
import com.javierarboleda.visualtilestogether.fragments.PresentationFragment;
import com.javierarboleda.visualtilestogether.models.PresentLayout;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.User;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by chris on 11/10/16.
 */

public class PresentationActivity extends AppCompatActivity
implements PresentationFragment.PresentationFragmentListener {
    private static final String TAG = PresentationActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presentation);

        PresentationFragment fragment = PresentationFragment.newInstance(false);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentHolder, fragment)
                .commit();
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
    }

    public static boolean isImmersiveAvailable() {
        return android.os.Build.VERSION.SDK_INT >= 19;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // beginFunAnimation();
    }

    @Override
    public void onTileTapped(int position, Tile tile) {
        // Do nothing.
    }
}
