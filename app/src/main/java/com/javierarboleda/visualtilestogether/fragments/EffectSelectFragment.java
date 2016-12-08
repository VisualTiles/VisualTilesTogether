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

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.databinding.FragmentEffectSelectBinding;
import com.javierarboleda.visualtilestogether.util.sidemenu.interfaces.ScreenShotable;

/**
 * Created on 11/17/16.
 */

public class EffectSelectFragment extends Fragment
    implements ScreenShotable {

    private FragmentEffectSelectBinding binding;
    private EffectSelectFragmentListener mListener;
    private View mSelectedButton;
    private Bitmap bitmap = null;
    private Activity mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_effect_select, parent, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        binding.ibSingleTileSelect.setSelected(true);

        setOnClickListeners();

        super.onViewCreated(view, savedInstanceState);
    }

    private void effectButtonClicked(View button) {
        boolean broadcastToTilesNow = binding.ibAllTileSelect.isSelected();
        if (button.isSelected()) {
            button.setSelected(false);
            mListener.updateSelectedEffect(false, null);
        }
        else {
            if (mSelectedButton != null) {
                mSelectedButton.setSelected(false);
            }
            mSelectedButton = button;
            button.setSelected(true);
            mListener.updateSelectedEffect(broadcastToTilesNow, (String) button.getTag());
        }
    }

    private void setOnClickListeners() {
        binding.ibSingleTileSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!binding.ibSingleTileSelect.isSelected()) {
                    binding.ibSingleTileSelect.setSelected(true);
                    binding.ibAllTileSelect.setSelected(false);
                    mListener.updateMultiTile(false);
                }
            }
        });
        binding.ibAllTileSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // ** below described approach not implemented (commented out) **
                // ** maybe implement initial approach after testing for a while **
                // when multi-select icon selected, then deselect any effects as the subsequent
                // effect select will initiate the update for that effect on all tiles

                if (!binding.ibAllTileSelect.isSelected()) {
//
//                    if (mSelectedButton != null) {
//                        mSelectedButton.setSelected(false);
//                        mListener.updateSelectedEffect(null);
//                    }

                    binding.ibSingleTileSelect.setSelected(false);
                    binding.ibAllTileSelect.setSelected(true);
                    mListener.updateMultiTile(true);
                }
            }
        });

        for (int k = 0; k < binding.buttons.getChildCount(); k++) {
            ViewGroup row = (ViewGroup) binding.buttons.getChildAt(k);
            for (int i = 0; i < row.getChildCount(); i++) {
                row.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        effectButtonClicked(view);
                    }
                });
            }
        }
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
                    EffectSelectFragment.this.bitmap = null;
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
                        EffectSelectFragment.this.bitmap = bitmap;
                    }
                });
            }
        };
        thread.start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = getActivity();
        if (context instanceof EffectSelectFragmentListener) {
            mListener = (EffectSelectFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement EffectSelectFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        mContext = null;
        mListener = null;
        super.onDetach();
    }

    public interface EffectSelectFragmentListener {
        void updateSelectedEffect(boolean broadcastToTilesNow, String effect);
        void updateMultiTile(boolean multiTile);
    }
}
