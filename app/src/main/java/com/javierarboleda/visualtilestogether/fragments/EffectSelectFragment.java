package com.javierarboleda.visualtilestogether.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.media.effect.EffectUpdateListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.databinding.FragmentEffectSelectBinding;

/**
 * Created on 11/17/16.
 */

public class EffectSelectFragment extends Fragment {

    FragmentEffectSelectBinding binding;
    EffectSelectFragmentListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_effect_select, parent, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        setOnClickListeners();

        super.onViewCreated(view, savedInstanceState);
    }

    private void setOnClickListeners() {
        binding.butFadeHalf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.updateSelectedEffect("FADE_HALF");
            }
        });

        binding.butFlip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.updateSelectedEffect("FLIP_HORIZONTAL");
            }
        });

        binding.butFlyAway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.updateSelectedEffect("FLY_AWAY");
            }
        });

        binding.butNone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.updateSelectedEffect("NONE");
            }
        });

        binding.butRotateLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.updateSelectedEffect("ROTATE_LEFT");
            }
        });

        binding.butRotateRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.updateSelectedEffect("ROTATE_RIGHT");
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
        void updateSelectedEffect(String effect);
    }
}
