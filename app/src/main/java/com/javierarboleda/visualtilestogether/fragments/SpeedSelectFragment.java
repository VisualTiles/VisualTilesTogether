package com.javierarboleda.visualtilestogether.fragments;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseError;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.databinding.FragmentSpeedSelectBinding;
import com.javierarboleda.visualtilestogether.util.sidemenu.interfaces.ScreenShotable;

/**
 * Created on 12/4/16.
 */

public class SpeedSelectFragment extends Fragment
        implements ScreenShotable, VisualTilesTogetherApp.VisualTilesListenerInterface {
    private FragmentSpeedSelectBinding binding;
    private Bitmap bitmap = null;
    private Activity mContext;
    private VisualTilesTogetherApp app;
    private SpeedSelectFragmentListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_speed_select, parent, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = getActivity();
        app = (VisualTilesTogetherApp) getActivity().getApplication();
        app.addListener(this);
        if (context instanceof SpeedSelectFragmentListener) {
            listener = (SpeedSelectFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SpeedSelectFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        mContext = null;
        app.removeListener(this);
        app = null;
        super.onDetach();
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public void takeScreenShot() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                if (binding.mainLayout == null) {
                    SpeedSelectFragment.this.bitmap = null;
                    return;
                }
                final Bitmap bitmap = Bitmap.createBitmap(binding.mainLayout.getWidth(),
                        binding.mainLayout.getHeight(), Bitmap.Config.ARGB_8888);
                final Canvas canvas = new Canvas(bitmap);
                // Draw must run on UI thread.
                mContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.mainLayout.draw(canvas);
                        SpeedSelectFragment.this.bitmap = bitmap;
                    }
                });
            }
        };
        thread.start();
    }

    @Override
    public void onChannelUpdated() {
        // Listen for speed field changes.
    }

    @Override
    public void onError(DatabaseError error) {
    }

    @Override
    public void onTilesUpdated() {
    }

    @Override
    public void onUserUpdated() {
    }

    public interface SpeedSelectFragmentListener {
        void updateAnimationSpeed(int speedMs);
        void updateColorTransition(boolean enabled, int speedMs);
    }
}

