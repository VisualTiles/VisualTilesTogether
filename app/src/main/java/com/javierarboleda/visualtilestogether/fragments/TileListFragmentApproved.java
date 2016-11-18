package com.javierarboleda.visualtilestogether.fragments;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.Channel;

public class TileListFragmentApproved extends TileListFragment {
    @Override
    Query getDbQuery(DatabaseReference dbRef) {
        return dbRef
                .child(VisualTilesTogetherApp.getUser().getChannelId())
                .child(Channel.TILE_IDS)
                .orderByValue()
                .equalTo(true);
    }
}
