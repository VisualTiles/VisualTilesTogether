package com.javierarboleda.visualtilestogether.fragments;

import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.Tile;

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
//         TODO: Firebase can't handle multiple index queries,
//         so for now this does the same as TileListFragmentInChannel.
//         Need to implement a real solution
        return dbRef
                .orderByChild(Tile.CHANNEL_ID)
                .equalTo(((VisualTilesTogetherApp) getActivity().getApplication()).getChannelId());
    }
}
