package com.javierarboleda.visualtilestogether.fragments;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.Tile;

public class TileListFragmentInChannel extends TileListFragment {
    @Override
    Query getDbQuery(DatabaseReference dbRef) {
        return dbRef
                .orderByChild(Tile.CHANNEL_ID)
                .equalTo(VisualTilesTogetherApp.getUser().getChannelId());
    }
}
