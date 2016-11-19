package com.javierarboleda.visualtilestogether.fragments;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.Channel;

/**
 * Created on 11/17/16.
 */

public class TileSelectFragment extends TileListFragment {

    public static TileSelectFragment newInstance() {
        TileSelectFragment fragmentDemo = new TileSelectFragment();
        Bundle args = new Bundle();
        args.putBoolean("consoleMode", true);
        fragmentDemo.setArguments(args);
        return fragmentDemo;
    }

    @Override
    Query getDbQuery(DatabaseReference dbRef) {
        VisualTilesTogetherApp visualTilesTogetherApp = (VisualTilesTogetherApp) getActivity()
                .getApplication();
        return dbRef
                .child(visualTilesTogetherApp.getUser().getChannelId())
                .child(Channel.TILE_IDS)
                .orderByKey();
    }
}
