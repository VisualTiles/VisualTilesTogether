package com.javierarboleda.visualtilestogether.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.databinding.FragmentEffectSelectBinding;

/**
 * Created on 11/17/16.
 */

public class EffectSelectFragment extends Fragment {

    private FragmentEffectSelectBinding binding;
    private EffectSelectFragmentListener mListener;
    private Button mSelectedButton;

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

    private void effectButtonClicked(Button button, String effect) {
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
            mListener.updateSelectedEffect(broadcastToTilesNow, effect);
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
        binding.butFadeHalf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                effectButtonClicked(binding.butFadeHalf, "FADE_HALF");
            }
        });

        binding.butFlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                effectButtonClicked(binding.butFlip, "FLIP_HORIZONTAL");
            }
        });

        binding.butFlyAway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                effectButtonClicked(binding.butFlyAway, "FLY_AWAY");
            }
        });

        binding.butNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                effectButtonClicked(binding.butNone, "NONE");
            }
        });

        binding.butRotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                effectButtonClicked(binding.butRotateLeft, "ROTATE_LEFT");
            }
        });

        binding.butRotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                effectButtonClicked(binding.butRotateRight, "ROTATE_RIGHT");
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EffectSelectFragmentListener) {
            mListener = (EffectSelectFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement EffectSelectFragmentListener");
        }
    }

    public interface EffectSelectFragmentListener {
        void updateSelectedEffect(boolean broadcastToTilesNow, String effect);
        void updateMultiTile(boolean multiTile);
    }
}
