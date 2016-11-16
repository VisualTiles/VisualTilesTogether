package com.javierarboleda.visualtilestogether.fragments;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.Tile;

public class TileListFragmentApproved extends TileListFragment {
    @Override
    Query getDbQuery(DatabaseReference dbRef) {
//         TODO: Firebase can't handle multiple index queries,
//         so for now this does the same as TileListFragmentInChannel.
//         Need to implement a real solution
        return dbRef
                .orderByChild(Tile.CHANNEL_ID)
                .equalTo(VisualTilesTogetherApp.getUser().getChannelId());
    }
}
