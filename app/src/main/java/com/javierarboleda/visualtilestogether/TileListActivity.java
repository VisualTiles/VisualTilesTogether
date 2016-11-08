package com.javierarboleda.visualtilestogether;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TileListActivity extends AppCompatActivity {

    private static final String TILES_TABLE = "tiles";
    private ProgressBar mProgressBar;
    private RecyclerView mRvTileList;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<TileContent, TileViewholder> mFirebaseAdapter;
    private Context mContext;
    private LinearLayoutManager mLinearLayoutManager;

    public static class TileViewholder extends RecyclerView.ViewHolder {
        public ImageView ivShape;
        public TextView tvYes;
        public TextView tvNo;

        public TileViewholder(View itemView) {
            super(itemView);
            ivShape = (ImageView) itemView.findViewById(R.id.ivShape);
            tvYes = (TextView) itemView.findViewById(R.id.tvYes);
            tvNo = (TextView) itemView.findViewById(R.id.tvNo);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_tile_list);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRvTileList = (RecyclerView) findViewById(R.id.rvTileList);

        // this should grab https://visual-tiles-together.firebaseio.com/
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // bind the tiles table to the RecyclerView
        mFirebaseAdapter = new FirebaseRecyclerAdapter<TileContent, TileViewholder>
                (TileContent.class,
                        R.layout.tile_list_item,
                        TileViewholder.class,
                        mFirebaseDatabaseReference.child(TILES_TABLE)) {

            @Override
            protected void populateViewHolder(TileViewholder viewHolder, TileContent tile, int position) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (tile.getShapeUrl() != null) {
                    Glide.with(mContext)
                            .load(tile.getShapeUrl())
                            .into(viewHolder.ivShape);
                }
                viewHolder.tvYes.setText(String.valueOf(tile.getPosVotes()));
                viewHolder.tvNo.setText(String.valueOf(tile.getNegVotes()));
            }
        };

        // watch for realtime changes
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int tileCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.
                        findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (tileCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRvTileList.scrollToPosition(positionStart);
                }
            }
        });

        // hook up the RecyclerView
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mRvTileList.setLayoutManager(mLinearLayoutManager);
        mRvTileList.setAdapter(mFirebaseAdapter);
    }
}
