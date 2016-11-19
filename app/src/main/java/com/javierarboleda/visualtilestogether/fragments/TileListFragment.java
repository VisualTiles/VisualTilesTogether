package com.javierarboleda.visualtilestogether.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.User;

public abstract class TileListFragment extends Fragment {
    private static final String LOG_TAG = TileListFragment.class.getSimpleName();

    private ProgressBar mProgressBar;
    private RecyclerView mRvTileList;
    private FirebaseRecyclerAdapter<Tile, TileListFragment.TileViewholder> mFirebaseAdapter;
    private Context mContext;
    private LinearLayoutManager mLinearLayoutManager;
    private VisualTilesTogetherApp visualTilesTogetherApp;

    // For moderator console mode
    public boolean mConsoleMode;
    private SparseBooleanArray mSelectedItems;
    private RelativeLayout mLastChecked;
    private String mSelectedTileRefId;


    public static class TileViewholder extends RecyclerView.ViewHolder {
        ImageView ivShape;
        ImageButton ibUpVote;
        ImageButton ibDownVote;
        TextView tvVotesTotal;
        Toolbar tbTileListItem;
        MenuItem miPublish;
        RelativeLayout rlMain;

        public TileViewholder(View itemView) {
            super(itemView);
            ivShape = (ImageView) itemView.findViewById(R.id.ivShape);
            ibUpVote = (ImageButton) itemView.findViewById(R.id.ibUpVote);
            ibDownVote = (ImageButton) itemView.findViewById(R.id.ibDownVote);
            tvVotesTotal = (TextView) itemView.findViewById(R.id.tvVotesTotal);
            tbTileListItem = (Toolbar) itemView.findViewById((R.id.tbTileListItem));
            tbTileListItem.inflateMenu(R.menu.tile_list_menu);
            miPublish = tbTileListItem.getMenu().findItem(R.id.action_publish);
            rlMain = (RelativeLayout) itemView.findViewById(R.id.rlMain);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSelectedItems = new SparseBooleanArray();

        if (getArguments() != null) {
            mConsoleMode = getArguments().getBoolean("consoleMode", false);
        }

        visualTilesTogetherApp =  (VisualTilesTogetherApp) getActivity().getApplication();
        // get the shapes folder of Firebase Storage for this app
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // this should grab https://visual-tiles-together.firebaseio.com/
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference dbUsers = dbRef.child(User.TABLE_NAME);
        dbUsers.child(visualTilesTogetherApp.getUid()).setValue(visualTilesTogetherApp.getUser());

        final View view = inflater.inflate(R.layout.fragment_tile_list, container, false);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mRvTileList = (RecyclerView) view.findViewById(R.id.rvTileList);

        // bind the tiles table to the RecyclerView
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Tile, TileListFragment.TileViewholder>
                (Tile.class,
                        //if this is console mode, then different list item layout file
                        mConsoleMode ? R.layout.tile_selector_list_item: R.layout.tile_list_item,
                        TileListFragment.TileViewholder.class,
                        getDbQuery(dbRef.child(Tile.TABLE_NAME))) {

            @Override
            protected void populateViewHolder(
                    final TileListFragment.TileViewholder viewHolder, final Tile tile, final int position) {
                final DatabaseReference tileRef = getRef(position);

                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                if (tile.getShapeUrl() != null && mContext != null) {
                    Glide.with(mContext)
                            .load(tile.getShapeUrl())
                            .into(viewHolder.ivShape);
                }

                viewHolder.ibUpVote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onVoteClicked(tileRef, 1);
                    }
                });

                viewHolder.ibDownVote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onVoteClicked(tileRef, -1);
                    }
                });

                viewHolder.tvVotesTotal.setText(String.valueOf(tile.getPosVotes() - tile.getNegVotes()));

                viewHolder.miPublish.setIcon(tile.isApproved()?
                        R.drawable.ic_unpublish_black_24px : R.drawable.ic_publish_black_24px);
                viewHolder.tbTileListItem.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Log.d(LOG_TAG, "Clicked a toolbar item");
                        switch (item.getItemId()) {
                            case R.id.action_publish:
                                onToggleApproval(tileRef);
                                return true;
                            case R.id.action_delete:
                                onDeleteTile(tileRef);
                                return true;
                        }
                        return false;
                    }
                });

                if (mConsoleMode) {
                    viewHolder.ivShape.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (mLastChecked == null) {

                                viewHolder.rlMain.setSelected(true);
                                mSelectedTileRefId = tileRef.getKey();
                                mLastChecked = viewHolder.rlMain;

                            } else if (mSelectedTileRefId.equals(tileRef.getKey())) {

                                viewHolder.rlMain.setSelected(false);
                                mSelectedTileRefId = null;
                                mLastChecked = null;

                            } else {

                                mLastChecked.setSelected(false);
                                viewHolder.rlMain.setSelected(true);
                                mSelectedTileRefId = tileRef.getKey();
                                mLastChecked = viewHolder.rlMain;
                            }

//                            // Save the selected positions to the SparseBooleanArray
//                            if (mSelectedItems.get(position, false)) {
//                                mSelectedItems.delete(position);
//                                viewHolder.rlMain.setSelected(false);
//                            }
//                            else {
//                                mSelectedItems.put(position, true);
//                                viewHolder.rlMain.setSelected(true);
//                            }
                        }
                    });
                }
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
        mLinearLayoutManager = new LinearLayoutManager(mContext);
        mLinearLayoutManager.setStackFromEnd(true);

        if (mConsoleMode) {
            mLinearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        }

        mRvTileList.setLayoutManager(mLinearLayoutManager);
        mRvTileList.setAdapter(mFirebaseAdapter);
        return view;
    }

    private void onDeleteTile(DatabaseReference tileRef) {
        tileRef.removeValue();
    }

    // run a transaction to uptick positive votes or negative votes
    // depending on the value of the vote increment
    private void onVoteClicked(DatabaseReference tileRef, final int voteIncrement) {
        tileRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Tile tile = mutableData.getValue(Tile.class);
                if (tile == null) {
                    return Transaction.success(mutableData);
                }

                if (voteIncrement > 0) {
                    tile.setPosVotes(tile.getPosVotes() + voteIncrement);
                } else if (voteIncrement < 0) {
                    // actually increments
                    tile.setNegVotes(tile.getNegVotes() - voteIncrement);
                }

                mutableData.setValue(tile);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "tileTransaction:onComplete: " + databaseError);
            }
        });
    }

    // run a transaction to uptick positive votes or negative votes
    // depending on the value of the vote increment
    private void onToggleApproval(DatabaseReference tileRef) {
        tileRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Tile tile = mutableData.getValue(Tile.class);
                if (tile == null) {
                    return Transaction.success(mutableData);
                }

                tile.setApproved(!tile.isApproved());

                mutableData.setValue(tile);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.d(LOG_TAG, "tileTransaction:onComplete: " + databaseError);
            }
        });
    }

    abstract Query getDbQuery(DatabaseReference dbRef);

    @Override
    public void onDetach() {
        mContext = null;
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}
