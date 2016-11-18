package com.javierarboleda.visualtilestogether.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.javierarboleda.visualtilestogether.R;

/**
 * Created on 11/17/16.
 */

public class TileSelectFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tile_select, parent, false);
    }
}
