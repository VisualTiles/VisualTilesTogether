package com.javierarboleda.visualtilestogether.fragments;

import android.support.v7.widget.LinearLayoutManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.adapters.TileListRecyclerViewAdapter;
import com.javierarboleda.visualtilestogether.adapters.TileSelectRecyclerViewAdapter;
import com.javierarboleda.visualtilestogether.models.Channel;

/**
 * Created on 11/17/16.
 */

public class TileSelectFragment extends TileListFragment {

    Query getDbQuery(DatabaseReference dbRef) {
        return dbRef
                .child(visualTilesTogetherApp.getUser().getChannelId())
                .child(Channel.TILE_IDS)
                .orderByValue()
                .equalTo(true);
    }

    @Override
    public int getBackgroundColorResId() {
        return R.color.tileSelectBackgroundColor;
    }

    @Override
    TileListRecyclerViewAdapter getAdapter(DatabaseReference dbRef) {
        return new TileSelectRecyclerViewAdapter(getContext(),
                R.layout.tile_selector_list_item,
                getDbQuery(dbRef.child(Channel.TABLE_NAME)),
                visualTilesTogetherApp);
    }

    @Override
    LinearLayoutManager getLayoutManager() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        return layoutManager;
    }
}
