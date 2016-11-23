package com.javierarboleda.visualtilestogether.fragments;


import android.support.v7.widget.LinearLayoutManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.adapters.TileListRecyclerViewAdapter;
import com.javierarboleda.visualtilestogether.models.Channel;

public class TileListFragmentInChannel extends TileListFragment {

    Query getDbQuery(DatabaseReference dbRef) {
        return dbRef
                .child(visualTilesTogetherApp.getUser().getChannelId())
                .child(Channel.TILE_IDS)
                .orderByKey();
    }

    @Override
    TileListRecyclerViewAdapter getAdapter(DatabaseReference dbRef) {
        return new TileListRecyclerViewAdapter(getContext(),
                R.layout.tile_list_item,
                getDbQuery(dbRef.child(Channel.TABLE_NAME)),
                visualTilesTogetherApp);
    }

    @Override
    LinearLayoutManager getLayoutManager() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        return layoutManager;
    }
}
