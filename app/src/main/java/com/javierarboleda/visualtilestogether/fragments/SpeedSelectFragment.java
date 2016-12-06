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

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.Locale;

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

    private void setSpeedViewText(int duration) {
        float secs = duration / 1000f;
        int bpm = 60000 / duration;
        binding.speedView.setText(
                String.format(Locale.US, "%.1f seconds / %d BPM.", secs, bpm));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        int duration = (int) app.getChannel().getMasterEffectDuration();
        binding.speedSeeker.setProgress(duration);
        setSpeedViewText(duration);

        binding.speedSeeker.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                // Round to increments of 100.
                return value - value % 100;
            }
        });
        binding.speedSeeker.setOnProgressChangeListener(
                new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                float secs = value / 1000f;
                int bpm = 60000 / value;
                String string = String.format(Locale.US, "%.1f", secs);
                setSpeedViewText(value);
                seekBar.setIndicatorFormatter(string);
                binding.speedView.setText(
                        String.format(Locale.US, "%.1f seconds / %d BPM.", secs, bpm));
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                listener.updateAnimationSpeed(seekBar.getProgress());
            }
        });
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
        binding.speedSeeker.setProgress((int) app.getChannel().getMasterEffectDuration());
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

