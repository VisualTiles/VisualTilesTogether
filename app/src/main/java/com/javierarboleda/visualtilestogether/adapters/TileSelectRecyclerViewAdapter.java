package com.javierarboleda.visualtilestogether.adapters;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.Query;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;

/**
 * Created by geo on 11/22/16.
 */

public class TileSelectRecyclerViewAdapter extends TileListRecyclerViewAdapter {
    // Selected tile.
    private ViewGroup mLastChecked;
    private String mSelectedTileRefId;

    public TileSelectRecyclerViewAdapter(Context context, int itemLayout, Query query, VisualTilesTogetherApp visualTilesTogetherApp) {
        super(context, itemLayout, query, visualTilesTogetherApp);
    }

    @Override
    public void onViewAttachedToWindow(TileViewHolder holder) {
        if (holder.rlMain != null) {
            if (holder.tile != null &&
                    mSelectedTileRefId != null && mSelectedTileRefId.equals(
                    holder.tile.getTileId())) {
                holder.rlMain.setSelected(true);
            } else {
                holder.rlMain.setSelected(false);
            }
        }
        super.onViewAttachedToWindow(holder);
}

    @Override
    protected void doTheWork(final TileViewHolder viewHolder, final String tileId) {
        super.doTheWork(viewHolder, tileId);
         viewHolder.ivShape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = viewHolder.getAdapterPosition();
                String tileKey = viewHolder.tileRef.getKey();
                if (tileKey.equals(mSelectedTileRefId)) {
                    viewHolder.rlMain.setSelected(false);
                    mSelectedTileRefId = null;
                    mLastChecked = null;
                    mListener.updateSelectedTile(null);
                } else {
                    if(mLastChecked != null)
                        mLastChecked.setSelected(false);
                    mLastChecked = viewHolder.rlMain;
                    mLastChecked.setSelected(true);
                    mSelectedTileRefId = tileKey;
                    viewHolder.tile.setTileId(viewHolder.tileRef.getKey());
                    mListener.updateSelectedTile(viewHolder.tile);
                }

            }
        });
    }
}
