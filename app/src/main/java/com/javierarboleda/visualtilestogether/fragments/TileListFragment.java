package com.javierarboleda.visualtilestogether.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.javierarboleda.visualtilestogether.R;
import com.javierarboleda.visualtilestogether.VisualTilesTogetherApp;
import com.javierarboleda.visualtilestogether.models.Channel;
import com.javierarboleda.visualtilestogether.models.Tile;
import com.javierarboleda.visualtilestogether.models.User;

import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.deleteTile;
import static com.javierarboleda.visualtilestogether.util.FirebaseUtil.toggleTileApproval;

public abstract class TileListFragment extends Fragment {
    private static final String LOG_TAG = TileListFragment.class.getSimpleName();

    private ProgressBar mProgressBar;
    private RecyclerView mRvTileList;
    private FirebaseRecyclerAdapter<Object, TileListFragment.TileViewholder> mFirebaseAdapter;
    private Context mContext;
    private LinearLayoutManager mLinearLayoutManager;
    private VisualTilesTogetherApp visualTilesTogetherApp;

    // For moderator console mode
    public boolean mConsoleMode;
    private RelativeLayout mLastChecked;
    private String mSelectedTileRefId;


    public static class TileViewholder extends RecyclerView.ViewHolder {
        ImageView ivShape;
        ImageButton ibUpVote;
        ImageButton ibDownVote;
        TextView tvVotesTotal;
        Toolbar tbTileListItem;
        MenuItem miPublish;
        ValueEventListener tileEventListener;
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
            tileEventListener = null;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Log.d(LOG_TAG, "enter onCreate");
        if (getArguments() != null) {
            mConsoleMode = getArguments().getBoolean("consoleMode", false);
        }

//        Log.d(LOG_TAG, "enter onCreate");
        visualTilesTogetherApp =  (VisualTilesTogetherApp) getActivity().getApplication();
        // get the shapes folder of Firebase Storage for this app
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
//        Log.d(LOG_TAG, "exit onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        Log.d(LOG_TAG, "enter onCreateView");
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference dbUsers = dbRef.child(User.TABLE_NAME);
        dbUsers.child(visualTilesTogetherApp.getUid()).setValue(visualTilesTogetherApp.getUser());

        final View view = inflater.inflate(R.layout.fragment_tile_list, container, false);

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mRvTileList = (RecyclerView) view.findViewById(R.id.rvTileList);
        // for now don't light up the ProgressBar
        // TODO: show "no tiles" instead of ProgressBar when tile list is empty
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // bind the tiles table to the RecyclerView
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Object, TileListFragment.TileViewholder>
                (Object.class,
                        //if this is console mode, then different list item layout file
                        mConsoleMode ? R.layout.tile_selector_list_item: R.layout.tile_list_item,
                        TileListFragment.TileViewholder.class,
                        getDbQuery(dbRef.child(Channel.TABLE_NAME))) {

            @Override
            protected void populateViewHolder(
                    final TileListFragment.TileViewholder viewHolder, final Object object, int position) {
                // if the key starts with a '-' then it must be a tileId...
                if (getRef(position).getKey().charAt(0) == '-') {
                    doTheWork(viewHolder, getRef(position).getKey());
                } else { // ...otherwise the tileId is the value, which we now have to go get
                    getRef(position).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            doTheWork(viewHolder, dataSnapshot.getValue(String.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

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
//        Log.d(LOG_TAG, "exit onCreateView");
        return view;
    }

    protected void doTheWork(final TileViewholder viewHolder, final String tileId) {
        final DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference tileRef = dbRef.child(Tile.TABLE_NAME)
                .child(tileId);
//                Log.d(LOG_TAG, "populateViewHolder key, tileref: " + getRef(position).getKey() + ", " + tileRef);

        if (viewHolder.tileEventListener != null) {
            tileRef.removeEventListener(viewHolder.tileEventListener);
        }
        viewHolder.tileEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Tile tile = dataSnapshot.getValue(Tile.class);
//                        Log.d(LOG_TAG, "onDataChange tile: " + tile);
                if (tile == null) {
                    return;
                }
                if (tile.getShapeUrl() != null && mContext != null) {
                    Glide.with(mContext)
                            .load(tile.getShapeUrl())
                            .into(viewHolder.ivShape);
                }

                viewHolder.ibUpVote.setEnabled(true);
                viewHolder.ibDownVote.setEnabled(true);

                final String userId = visualTilesTogetherApp.getUid();

                if (dataSnapshot.child(Tile.USER_VOTES).child(userId).exists()) {
                    boolean userVote = dataSnapshot.child(Tile.USER_VOTES)
                            .child(userId)
                            .getValue(boolean.class);
                    if (userVote) {
                        // the user has already voted "yes"
                        viewHolder.ibUpVote.setEnabled(false);
                    } else {
                        // the user has already voted "no"
                        viewHolder.ibDownVote.setEnabled(false);
                    }
                }

                viewHolder.ibUpVote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (viewHolder.ibDownVote.isEnabled()) {
                            // the user has voted "yes"
                            onVoteClicked(tileRef, 1, 0);
                            tileRef.child(Tile.USER_VOTES)
                                    .child(userId)
                                    .setValue(true);
                        } else {
                            // the user has retracted a "no" vote
                            onVoteClicked(tileRef, 0, -1);
                            tileRef.child(Tile.USER_VOTES)
                                    .child(userId)
                                    .removeValue();
                        }
                    }
                });

                viewHolder.ibDownVote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (viewHolder.ibUpVote.isEnabled()) {
                            // the user has voted "no"
                            onVoteClicked(tileRef, 0, 1);
                            tileRef.child(Tile.USER_VOTES)
                                    .child(userId)
                                    .setValue(false);
                        } else {
                            // the user has retracted a "yes" vote
                            onVoteClicked(tileRef, -1, 0);
                            tileRef.child(Tile.USER_VOTES)
                                    .child(userId)
                                    .removeValue();
                        }
                    }
                });

                viewHolder.tvVotesTotal.setText(String.valueOf(tile.getPosVotes() - tile.getNegVotes()));

                viewHolder.miPublish.setIcon(tile.isApproved()?
                        R.drawable.ic_unpublish_black_24px : R.drawable.ic_publish_black_24px);
                viewHolder.tbTileListItem.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
//                                Log.d(LOG_TAG, "Clicked a toolbar item");
                        switch (item.getItemId()) {
                            case R.id.action_publish:
                                toggleTileApproval(tileRef);
                                return true;
                            case R.id.action_delete:
                                deleteTile(tileRef, tileId, tile.getChannelId());
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        tileRef.addValueEventListener(viewHolder.tileEventListener);
    }

    // run a transaction to uptick positive votes or negative votes
    // depending on the value of the vote increment
    private void onVoteClicked(DatabaseReference tileRef, final int posVote, final int negVote) {
        tileRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                Tile tile = mutableData.getValue(Tile.class);
                if (tile == null) {
                    return Transaction.success(mutableData);
                }

                tile.setPosVotes(tile.getPosVotes() + posVote);
                tile.setNegVotes(tile.getNegVotes() + negVote);

                mutableData.setValue(tile);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
//                Log.d(LOG_TAG, "tileTransaction:onComplete: " + databaseError);
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
