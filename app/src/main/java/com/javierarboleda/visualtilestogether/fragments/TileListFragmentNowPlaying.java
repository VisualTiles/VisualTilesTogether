package com.javierarboleda.visualtilestogether.fragments;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.adapters.TileListRecyclerViewAdapter;
import com.javierarboleda.visualtilestogether.models.Channel;

public class TileListFragmentNowPlaying extends TileListFragment {

    Query getDbQuery(DatabaseReference dbRef) {
        return dbRef
                .child(visualTilesTogetherApp.getUser().getChannelId())
                .child(Channel.POS_TO_TILE_IDS)
                .orderByKey();
    }

    @Override
    TileListRecyclerViewAdapter getAdapter(DatabaseReference dbRef) {
        return new TileListRecyclerViewAdapter(getContext(),
                R.layout.tile_list_item,
                getDbQuery(dbRef.child(Channel.TABLE_NAME)),
                R.array.empty_tile_list_now_playing,
                visualTilesTogetherApp);
    }
}
