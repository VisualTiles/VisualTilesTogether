package com.javierarboleda.visualtilestogether.adapters;


import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.firebase.database.Query;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;

/**
 * Created by geo on 11/22/16.
 */

public class TileSelectRecyclerViewAdapter extends TileListRecyclerViewAdapter {
    private RelativeLayout mLastChecked;
    private String mSelectedTileRefId;

    public TileSelectRecyclerViewAdapter(Context context, int itemLayout, Query query, VisualTilesTogetherApp visualTilesTogetherApp) {
        super(context, itemLayout, query, visualTilesTogetherApp);
    }

    @Override
    protected void doTheWork(final TileViewHolder viewHolder, String tileId) {
        super.doTheWork(viewHolder, tileId);
         viewHolder.ivShape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle selection and make sure only one item selected at a time
                if (mLastChecked == null) {
                    viewHolder.rlMain.setSelected(true);
                    mSelectedTileRefId = viewHolder.tileRef.getKey();
                    mLastChecked = viewHolder.rlMain;

                    viewHolder.tile.setTileId(viewHolder.tileRef.getKey());
                    mListener.updateSelectedTile(viewHolder.tile);
                } else if (mSelectedTileRefId.equals(viewHolder.tileRef.getKey())) {
                    viewHolder.rlMain.setSelected(false);
                    mSelectedTileRefId = null;
                    mLastChecked = null;

                    mListener.updateSelectedTile(null);
                } else {
                    mLastChecked.setSelected(false);
                    viewHolder.rlMain.setSelected(true);
                    mSelectedTileRefId = viewHolder.tileRef.getKey();
                    mLastChecked = viewHolder.rlMain;

                    viewHolder.tile.setTileId(viewHolder.tileRef.getKey());
                    mListener.updateSelectedTile(viewHolder.tile);
                }
            }
        });
    }
}
