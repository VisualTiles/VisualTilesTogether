package com.javierarboleda.visualtilestogether.fragments;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.adapters.TileListRecyclerViewAdapter;
import com.javierarboleda.visualtilestogether.models.User;

public class TileListFragmentMine extends TileListFragment {

    Query getDbQuery(DatabaseReference dbRef) {
        return dbRef
                .child(visualTilesTogetherApp.getUid())
                .child(User.TILE_IDS)
                .orderByValue().equalTo(visualTilesTogetherApp.getChannelId());
    }

    @Override
    TileListRecyclerViewAdapter getAdapter(DatabaseReference dbRef) {
        return new TileListRecyclerViewAdapter(getContext(),
                R.layout.tile_list_item,
                getDbQuery(dbRef.child(User.TABLE_NAME)),
                visualTilesTogetherApp);
    }
}
